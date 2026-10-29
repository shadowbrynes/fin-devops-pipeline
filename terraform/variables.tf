variable "aws_region" {
  type    = string
  default = "us-east-1"
}

variable "vpc_id" {
  type        = string
  description = "VPC ID where ASG will reside"
  default     = "" # Left blank to auto-discover default VPC if not provided
}

variable "subnet_ids" {
  type        = list(string)
  description = "Subnet IDs for the ASG"
  default     = []
}

variable "key_name" {
  type        = string
  description = "EC2 Key Pair name for SSH"
  default     = ""
}
