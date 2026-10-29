output "ecr_repository_url" {
  value = aws_ecr_repository.cards_repo.repository_url
}

output "sns_topic_arn" {
  value = aws_sns_topic.alerts.arn
}

output "asg_name" {
  value = aws_autoscaling_group.cards_asg.name
}

output "jenkins_url" {
  description = "Jenkins Dashboard URL"
  value       = "http://${aws_instance.ci_cd_server.public_ip}:8080"
}

output "sonarqube_url" {
  description = "SonarQube Quality Gate Dashboard URL"
  value       = "http://${aws_instance.ci_cd_server.public_ip}:9000"
}
