#!/bin/bash

echo "--------------- 서버 배포 시작 -----------------"

#기본 servername 가져오기
source /home/ubuntu/.env

# S3에서 .env 파일 다운로드
aws s3 cp s3://$SERVER_NAME/.env /home/ubuntu/$SERVER_NAME/.env --region ap-northeast-2

# .env 파일 로드
source /home/ubuntu/$SERVER_NAME/.env

# 임시 헬스 체크 서버 종료
fuser -k 8080/tcp || true
pkill -f health_check.py || true

# 최신 Docker 이미지 Pull
docker pull $ECR_URL:latest

cd /home/ubuntu/$SERVER_NAME

# Docker Compose로 컨테이너 실행
docker compose -f /home/ubuntu/$SERVER_NAME/docker-compose.yml up -d

echo "--------------- 서버 배포 끝 -----------------"
