# InStream Tenant Application Server

## 개요
4학년 2학기 SW캡스톤디자인 "실시간 라이브 스트리밍 SaaS" 프로젝트 중, 기업용 어플리케이션 서버 프로젝트입니다. 주요 기능은 다음과 같습니다.

+ InStream 기업 서비스 제공 
  + 영상, 채팅 어플리케이션 생성 및 사용
  + 사용량 내역 조회 및 결제
  + InStream 서비스 관련 문의하기
+ 채팅 보내기

## 서버 실행

프로젝트 루트 경로에 .env 파일을 생성하고 다음과 같이 파일 내용을 작성합니다.
```dotenv
# .env
MARIA_IP=mariadb # Docker Network 기준
MARIA_PORT=3306 # Docker Network 기준
MARIA_DATABASE=your_maria_database
MARIA_USERNAME=your_maria_username
MARIA_PASSWORD=your_maria_password
MARIA_ROOT_PASSWORD=your_maria_root_password
REDIS_IP=redis # Docker Network 기준
REDIS_PORT=6379 # Docker Network 기준
REDIS_PASSWORD=your_redis_password 
```

이후 터미널에서 다음 명령어를 실행합니다.
```shell
# shell
docker build --tag instream-tenant-server .
docker-compose up -d
```

