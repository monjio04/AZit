from fastapi import FastAPI, UploadFile, File
from fastapi.responses import FileResponse, JSONResponse
from pathlib import Path
import subprocess, traceback, hashlib, uuid
from basic_pitch.inference import predict_and_save
from basic_pitch import ICASSP_2022_MODEL_PATH
from mido import MidiFile, MidiTrack, Message

app = FastAPI(title="MIDI → 15홀 오르골 SVG API", version="2.2") # 버전 수정

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
        "ffmpeg", "-y", "-i", str(input_path),
        "-af", "silenceremove=start_periods=1:start_silence=0.5:start_threshold=-40dB, loudnorm",
        str(processed_path)
    ]
    subprocess.run(command, check=True)
    return processed_path

# --- (★ 여기가 수정되었습니다) ---

# MIDI 후처리 (트랙 병합 + velocity 보정)
def clean_midi(input_path: Path, output_path: Path):
    try:
        # 1. MIDI 파일을 열 때 OSError를 잡도록 수정
        midi = MidiFile(input_path)
    except OSError as e:
        # 2. "MThd not found" (유효하지 않은 MIDI) 오류인 경우
        if 'MThd not found' in str(e):
            # 3. 500 오류 대신 400 오류를 유발할 ValueError 발생
            raise ValueError(f"유효한 MIDI 파일이 아닙니다: {input_path.name}")
        else:
            # 다른 종류의 OS 오류라면 그대로 발생
            raise e

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


# --- (★ 여기가 수정되었습니다) ---
@app.post("/convert-musicbox")
async def convert_musicbox(file: UploadFile = File(...)):
    try:
        # 업로드 파일 기본 정보
        file_hash = hashlib.md5(file.filename.encode()).hexdigest()
        ext = Path(file.filename).suffix.lower()

        # 1. 파일 확장자 검사
        if ext not in [".wav", ".mid"]:
            return JSONResponse({"error": "지원하지 않는 형식 (wav, mid만 가능)"}, status_code=400)

        # 업로드 파일 저장
        input_path = UPLOAD_DIR / f"{file_hash}{ext}"
        with open(input_path, "wb") as f:
            f.write(await file.read())

        base_stem = ""
        midi_to_clean = None

        # 2. 확장자에 따라 처리 분기
        if ext == ".wav":
            # --- WAV 파일 처리 경로 (WAV -> MIDI) ---
            processed_audio = preprocess_wav(input_path, TEMP_DIR)
            base_stem = processed_audio.stem

            predict_and_save(
                [str(processed_audio)],
                str(MIDI_DIR),
                True, False, False, False,
                ICASSP_2022_MODEL_PATH
            )

            generated_file = MIDI_DIR / f"{processed_audio.stem}_basic_pitch.mid"
            if not generated_file.exists():
                return JSONResponse({"error": "MIDI 변환 실패"}, status_code=500)

            midi_to_clean = generated_file

            if input_path.exists():
                input_path.unlink()
            if processed_audio.exists():
                processed_audio.unlink()

            # 3. MIDI 정리 (공통 로직)
            cleaned_midi_name = f"{base_stem}_{uuid.uuid4().hex}_cleaned.mid"
            cleaned_midi_path = MIDI_DIR / cleaned_midi_name

            clean_midi(midi_to_clean, cleaned_midi_path)

            if midi_to_clean.exists():
                midi_to_clean.unlink()

            # ★★★ 수정된 부분 ★★★
            # 5. 결과 반환 (WAV -> MIDI)
            # SVG 변환을 하지 않고, 정리된 MIDI 파일을 즉시 반환
            return FileResponse(
                cleaned_midi_path,
                filename=cleaned_midi_path.name,
                media_type='audio/midi' # 미디어 타입 지정
            )

            # --- (WAV 처리는 여기서 끝) ---

        else: # ext == ".mid"
            # --- MID 파일 처리 경로 (MID -> SVG) ---
            base_stem = input_path.stem
            midi_to_clean = input_path # 업로드한 원본 파일 자체를 정리

            # 3. MIDI 정리 (공통 로직)
            cleaned_midi_name = f"{base_stem}_{uuid.uuid4().hex}_cleaned.mid"
            cleaned_midi_path = MIDI_DIR / cleaned_midi_name

            # ★ 여기서 clean_midi 함수가 ValueError를 발생시킬 수 있음
            clean_midi(midi_to_clean, cleaned_midi_path)

            if midi_to_clean.exists():
                midi_to_clean.unlink()

            # 4. SVG 변환 (Node.js)
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

            # 5. 결과 반환 (MID -> SVG)
            # ★★★ 수정된 부분 ★★★
            return FileResponse(
                svg_file_path,
                filename=svg_file_path.name,
                media_type='image/svg+xml' # 미디어 타입 지정
            )

    # --- 예외 처리 블록 (동일) ---

    except subprocess.CalledProcessError as cpe:
        traceback.print_exc()
        return JSONResponse({"error": f"외부 프로세스 실패: {cpe}"}, status_code=500)

    except ValueError as ve:
        traceback.print_exc()
        return JSONResponse({"error": str(ve)}, status_code=400)

    except Exception as e:
        traceback.print_exc()
        return JSONResponse({"error": str(e)}, status_code=500)