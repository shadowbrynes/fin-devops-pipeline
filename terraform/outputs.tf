output "ecr_repository_url" {
  value = aws_ecr_repository.cards_repo.repository_url
}

output "sns_topic_arn" {
  value = aws_sns_topic.alerts.arn
}

output "asg_name" {
  value = aws_autoscaling_group.cards_asg.name
}
