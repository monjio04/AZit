# 1. (수정 전) FROM python:3.10-slim
FROM python:3.9-slim

# 2. 컨테이너 안의 /app 폴더에서 작업합니다.
WORKDIR /app

# 3. requirements.txt 파일을 먼저 복사합니다.
COPY requirements.txt .

# 4. pip로 라이브러리들을 설치합니다.
RUN pip install --no-cache-dir -r requirements.txt

# 5. 나머지 모든 파일들을 복사합니다.
COPY . .

# 6. 8000번 포트를 엽니다.
EXPOSE 8000

# 7. uvicorn으로 서버를 실행합니다.
CMD ["uvicorn", "convert_to_midi_api:app", "--host", "0.0.0.0", "--port", "8000"]