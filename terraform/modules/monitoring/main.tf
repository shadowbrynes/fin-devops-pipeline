variable "subnet_id" {}
variable "security_groups" { type = list(string) }
variable "key_name" {}
variable "instance_type" {}
variable "environment" {}

data "aws_ami" "amazon_linux_2023" {
  most_recent = true
  owners      = ["amazon"]
  filter {
    name   = "name"
    values = ["al2023-ami-2023.*-x86_64"]
  }
}

resource "aws_instance" "monitoring" {
  ami                         = data.aws_ami.amazon_linux_2023.id
  instance_type               = var.instance_type
  subnet_id                   = var.subnet_id
  vpc_security_group_ids      = var.security_groups
  key_name                    = var.key_name
  associate_public_ip_address = true

  tags = {
    Name = "${var.environment}-monitoring"
    Role = "monitoring"
    Env  = var.environment
  }
}

output "public_ip" { value = aws_instance.monitoring.public_ip }
