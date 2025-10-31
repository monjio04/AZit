@echo off
echo 🌱 Activating virtual environment...
python -m venv venv
call venv\Scripts\activate

echo 📦 Installing dependencies...
pip install -r requirements.txt

echo 🚀 Starting FastAPI server...
uvicorn convert_to_midi_api:app --host 0.0.0.0 --port 8000
