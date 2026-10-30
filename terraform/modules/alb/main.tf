variable "vpc_id" {}
variable "public_subnets" { type = list(string) }
variable "security_groups" { type = list(string) }
variable "environment" {}
variable "app_port" {}
variable "health_check_path" {}

resource "aws_lb" "main" {
  name               = "${var.environment}-alb"
  internal           = false
  load_balancer_type = "application"
  security_groups    = var.security_groups
  subnets            = var.public_subnets
  tags = { Name = "${var.environment}-alb" }
}

resource "aws_lb_target_group" "app_tg" {
  name        = "${var.environment}-cards-tg"
  port        = var.app_port
  protocol    = "HTTP"
  vpc_id      = var.vpc_id
  target_type = "instance"

  health_check {
    path                = var.health_check_path
    port                = tostring(var.app_port)
    protocol            = "HTTP"
    matcher             = "200"
    interval            = 30
    timeout             = 5
    healthy_threshold   = 2
    unhealthy_threshold = 3
  }
}

resource "aws_lb_listener" "front_end" {
  load_balancer_arn = aws_lb.main.arn
  port              = "80"
  protocol          = "HTTP"

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.app_tg.arn
  }
}

output "target_group_arn" { value = aws_lb_target_group.app_tg.arn }
output "alb_dns_name" { value = aws_lb.main.dns_name }
