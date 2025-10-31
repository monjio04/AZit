from fastapi import FastAPI, UploadFile, File
from fastapi.responses import FileResponse, JSONResponse
from pathlib import Path
import subprocess, traceback, hashlib, uuid
from basic_pitch.inference import predict_and_save
from basic_pitch import ICASSP_2022_MODEL_PATH
from mido import MidiFile, MidiTrack, Message

app = FastAPI(title="MIDI → 15홀 오르골 SVG API", version="2.0")

# 폴더 설정
BASE_DIR = Path(__file__).parent
UPLOAD_DIR = BASE_DIR / "uploads"
TEMP_DIR = BASE_DIR / "temp"
MIDI_DIR = BASE_DIR / "midi_outputs"
SVG_DIR = BASE_DIR / "svg_outputs"

for folder in [UPLOAD_DIR, TEMP_DIR, MIDI_DIR, SVG_DIR]:
    folder.mkdir(exist_ok=True)

# WAV 전처리
def preprocess_wav(input_path: Path, temp_dir: Path) -> Path:
    processed_path = temp_dir / input_path.name.replace(".wav", "_processed.wav")
    command = [
        "ffmpeg", "-y", "-i", str(input_path),
        "-af", "silenceremove=start_periods=1:start_silence=0.5:start_threshold=-40dB, loudnorm",
        str(processed_path)
    ]
    subprocess.run(command, check=True)
    return processed_path

# MIDI 후처리 (트랙 병합 + velocity 보정)
def clean_midi(input_path: Path, output_path: Path):
    midi = MidiFile(input_path)
    merged = MidiFile(ticks_per_beat=midi.ticks_per_beat)
    track = MidiTrack()
    merged.tracks.append(track)

    for t in midi.tracks:
        for msg in t:
            if msg.type in ("note_on", "note_off"):
                # velocity 0을 note_off로 변환
                if msg.type == "note_on" and msg.velocity == 0:
                    msg = Message("note_off", note=msg.note, time=msg.time)
                track.append(msg)
    merged.save(output_path)

@app.post("/convert-musicbox")
async def convert_musicbox(file: UploadFile = File(...)):
    try:
        # 업로드 파일 저장
        file_hash = hashlib.md5(file.filename.encode()).hexdigest()
        ext = Path(file.filename).suffix
        input_path = UPLOAD_DIR / f"{file_hash}{ext}"
        with open(input_path, "wb") as f:
            f.write(await file.read())

        # WAV 전처리
        processed_audio = preprocess_wav(input_path, TEMP_DIR)

        # MIDI 변환 (Basic Pitch)
        predict_and_save(
            [str(processed_audio)],
            str(MIDI_DIR),
            True, False, False, False,
            ICASSP_2022_MODEL_PATH
        )
        generated_file = MIDI_DIR / f"{processed_audio.stem}_basic_pitch.mid"
        if not generated_file.exists():
            return JSONResponse({"error": "MIDI 변환 실패"}, status_code=500)

        # MIDI 정리
        cleaned_midi = MIDI_DIR / f"{processed_audio.stem}_{uuid.uuid4().hex}_cleaned.mid"
        clean_midi(generated_file, cleaned_midi)

        # SVG 변환 (Node.js)
        svg_file = SVG_DIR / f"{processed_audio.stem}_{uuid.uuid4().hex}.svg"
        subprocess.run(
            ["node", "../node_server/musicbox_convert.js", str(cleaned_midi), str(svg_file)],
            check=True
        )

        return FileResponse(svg_file, filename=svg_file.name)

    except Exception as e:
        traceback.print_exc()
        return JSONResponse({"error": str(e)}, status_code=500)
