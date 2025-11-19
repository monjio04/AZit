import fs from "fs";
import { parseMidi } from "midi-file";
import { createSVGWindow } from "svgdom";
import { SVG, registerWindow } from "@svgdotjs/svg.js";

const inputFile = process.argv[2];
const outputFile = process.argv[3];

if (!inputFile || !outputFile) {
    console.error("Usage: node musicbox_convert.js input.mid output.svg");
    process.exit(1);
}

// -----------------------------------------------
// MIDI 읽기
// -----------------------------------------------
const input = fs.readFileSync(inputFile);
const midi = parseMidi(input);

// -----------------------------------------------
// SVG 초기화
// -----------------------------------------------
const window = createSVGWindow();
const document = window.document;
registerWindow(window, document);

const width = 800;
const height = 200;

const draw = SVG(document.documentElement);
draw.attr({ width, height, viewBox: `0 0 ${width} ${height}` });

// -----------------------------------------------
// 15홀 오르골 (C4~C6) 설정
// -----------------------------------------------
const midiNotes = [
    60, 62, 64, 65, 67, 69, 71,
    72, 74, 76, 77, 79, 81, 83, 84
];

const holeCount = midiNotes.length;
const noteSpacing = 10;
const yOffset = 20;

// 굵은 기준선
const boldLines = [4, 6, 8, 10, 12];

for (let i = 0; i < holeCount; i++) {
    const y = yOffset + i * noteSpacing;
    draw.line(20, y, width - 20, y)
        .stroke({ width: boldLines.includes(i) ? 2 : 1, color: "#ccc" });
}

// -----------------------------------------------
// MIDI → note 리스트 추출
// -----------------------------------------------
let notes = [];
let timeAcc = 0;

midi.tracks.forEach(track => {
    track.forEach(event => {
        timeAcc += event.deltaTime || 0;

        if (event.type === "noteOn" && event.velocity > 0) {
            if (midiNotes.includes(event.noteNumber)) {
                notes.push({
                    time: timeAcc,       // 실제 MIDI 시간
                    note: event.noteNumber
                });
            }
        }
    });
});

// 시간순 정렬
notes.sort((a, b) => a.time - b.time);

// -----------------------------------------------
// ⭐ SVG 전용 가짜 시간(punchTime) 생성 로직
//    → 음악 시간(time)은 건드리지 않음
// -----------------------------------------------

// 최소 간격 (가까운 것만 밀기)
const minGap = 120; // ms (원하면 80~150 조정 가능)

// MIDI 시간 → SVG 기본 시간 스케일
// 너무 좁지 않게 살짝 축소
const baseScale = 0.8;

let lastPunchTime = -Infinity;

for (let n of notes) {
    let desired = n.time * baseScale;

    // 너무 가까우면 최소 간격으로 밀어줌
    if (desired - lastPunchTime < minGap) {
        desired = lastPunchTime + minGap;
    }

    n.punchTime = desired;
    lastPunchTime = desired;
}

// -----------------------------------------------
// SVG x축은 punchTime 기준으로 배치
// -----------------------------------------------
const maxPunchTime = Math.max(...notes.map(n => n.punchTime));

for (const n of notes) {
    const noteIndex = midiNotes.indexOf(n.note);

    const x = (n.punchTime / maxPunchTime) * (width - 40) + 20;
    const y = yOffset + noteIndex * noteSpacing;

    draw.circle(6)
        .fill("black")
        .attr({ cx: x, cy: y });
}

// -----------------------------------------------
// SVG 출력
// -----------------------------------------------
fs.writeFileSync(outputFile, draw.svg());
console.log(`✅ SVG 생성 완료: ${outputFile}`);
