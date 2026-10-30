variable "aws_region" {
  default = "us-east-1"
}

variable "environment" {
  default = "production"
}

variable "key_name" {
  description = "EC2 Key Pair name"
  default     = "umoh2"
}
