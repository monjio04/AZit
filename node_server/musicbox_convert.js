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

// 15홀 오르골 설정 (C4~C6, 반음 제외)
const holes = 15;
const holeSpacing = 10;
const noteSpacing = 10;
const yOffset = 20;

// C4~C6 실제 음과 홀 매핑
const midiNotes = [
    60, 62, 64, 65, 67, 69, 71, 72, 74, 76, 77, 79, 81, 83, 84
]; // C4, D4, E4, F4, G4, A4, B4, C5, D5, E5, F5, G5, A5, B5, C6

// 기준선 그리기
for (let i = 0; i < holes; i++) {
    const y = yOffset + i * noteSpacing;
    draw.line(20, y, width - 20, y).stroke({ width: 1, color: "#ccc" });
}

// 모든 트랙 순회
let allEvents = midi.tracks.flat();
let timeAcc = 0;
const maxTime = allEvents.reduce((sum, e) => sum + (e.deltaTime || 0), 0);

for (const event of allEvents) {
    timeAcc += event.deltaTime || 0;
    if (event.type === "noteOn" && event.velocity > 0) {
        const noteIndex = midiNotes.indexOf(event.noteNumber);
        if (noteIndex === -1) continue; // 범위 밖이면 무시
        const x = (timeAcc / maxTime) * (width - 40) + 20;
        const y = yOffset + noteIndex * noteSpacing;
        draw.circle(6).fill("black").attr({ cx: x, cy: y });
    }
}

fs.writeFileSync(outputFile, draw.svg());
console.log(`✅ SVG 생성 완료: ${outputFile}`);
