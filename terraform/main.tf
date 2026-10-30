terraform {
  required_version = ">= 1.5.0"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region = var.aws_region
}

# --- VPC & Networking ---
module "vpc" {
  source             = "./modules/vpc"
  cidr_block         = "10.0.0.0/16"
  public_subnets     = ["10.0.1.0/24", "10.0.2.0/24"]
  private_subnets    = ["10.0.10.0/24", "10.0.20.0/24"]
  availability_zones = ["${var.aws_region}a", "${var.aws_region}b"]
  environment        = var.environment
}

# --- Security Groups ---
resource "aws_security_group" "alb_sg" {
  name        = "${var.environment}-alb-sg"
  description = "Allow inbound HTTP traffic to ALB"
  vpc_id      = module.vpc.vpc_id

  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_security_group" "app_sg" {
  name        = "${var.environment}-app-sg"
  description = "Allow traffic from ALB, Monitoring and Jenkins"
  vpc_id      = module.vpc.vpc_id

  ingress {
    description     = "Traffic from ALB"
    from_port       = 8085
    to_port         = 8085
    protocol        = "tcp"
    security_groups = [aws_security_group.alb_sg.id]
  }

  ingress {
    description = "SSH from VPC / Jenkins"
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    description = "Prometheus node exporter"
    from_port   = 9100
    to_port     = 9100
    protocol    = "tcp"
    cidr_blocks = ["10.0.0.0/16"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_security_group" "monitoring_sg" {
  name        = "${var.environment}-monitoring-sg"
  description = "Allow Prometheus, Grafana, and SSH access"
  vpc_id      = module.vpc.vpc_id

  ingress {
    description = "SSH"
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    description = "Grafana UI"
    from_port   = 3000
    to_port     = 3000
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    description = "Prometheus UI"
    from_port   = 9090
    to_port     = 9090
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

# --- Application Load Balancer ---
module "alb" {
  source          = "./modules/alb"
  vpc_id          = module.vpc.vpc_id
  public_subnets  = module.vpc.public_subnet_ids
  security_groups = [aws_security_group.alb_sg.id]
  environment     = var.environment
  app_port        = 8085
  health_check_path = "/swagger-ui/index.html"
}

# --- Auto Scaling Group for Cards App ---
module "asg" {
  source            = "./modules/asg"
  vpc_zone_subnets  = module.vpc.public_subnet_ids # Or private_subnet_ids if NAT deployed
  security_groups   = [aws_security_group.app_sg.id]
  key_name          = var.key_name
  target_group_arns = [module.alb.target_group_arn]
  instance_type     = "t3.micro"
  environment       = var.environment
  min_size          = 1
  max_size          = 3
  desired_capacity  = 1
}

# --- Dedicated Monitoring Instance ---
module "monitoring" {
  source          = "./modules/monitoring"
  subnet_id       = module.vpc.public_subnet_ids[0]
  security_groups = [aws_security_group.monitoring_sg.id]
  key_name        = var.key_name
  instance_type   = "t3.small"
  environment     = var.environment
}
