import fs from "fs";
import { parseMidi } from "midi-file";
import { createSVGWindow } from "svgdom";
import { SVG, registerWindow } from "@svgdotjs/svg.js";

// 입력/출력 파일
const inputFile = process.argv[2];
const outputFile = process.argv[3];

if (!inputFile || !outputFile) {
    console.error("Usage: node musicbox_convert.js input.mid output.svg");
    process.exit(1);
}

// MIDI 파일 읽기
const input = fs.readFileSync(inputFile);
const midi = parseMidi(input);

// SVG 초기화
const window = createSVGWindow();
const document = window.document;
registerWindow(window, document);

const width = 800;
const height = 200;

const draw = SVG(document.documentElement);
draw.attr({ width, height, viewBox: `0 0 ${width} ${height}` });

// ==============================
//  오르골 설정
// ==============================

// C4~C6 자연음 15홀 오르골 음계
const ORGEL_NOTES = [
    60, 62, 64, 65, 67, 69, 71,
    72, 74, 76, 77, 79, 81, 83, 84
];

// 음계를 스냅하는 함수
function snapToOrgel(noteNumber) {
    let closest = ORGEL_NOTES[0];
    let minDist = Math.abs(noteNumber - closest);

    for (const allowed of ORGEL_NOTES) {
        const d = Math.abs(noteNumber - allowed);
        if (d < minDist) {
            minDist = d;
            closest = allowed;
        }
    }
    return closest;
}

const holes = 15;
const holeSpacing = 10;
const noteSpacing = 10;
const yOffset = 20;

// 굵게 칠할 기준선 index
const boldLines = [4, 6, 8, 10, 12];

// 줄 그리기
for (let i = 0; i < holes; i++) {
    const y = yOffset + i * noteSpacing;
    draw.line(20, y, width - 20, y)
        .stroke({ width: boldLines.includes(i) ? 2 : 1, color: "#ccc" });
}

// ==============================
//   MIDI 이벤트 시간 계산
// ==============================

let events = midi.tracks.flat();
let currentTime = 0;
let maxTime = events.reduce((acc, e) => acc + (e.deltaTime || 0), 0);

for (const event of events) {
    currentTime += event.deltaTime || 0;

    // NOTE ON (velocity > 0)
    if (event.type === "noteOn" && event.velocity > 0) {
        // 🔥 MIDI 노트를 오르골 허용 음으로 스냅
        let snappedNote = snapToOrgel(event.noteNumber);

        // 스냅된 노트가 몇 번째 라인인지 찾기
        const noteIndex = ORGEL_NOTES.indexOf(snappedNote);
        if (noteIndex === -1) continue; // 방어코드

        // 시간 → X좌표
        const x = (currentTime / maxTime) * (width - 40) + 20;
        // 음높이 → Y좌표
        const y = yOffset + noteIndex * noteSpacing;

        // 점 찍기
        draw.circle(6).fill("black").attr({ cx: x, cy: y });
    }
}

// SVG 저장
fs.writeFileSync(outputFile, draw.svg());
console.log(`✅ SVG 생성 완료: ${outputFile}`);
