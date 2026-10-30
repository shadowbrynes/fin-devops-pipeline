output "alb_dns_name" {
  value = module.alb.alb_dns_name
}

output "monitoring_public_ip" {
  value = module.monitoring.public_ip
}
