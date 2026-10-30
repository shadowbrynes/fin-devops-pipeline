variable "vpc_zone_subnets" { type = list(string) }
variable "security_groups" { type = list(string) }
variable "key_name" {}
variable "target_group_arns" { type = list(string) }
variable "instance_type" {}
variable "environment" {}
variable "min_size" {}
variable "max_size" {}
variable "desired_capacity" {}

data "aws_ami" "amazon_linux_2023" {
  most_recent = true
  owners      = ["amazon"]
  filter {
    name   = "name"
    values = ["al2023-ami-2023.*-x86_64"]
  }
}

resource "aws_launch_template" "app" {
  name_prefix   = "${var.environment}-cards-lt-"
  image_id      = data.aws_ami.amazon_linux_2023.id
  instance_type = var.instance_type
  key_name      = var.key_name

  vpc_security_group_ids = var.security_groups

  user_data = base64encode(<<-EOF
              #!/bin/bash
              dnf update -y
              dnf install -y docker
              systemctl enable --now docker
              usermod -aG docker ec2-user
              EOF
  )

  tag_specifications {
    resource_type = "instance"
    tags = {
      Name = "${var.environment}-cards-instance"
      Role = "cards_backend"
      Env  = var.environment
    }
  }
}

resource "aws_autoscaling_group" "asg" {
  name                = "${var.environment}-cards-asg"
  vpc_zone_identifier = var.vpc_zone_subnets
  target_group_arns   = var.target_group_arns

  min_size         = var.min_size
  max_size         = var.max_size
  desired_capacity = var.desired_capacity

  launch_template {
    id      = aws_launch_template.app.id
    version = "$Latest"
  }

  tag {
    key                 = "Role"
    value               = "cards_backend"
    propagate_at_launch = true
  }

  tag {
    key                 = "Name"
    value               = "${var.environment}-cards-instance"
    propagate_at_launch = true
  }
}
