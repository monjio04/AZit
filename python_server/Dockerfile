# 1. Python 3.9 slim 이미지 사용
FROM python:3.9-slim-bullseye

# 2. Debian 미러를 한국 서버로 교체 (또는 재시도 로직 포함)
RUN sed -i 's|http://deb.debian.org/debian|http://ftp.kr.debian.org/debian|g' /etc/apt/sources.list \
    && for i in 1 2 3; do apt-get update && break || sleep 5; done \
    && apt-get install -y --no-install-recommends \
        ffmpeg \
        ca-certificates \
        wget \
        git \
        build-essential \
    && rm -rf /var/lib/apt/lists/*

# 3. 작업 디렉토리 생성
WORKDIR /app

# 4. requirements.txt 복사 후 설치
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# 5. 나머지 파일 복사
COPY . .

# 6. 포트 열기
EXPOSE 8000

# 7. uvicorn으로 FastAPI 실행
CMD ["uvicorn", "convert_to_midi_api:app", "--host", "0.0.0.0", "--port", "8000", "--log-level", "debug", "--no-access-log"]
