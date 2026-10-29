# Imported SNS Topic for Monitoring Alerts
resource "aws_sns_topic" "alerts" {
  name = "cards-service-alerts"
}

# 1. Look up Default VPC and Subnets if none provided
data "aws_vpc" "default" {
  default = true
}

data "aws_subnets" "default" {
  filter {
    name   = "vpc-id"
    values = [data.aws_vpc.default.id]
  }
}

# 2. Look up latest Amazon Linux 2023 AMI
data "aws_ami" "amazon_linux_2023" {
  most_recent = true
  owners      = ["amazon"]

  filter {
    name   = "name"
    values = ["al2023-ami-2023.*-x86_64"]
  }
}

# 3. AWS ECR Repository for Cards Microservice
resource "aws_ecr_repository" "cards_repo" {
  name                 = "cards-service"
  image_tag_mutability = "MUTABLE"

  image_scanning_configuration {
    scan_on_push = true
  }
}

# 4. IAM Role & Instance Profile for EC2 (Allows SSM and ECR Pull)
resource "aws_iam_role" "ec2_app_role" {
  name = "cards-service-ec2-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "ec2.amazonaws.com"
        }
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "ssm_policy" {
  role       = aws_iam_role.ec2_app_role.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
}

resource "aws_iam_role_policy_attachment" "ecr_readonly" {
  role       = aws_iam_role.ec2_app_role.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonEC2ContainerRegistryReadOnly"
}

resource "aws_iam_instance_profile" "ec2_profile" {
  name = "cards-service-ec2-profile"
  role = aws_iam_role.ec2_app_role.name
}

# 5. Security Group
resource "aws_security_group" "cards_service_sg" {
  name        = "cards-service-sg"
  description = "Security group for Cards Microservice"
  vpc_id      = data.aws_vpc.default.id

  ingress {
    description = "Allow HTTP to Cards Service"
    from_port   = 8085
    to_port     = 8085
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    description = "Allow SSH"
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "cards-service-sg"
  }
}

# 6. Launch Template
resource "aws_launch_template" "cards_lt" {
  name_prefix   = "cards-service-lt-"
  image_id      = data.aws_ami.amazon_linux_2023.id
  instance_type = "t3.medium"
  key_name      = var.key_name != "" ? var.key_name : null

  vpc_security_group_ids = [aws_security_group.cards_service_sg.id]

  iam_instance_profile {
    name = aws_iam_instance_profile.ec2_profile.name
  }

  tag_specifications {
    resource_type = "instance"
    tags = {
      Name = "cards-service-node"
      Role = "cards-backend"
    }
  }

  user_data = base64encode(<<-EOF
              #!/bin/bash
              dnf update -y
              dnf install -y docker python3-pip
              systemctl enable --now docker
              usermod -aG docker ec2-user
              pip3 install botocore boto3
              EOF
  )
}

# 7. Auto Scaling Group
resource "aws_autoscaling_group" "cards_asg" {
  name_prefix         = "cards-asg-"
  desired_capacity    = 1
  max_size            = 2
  min_size            = 1
  vpc_zone_identifier = data.aws_subnets.default.ids

  launch_template {
    id      = aws_launch_template.cards_lt.id
    version = "$Latest"
  }

  tag {
    key                 = "Role"
    value               = "cards-backend"
    propagate_at_launch = true
  }

  tag {
    key                 = "Name"
    value               = "cards-service-node"
    propagate_at_launch = true
  }
}

# 8. CloudWatch High CPU Alarm -> SNS Topic
resource "aws_cloudwatch_metric_alarm" "high_cpu" {
  alarm_name          = "cards-service-high-cpu"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 2
  metric_name         = "CPUUtilization"
  namespace           = "AWS/EC2"
  period              = 120
  statistic           = "Average"
  threshold           = 80
  alarm_description   = "Triggers when CPU exceeds 80% on Cards nodes"
  alarm_actions       = [aws_sns_topic.alerts.arn]

  dimensions = {
    AutoScalingGroupName = aws_autoscaling_group.cards_asg.name
  }
}
