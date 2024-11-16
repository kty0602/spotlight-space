#!/bin/bash

fuser -k 8080/tcp || true
pkill -f health_check.py || true

# 헬스체크용 python프로그램이 진짜 삭제되었는지 확인
while lsof -i:8080; do
    pkill -f health_check.py || true
    sleep 1
done
