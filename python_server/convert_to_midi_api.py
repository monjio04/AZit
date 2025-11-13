from fastapi import FastAPI, UploadFile, File
from fastapi.responses import FileResponse, JSONResponse
from pathlib import Path
import subprocess, traceback, hashlib, uuid, shutil, os
from basic_pitch.inference import predict_and_save
from basic_pitch import ICASSP_2022_MODEL_PATH
from mido import MidiFile, MidiTrack, Message
from pydub import AudioSegment

# ==========================================
# 1. [핵심 수정] FFMPEG 경로 강제 설정 (제일 중요)
# ==========================================
# 시스템 PATH에 /usr/local/bin 강제 주입 (라이브러리들이 찾을 수 있게)
os.environ["PATH"] += os.pathsep + "/usr/bin"
os.environ["PATH"] += os.pathsep + "/usr/local/bin"

# 2. FFMPEG 위치 찾기 & Pydub에 설정
FFMPEG_PATH = shutil.which("ffmpeg") or "/usr/bin/ffmpeg"
AudioSegment.converter = FFMPEG_PATH
AudioSegment.ffmpeg = FFMPEG_PATH
app = FastAPI(title="MIDI → 15홀 오르골 SVG API", version="2.3")

print(f"🚀 FFMPEG 경로 설정 완료: {FFMPEG_PATH}")

# 폴더 설정
BASE_DIR = Path(__file__).parent
UPLOAD_DIR = BASE_DIR / "uploads"
TEMP_DIR = BASE_DIR / "temp"
MIDI_DIR = BASE_DIR / "midi_outputs"
SVG_DIR = BASE_DIR / "svg_outputs"

for folder in [UPLOAD_DIR, TEMP_DIR, MIDI_DIR, SVG_DIR]:
    folder.mkdir(exist_ok=True)

# Node.js 스크립트 경로 설정
NODE_SCRIPT = (BASE_DIR.parent / "node_server" / "musicbox_convert.js").resolve()

# WAV 전처리
def preprocess_wav(input_path: Path, temp_dir: Path) -> Path:
    processed_path = temp_dir / input_path.name.replace(".wav", "_processed.wav")
    command = [
        FFMPEG_PATH,  # ★ 수정됨: 하드코딩 대신 찾은 경로 변수 사용
        "-y", "-i", str(input_path),
        "-af", "silenceremove=start_periods=1:start_silence=0.5:start_threshold=-40dB, loudnorm",
        str(processed_path)
    ]
    # check=True로 설정하면 에러 시 즉시 예외 발생
    subprocess.run(command, check=True)
    return processed_path

# MIDI 후처리 (트랙 병합 + velocity 보정)
def clean_midi(input_path: Path, output_path: Path):
    try:
        midi = MidiFile(input_path)
    except OSError as e:
        if 'MThd not found' in str(e):
            raise ValueError(f"유효한 MIDI 파일이 아닙니다: {input_path.name}")
        else:
            raise e

    merged = MidiFile(ticks_per_beat=midi.ticks_per_beat)
    track = MidiTrack()
    merged.tracks.append(track)

    for t in midi.tracks:
        for msg in t:
            if msg.type in ("note_on", "note_off"):
                if msg.type == "note_on" and msg.velocity == 0:
                    msg = Message("note_off", note=msg.note, time=msg.time)
                track.append(msg)
    merged.save(output_path)

@app.post("/convert-musicbox")
async def convert_musicbox(file: UploadFile = File(...)):
    try:
        file_hash = hashlib.md5(file.filename.encode()).hexdigest()
        ext = Path(file.filename).suffix.lower()

        if ext not in [".wav", ".mid"]:
            return JSONResponse({"error": "지원하지 않는 형식 (wav, mid만 가능)"}, status_code=400)

        input_path = UPLOAD_DIR / f"{file_hash}{ext}"
        with open(input_path, "wb") as f:
            f.write(await file.read())

        base_stem = ""
        midi_to_clean = None

        if ext == ".wav":
            # --- WAV 처리 ---
            print(f"▶ WAV 전처리 시작: {input_path}")
            processed_audio = preprocess_wav(input_path, TEMP_DIR)
            base_stem = processed_audio.stem

            print("▶ Basic Pitch 변환 시작")
            predict_and_save(
                [str(processed_audio)],
                str(MIDI_DIR),
                True, False, False, False,
                ICASSP_2022_MODEL_PATH
            )

            generated_file = MIDI_DIR / f"{processed_audio.stem}_basic_pitch.mid"
            if not generated_file.exists():
                return JSONResponse({"error": "MIDI 변환 실패 (파일 생성 안됨)"}, status_code=500)

            midi_to_clean = generated_file

            # 청소
            if input_path.exists(): input_path.unlink()
            if processed_audio.exists(): processed_audio.unlink()

            # MIDI 정리 및 반환
            cleaned_midi_name = f"{base_stem}_{uuid.uuid4().hex}_cleaned.mid"
            cleaned_midi_path = MIDI_DIR / cleaned_midi_name

            clean_midi(midi_to_clean, cleaned_midi_path)
            if midi_to_clean.exists(): midi_to_clean.unlink()

            return FileResponse(
                cleaned_midi_path,
                filename=cleaned_midi_path.name,
                media_type='audio/midi'
            )

        else: # ext == ".mid"
            # --- MIDI 처리 ---
            base_stem = input_path.stem
            midi_to_clean = input_path

            cleaned_midi_name = f"{base_stem}_{uuid.uuid4().hex}_cleaned.mid"
            cleaned_midi_path = MIDI_DIR / cleaned_midi_name

            clean_midi(midi_to_clean, cleaned_midi_path)
            if midi_to_clean.exists(): midi_to_clean.unlink()

            if not NODE_SCRIPT.exists():
                return JSONResponse({"error": f"Node.js 스크립트 없음: {NODE_SCRIPT}"}, 500)

            svg_file_name = f"{cleaned_midi_path.stem}.svg"
            svg_file_path = SVG_DIR / svg_file_name

            subprocess.run(
                ["node", str(NODE_SCRIPT), str(cleaned_midi_path), str(svg_file_path)],
                check=True
            )

            if not svg_file_path.exists():
                return JSONResponse({"error": "SVG 생성 실패"}, 500)

            return FileResponse(
                svg_file_path,
                filename=svg_file_path.name,
                media_type='image/svg+xml'
            )

    except subprocess.CalledProcessError as cpe:
        traceback.print_exc()
        return JSONResponse({"error": f"외부 프로세스(FFmpeg/Node) 실행 실패: {cpe}"}, status_code=500)

    except ValueError as ve:
        traceback.print_exc()
        return JSONResponse({"error": str(ve)}, status_code=400)

    except Exception as e:
        traceback.print_exc()
        return JSONResponse({"error": f"서버 내부 오류: {str(e)}"}, status_code=500)