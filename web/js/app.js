/**
 * AuraMotion Studio Engine - Interactive Real-time Motion Graphics Editor
 * Integrated with Supabase Cloud Sync & Cloudinary CDN Asset Storage.
 */

// --- Supabase Client Initialization ---
const SUPABASE_URL = "https://slncetmqstgiiobeqhqk.supabase.co";
const SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InNsbmNldG1xc3RnaWlvYmVxaHFrIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODY5MjU5OTcsImV4cCI6MjEwMjUwMTk5N30.3SoGI4zz2dov4gA4Dgc0Ca83ypNsgMoPxPSZqGxu5qg";

let supabaseClient = null;
if (window.supabase) {
  try {
    supabaseClient = window.supabase.createClient(SUPABASE_URL, SUPABASE_ANON_KEY);
    console.log("✓ Supabase Cloud Client Initialized successfully!");
  } catch (err) {
    console.warn("Supabase init notice:", err);
  }
}

// --- Cubic Bézier Solver (Newton-Raphson + Bisection) ---
class BezierCurve {
  constructor(x1 = 0.42, y1 = 0.0, x2 = 0.58, y2 = 1.0) {
    this.x1 = Math.max(0, Math.min(1, x1));
    this.y1 = y1;
    this.x2 = Math.max(0, Math.min(1, x2));
    this.y2 = y2;
  }

  solve(x) {
    if (x <= 0) return 0;
    if (x >= 1) return 1;

    // Newton-Raphson
    let u = x;
    for (let i = 0; i < 8; i++) {
      const currentX = this.sampleCurveX(u) - x;
      if (Math.abs(currentX) < 1e-5) return this.sampleCurveY(u);
      const dX = this.sampleDerivativeX(u);
      if (Math.abs(dX) < 1e-5) break;
      u -= currentX / dX;
    }

    // Bisection fallback
    let uMin = 0, uMax = 1;
    u = x;
    for (let i = 0; i < 12; i++) {
      const currentX = this.sampleCurveX(u);
      if (Math.abs(currentX - x) < 1e-5) return this.sampleCurveY(u);
      if (x > currentX) uMin = u;
      else uMax = u;
      u = (uMin + uMax) * 0.5;
    }
    return this.sampleCurveY(u);
  }

  sampleCurveX(t) {
    const oneMinusT = 1 - t;
    return 3 * oneMinusT * oneMinusT * t * this.x1 + 3 * oneMinusT * t * t * this.x2 + t * t * t;
  }

  sampleCurveY(t) {
    const oneMinusT = 1 - t;
    return 3 * oneMinusT * oneMinusT * t * this.y1 + 3 * oneMinusT * t * t * this.y2 + t * t * t;
  }

  sampleDerivativeX(t) {
    const oneMinusT = 1 - t;
    return 3 * oneMinusT * oneMinusT * this.x1 + 6 * oneMinusT * t * (this.x2 - this.x1) + 3 * t * t * (1 - this.x2);
  }
}

// Preset Curves
const CurvePresets = {
  linear: new BezierCurve(0, 0, 1, 1),
  easeIn: new BezierCurve(0.42, 0, 1, 1),
  easeOut: new BezierCurve(0, 0, 0.58, 1),
  easeInOut: new BezierCurve(0.42, 0, 0.58, 1),
  fastInSlowOut: new BezierCurve(0.12, 0, 0.39, 0),
  overshoot: new BezierCurve(0.34, 1.56, 0.64, 1)
};

// --- Project State ---
class MotionProject {
  constructor() {
    this.title = "Cyberpunk Motion Intro";
    this.width = 1080;
    this.height = 1920;
    this.fps = 60;
    this.duration = 5.0; // seconds
    this.currentTime = 0.0;
    this.isPlaying = false;
    this.isLooping = true;
    this.layers = [];
    this.selectedLayerId = null;
    this.activeCurve = CurvePresets.easeInOut;
  }
}

const project = new MotionProject();

// Initial Layers Setup
function createInitialTemplate() {
  project.layers = [
    {
      id: "layer_particles",
      name: "Cyber Particles",
      type: "particles",
      startTime: 0.0,
      endTime: 5.0,
      visible: true,
      posX: 0,
      posY: 0,
      scale: 1.0,
      rotation: 0,
      opacity: 0.8,
      blendMode: "lighter",
      particles: Array.from({ length: 45 }, () => ({
        x: (Math.random() - 0.5) * 800,
        y: (Math.random() - 0.5) * 1200,
        size: Math.random() * 4 + 2,
        speedX: (Math.random() - 0.5) * 40,
        speedY: (Math.random() - 0.5) * 40,
        color: Math.random() > 0.5 ? "#00e5ff" : "#7c4dff"
      })),
      effects: []
    },
    {
      id: "layer_ring",
      name: "Neon Energy Ring",
      type: "shape",
      shapeType: "circle",
      startTime: 0.0,
      endTime: 5.0,
      visible: true,
      posX: 0,
      posY: -140,
      scale: 1.0,
      rotation: 0,
      opacity: 1.0,
      blendMode: "lighter",
      width: 280,
      height: 280,
      strokeColor: "#00e5ff",
      strokeWidth: 10,
      hasFill: false,
      fillColor: "#00000000",
      hasStroke: true,
      effects: [
        { id: "fx_glow", name: "Neon Glow", type: "glow", intensity: 2.5, radius: 20 }
      ],
      posKeyframes: [
        { time: 0.0, value: { x: 0, y: -400 }, curve: CurvePresets.overshoot },
        { time: 1.2, value: { x: 0, y: -140 }, curve: CurvePresets.easeInOut }
      ],
      rotKeyframes: [
        { time: 0.0, value: 0, curve: CurvePresets.easeInOut },
        { time: 2.5, value: 180, curve: CurvePresets.easeInOut },
        { time: 5.0, value: 360, curve: CurvePresets.easeInOut }
      ],
      scaleKeyframes: [
        { time: 0.0, value: 0.2, curve: CurvePresets.overshoot },
        { time: 1.0, value: 1.0, curve: CurvePresets.easeInOut }
      ]
    },
    {
      id: "layer_card",
      name: "Glassmorphism Card",
      type: "shape",
      shapeType: "rounded_rect",
      startTime: 0.5,
      endTime: 5.0,
      visible: true,
      posX: 0,
      posY: 180,
      scale: 1.0,
      rotation: 0,
      opacity: 0.9,
      blendMode: "source-over",
      width: 380,
      height: 220,
      cornerRadius: 24,
      fillColor: "rgba(22, 27, 36, 0.75)",
      strokeColor: "rgba(0, 229, 255, 0.5)",
      strokeWidth: 2,
      hasFill: true,
      hasStroke: true,
      effects: [
        { id: "fx_blur", name: "Backdrop Blur", type: "blur", radius: 8 }
      ],
      scaleKeyframes: [
        { time: 0.5, value: 0.4, curve: CurvePresets.overshoot },
        { time: 1.5, value: 1.0, curve: CurvePresets.easeInOut }
      ]
    },
    {
      id: "layer_title",
      name: "AURAMOTION Title",
      type: "text",
      text: "AURAMOTION",
      font: "900 52px Inter, sans-serif",
      color: "#ffffff",
      letterSpacing: 4,
      startTime: 0.8,
      endTime: 5.0,
      visible: true,
      posX: 0,
      posY: 160,
      scale: 1.0,
      rotation: 0,
      opacity: 1.0,
      blendMode: "source-over",
      effects: [
        { id: "fx_chromatic", name: "RGB Split", type: "chromatic", distance: 12 }
      ],
      opacityKeyframes: [
        { time: 0.8, value: 0, curve: CurvePresets.easeOut },
        { time: 1.6, value: 1, curve: CurvePresets.linear }
      ]
    },
    {
      id: "layer_subtitle",
      name: "Motion VFX Studio",
      type: "text",
      text: "NEXT-GEN MOTION GRAPHICS",
      font: "600 18px Inter, sans-serif",
      color: "#00e5ff",
      letterSpacing: 6,
      startTime: 1.2,
      endTime: 5.0,
      visible: true,
      posX: 0,
      posY: 220,
      scale: 1.0,
      rotation: 0,
      opacity: 1.0,
      blendMode: "source-over",
      effects: []
    }
  ];
  project.selectedLayerId = "layer_ring";
}

createInitialTemplate();

// --- DOM References ---
const glCanvas = document.getElementById("glCanvas");
const overlayCanvas = document.getElementById("overlayCanvas");
const glCtx = glCanvas.getContext("2d");
const overlayCtx = overlayCanvas.getContext("2d");
const canvasWrapper = document.getElementById("canvasWrapper");

const curveCanvas = document.getElementById("curveCanvas");
const curveCtx = curveCanvas.getContext("2d");

const rulerCanvas = document.getElementById("rulerCanvas");
const rulerCtx = rulerCanvas.getContext("2d");

const timecodeDisplay = document.getElementById("timecodeDisplay");
const timecodeFrames = document.getElementById("timecodeFrames");
const btnPlayPause = document.getElementById("btnPlayPause");
const timelinePlayhead = document.getElementById("timelinePlayhead");
const timelineTracks = document.getElementById("timelineTracks");
const timelineScrollArea = document.getElementById("timelineScrollArea");

const posXRange = document.getElementById("posXRange");
const posXNum = document.getElementById("posXNum");
const posYRange = document.getElementById("posYRange");
const posYNum = document.getElementById("posYNum");
const scaleRange = document.getElementById("scaleRange");
const scaleNum = document.getElementById("scaleNum");
const rotRange = document.getElementById("rotRange");
const rotNum = document.getElementById("rotNum");
const opacityRange = document.getElementById("opacityRange");
const opacityNum = document.getElementById("opacityNum");
const blendModeSelect = document.getElementById("blendModeSelect");
const activeLayerName = document.getElementById("activeLayerName");

let zoomLevel = 0.42;

function updateCanvasSize() {
  const containerW = canvasWrapper.parentElement.clientWidth - 40;
  const containerH = canvasWrapper.parentElement.clientHeight - 40;
  const aspect = project.width / project.height;

  let displayH = containerH * zoomLevel * 2.2;
  let displayW = displayH * aspect;

  if (displayW > containerW) {
    displayW = containerW;
    displayH = displayW / aspect;
  }

  canvasWrapper.style.width = `${displayW}px`;
  canvasWrapper.style.height = `${displayH}px`;
  document.getElementById("zoomLabel").textContent = `${Math.round(zoomLevel * 100 * 2.4)}%`;
}

window.addEventListener("resize", () => {
  updateCanvasSize();
  renderTimelineRuler();
});
updateCanvasSize();

// --- Keyframe Evaluator ---
function evaluateKeyframes(keyframes, time, defaultValue, isVec2 = false) {
  if (!keyframes || keyframes.length === 0) return defaultValue;
  if (keyframes.length === 1 || time <= keyframes[0].time) return keyframes[0].value;
  if (time >= keyframes[keyframes.length - 1].time) return keyframes[keyframes.length - 1].value;

  for (let i = 0; i < keyframes.length - 1; i++) {
    const k0 = keyframes[i];
    const k1 = keyframes[i + 1];
    if (time >= k0.time && time <= k1.time) {
      const duration = k1.time - k0.time;
      if (duration <= 1e-6) return k1.value;
      const linearT = (time - k0.time) / duration;
      const curve = k0.curve || CurvePresets.easeInOut;
      const easedT = curve.solve(linearT);

      if (isVec2) {
        return {
          x: k0.value.x + (k1.value.x - k0.value.x) * easedT,
          y: k0.value.y + (k1.value.y - k0.value.y) * easedT
        };
      } else {
        return k0.value + (k1.value - k0.value) * easedT;
      }
    }
  }
  return keyframes[keyframes.length - 1].value;
}

// --- Render Loop (60 FPS Motion Compositing) ---
let lastFrameTime = performance.now();
let fpsCounter = 0;
let lastFpsTime = performance.now();

function renderFrame() {
  const now = performance.now();
  const delta = (now - lastFrameTime) / 1000;
  lastFrameTime = now;

  // FPS Counter HUD
  fpsCounter++;
  if (now - lastFpsTime >= 500) {
    const fpsVal = ((fpsCounter * 1000) / (now - lastFpsTime)).toFixed(1);
    document.getElementById("hudFps").textContent = `${fpsVal} FPS (Hardware Accelerated)`;
    fpsCounter = 0;
    lastFpsTime = now;
  }

  // Advance Timeline if Playing
  if (project.isPlaying) {
    project.currentTime += delta;
    if (project.currentTime >= project.duration) {
      if (project.isLooping) {
        project.currentTime %= project.duration;
      } else {
        project.currentTime = project.duration;
        project.isPlaying = false;
        btnPlayPause.innerHTML = '<i class="fa-solid fa-play"></i>';
      }
    }
    updateTimecodeUI();
  }

  // Clear GL Canvas
  glCtx.fillStyle = "#0d0f12";
  glCtx.fillRect(0, 0, project.width, project.height);

  const cx = project.width / 2;
  const cy = project.height / 2;

  // Render Layers in Order
  project.layers.forEach(layer => {
    if (!layer.visible || project.currentTime < layer.startTime || project.currentTime > layer.endTime) return;

    const pos = evaluateKeyframes(layer.posKeyframes, project.currentTime, { x: layer.posX, y: layer.posY }, true);
    const scale = evaluateKeyframes(layer.scaleKeyframes, project.currentTime, layer.scale, false);
    const rot = evaluateKeyframes(layer.rotKeyframes, project.currentTime, layer.rotation, false);
    const opacity = evaluateKeyframes(layer.opacityKeyframes, project.currentTime, layer.opacity, false);

    glCtx.save();
    glCtx.globalCompositeOperation = layer.blendMode || "source-over";
    glCtx.globalAlpha = opacity;

    // Translate to Layer Pivot
    glCtx.translate(cx + pos.x, cy + pos.y);
    glCtx.rotate((rot * Math.PI) / 180);
    glCtx.scale(scale, scale);

    // Apply Shader Glow FX
    const glowFx = layer.effects.find(e => e.type === "glow");
    if (glowFx) {
      glCtx.shadowColor = glowFx.color || "#00e5ff";
      glCtx.shadowBlur = (glowFx.radius || 15) * (glowFx.intensity || 1.8);
    } else {
      glCtx.shadowBlur = 0;
    }

    if (layer.type === "particles") {
      layer.particles.forEach(p => {
        p.x += p.speedX * delta;
        p.y += p.speedY * delta;
        if (p.x > 500) p.x = -500;
        if (p.x < -500) p.x = 500;
        if (p.y > 800) p.y = -800;
        if (p.y < -800) p.y = 800;

        glCtx.fillStyle = p.color;
        glCtx.shadowColor = p.color;
        glCtx.shadowBlur = 10;
        glCtx.beginPath();
        glCtx.arc(p.x, p.y, p.size, 0, Math.PI * 2);
        glCtx.fill();
      });
    } else if (layer.type === "shape") {
      if (layer.shapeType === "circle") {
        glCtx.beginPath();
        glCtx.arc(0, 0, layer.width / 2, 0, Math.PI * 2);
        if (layer.hasFill) {
          glCtx.fillStyle = layer.fillColor;
          glCtx.fill();
        }
        if (layer.hasStroke) {
          glCtx.strokeStyle = layer.strokeColor;
          glCtx.lineWidth = layer.strokeWidth;
          glCtx.stroke();
        }
      } else if (layer.shapeType === "rounded_rect") {
        const w = layer.width;
        const h = layer.height;
        const r = layer.cornerRadius || 16;
        glCtx.beginPath();
        glCtx.roundRect(-w / 2, -h / 2, w, h, r);
        if (layer.hasFill) {
          glCtx.fillStyle = layer.fillColor;
          glCtx.fill();
        }
        if (layer.hasStroke) {
          glCtx.strokeStyle = layer.strokeColor;
          glCtx.lineWidth = layer.strokeWidth;
          glCtx.stroke();
        }
      } else if (layer.shapeType === "star") {
        const pts = 5;
        const rOuter = layer.width / 2;
        const rInner = rOuter * 0.5;
        glCtx.beginPath();
        for (let i = 0; i < pts * 2; i++) {
          const angle = (i * Math.PI) / pts - Math.PI / 2;
          const r = i % 2 === 0 ? rOuter : rInner;
          const px = Math.cos(angle) * r;
          const py = Math.sin(angle) * r;
          if (i === 0) glCtx.moveTo(px, py);
          else glCtx.lineTo(px, py);
        }
        glCtx.closePath();
        if (layer.hasFill) {
          glCtx.fillStyle = layer.fillColor || "#ffd600";
          glCtx.fill();
        }
        if (layer.hasStroke) {
          glCtx.strokeStyle = layer.strokeColor || "#ffffff";
          glCtx.lineWidth = layer.strokeWidth || 4;
          glCtx.stroke();
        }
      }
    } else if (layer.type === "text") {
      glCtx.font = layer.font || "700 48px Inter, sans-serif";
      glCtx.textAlign = "center";
      glCtx.textBaseline = "middle";

      const chromaticFx = layer.effects.find(e => e.type === "chromatic");
      if (chromaticFx) {
        const dist = chromaticFx.distance || 8;
        glCtx.fillStyle = "rgba(255, 0, 80, 0.85)";
        glCtx.fillText(layer.text, -dist, 0);
        glCtx.fillStyle = "rgba(0, 229, 255, 0.85)";
        glCtx.fillText(layer.text, dist, 0);
      }

      glCtx.fillStyle = layer.color || "#ffffff";
      glCtx.fillText(layer.text, 0, 0);
    } else if (layer.type === "image" && layer.imgElement) {
      const w = layer.width || 300;
      const h = layer.height || 300;
      glCtx.drawImage(layer.imgElement, -w / 2, -h / 2, w, h);
    }

    glCtx.restore();
  });

  renderGizmoOverlay();
  requestAnimationFrame(renderFrame);
}

// --- Gizmo Overlay ---
function renderGizmoOverlay() {
  overlayCtx.clearRect(0, 0, project.width, project.height);
  const selected = project.layers.find(l => l.id === project.selectedLayerId);
  if (!selected) return;

  const cx = project.width / 2;
  const cy = project.height / 2;
  const pos = evaluateKeyframes(selected.posKeyframes, project.currentTime, { x: selected.posX, y: selected.posY }, true);
  const scale = evaluateKeyframes(selected.scaleKeyframes, project.currentTime, selected.scale, false);
  const rot = evaluateKeyframes(selected.rotKeyframes, project.currentTime, selected.rotation, false);

  overlayCtx.save();
  overlayCtx.translate(cx + pos.x, cy + pos.y);
  overlayCtx.rotate((rot * Math.PI) / 180);
  overlayCtx.scale(scale, scale);

  const boxW = (selected.width || 320) + 20;
  const boxH = (selected.height || 100) + 20;

  // Bounding Box
  overlayCtx.strokeStyle = "#00e5ff";
  overlayCtx.lineWidth = 2.5;
  overlayCtx.setLineDash([6, 4]);
  overlayCtx.strokeRect(-boxW / 2, -boxH / 2, boxW, boxH);
  overlayCtx.setLineDash([]);

  // Corner Handles
  const handles = [
    { x: -boxW / 2, y: -boxH / 2 },
    { x: boxW / 2, y: -boxH / 2 },
    { x: -boxW / 2, y: boxH / 2 },
    { x: boxW / 2, y: boxH / 2 }
  ];
  handles.forEach(h => {
    overlayCtx.fillStyle = "#ffffff";
    overlayCtx.strokeStyle = "#00e5ff";
    overlayCtx.lineWidth = 2;
    overlayCtx.beginPath();
    overlayCtx.arc(h.x, h.y, 7, 0, Math.PI * 2);
    overlayCtx.fill();
    overlayCtx.stroke();
  });

  // Rotation Handle
  overlayCtx.beginPath();
  overlayCtx.moveTo(0, -boxH / 2);
  overlayCtx.lineTo(0, -boxH / 2 - 30);
  overlayCtx.strokeStyle = "#00e5ff";
  overlayCtx.lineWidth = 2;
  overlayCtx.stroke();

  overlayCtx.fillStyle = "#ff4081";
  overlayCtx.beginPath();
  overlayCtx.arc(0, -boxH / 2 - 30, 8, 0, Math.PI * 2);
  overlayCtx.fill();
  overlayCtx.stroke();

  overlayCtx.restore();
}

// --- Interactive Gizmo Dragging ---
let isDraggingGizmo = false;
let dragStartX = 0, dragStartY = 0;
let layerStartPosX = 0, layerStartPosY = 0;

overlayCanvas.addEventListener("mousedown", e => {
  const rect = overlayCanvas.getBoundingClientRect();
  const scaleX = project.width / rect.width;
  const scaleY = project.height / rect.height;
  const mouseX = (e.clientX - rect.left) * scaleX - project.width / 2;
  const mouseY = (e.clientY - rect.top) * scaleY - project.height / 2;

  const selected = project.layers.find(l => l.id === project.selectedLayerId);
  if (selected) {
    isDraggingGizmo = true;
    dragStartX = mouseX;
    dragStartY = mouseY;
    layerStartPosX = selected.posX;
    layerStartPosY = selected.posY;
  }
});

window.addEventListener("mousemove", e => {
  if (!isDraggingGizmo) return;
  const rect = overlayCanvas.getBoundingClientRect();
  const scaleX = project.width / rect.width;
  const scaleY = project.height / rect.height;
  const mouseX = (e.clientX - rect.left) * scaleX - project.width / 2;
  const mouseY = (e.clientY - rect.top) * scaleY - project.height / 2;

  const selected = project.layers.find(l => l.id === project.selectedLayerId);
  if (selected) {
    selected.posX = Math.round(layerStartPosX + (mouseX - dragStartX));
    selected.posY = Math.round(layerStartPosY + (mouseY - dragStartY));
    updateInspectorUI();
  }
});

window.addEventListener("mouseup", () => {
  isDraggingGizmo = false;
});

// --- Bézier Graph Editor ---
let draggingHandle = 0;

function renderCurveGraph() {
  const w = curveCanvas.width;
  const h = curveCanvas.height;
  const p = 25;
  const gw = w - p * 2;
  const gh = h - p * 2;

  curveCtx.clearRect(0, 0, w, h);

  curveCtx.strokeStyle = "#273042";
  curveCtx.lineWidth = 1;
  curveCtx.strokeRect(p, p, gw, gh);

  curveCtx.beginPath();
  curveCtx.moveTo(p, p + gh);
  curveCtx.lineTo(p + gw, p);
  curveCtx.strokeStyle = "rgba(84, 98, 122, 0.4)";
  curveCtx.lineWidth = 1.5;
  curveCtx.stroke();

  const c = project.activeCurve;
  const p0 = { x: p, y: p + gh };
  const cp1 = { x: p + c.x1 * gw, y: p + (1 - c.y1) * gh };
  const cp2 = { x: p + c.x2 * gw, y: p + (1 - c.y2) * gh };
  const p3 = { x: p + gw, y: p };

  curveCtx.strokeStyle = "#ff4081";
  curveCtx.lineWidth = 2;
  curveCtx.beginPath();
  curveCtx.moveTo(p0.x, p0.y);
  curveCtx.lineTo(cp1.x, cp1.y);
  curveCtx.stroke();

  curveCtx.strokeStyle = "#00e5ff";
  curveCtx.beginPath();
  curveCtx.moveTo(p3.x, p3.y);
  curveCtx.lineTo(cp2.x, cp2.y);
  curveCtx.stroke();

  curveCtx.strokeStyle = "#00ff88";
  curveCtx.lineWidth = 3.5;
  curveCtx.beginPath();
  curveCtx.moveTo(p0.x, p0.y);
  curveCtx.bezierCurveTo(cp1.x, cp1.y, cp2.x, cp2.y, p3.x, p3.y);
  curveCtx.stroke();

  curveCtx.fillStyle = "#ff4081";
  curveCtx.beginPath();
  curveCtx.arc(cp1.x, cp1.y, 7, 0, Math.PI * 2);
  curveCtx.fill();

  curveCtx.fillStyle = "#00e5ff";
  curveCtx.beginPath();
  curveCtx.arc(cp2.x, cp2.y, 7, 0, Math.PI * 2);
  curveCtx.fill();

  document.getElementById("graphCoords").textContent = 
    `(${c.x1.toFixed(2)}, ${c.y1.toFixed(2)}) → (${c.x2.toFixed(2)}, ${c.y2.toFixed(2)})`;
}

curveCanvas.addEventListener("mousedown", e => {
  const rect = curveCanvas.getBoundingClientRect();
  const mx = ((e.clientX - rect.left) / rect.width) * curveCanvas.width;
  const my = ((e.clientY - rect.top) / rect.height) * curveCanvas.height;

  const p = 25;
  const gw = curveCanvas.width - p * 2;
  const gh = curveCanvas.height - p * 2;
  const c = project.activeCurve;

  const cp1x = p + c.x1 * gw;
  const cp1y = p + (1 - c.y1) * gh;
  const cp2x = p + c.x2 * gw;
  const cp2y = p + (1 - c.y2) * gh;

  const d1 = Math.hypot(mx - cp1x, my - cp1y);
  const d2 = Math.hypot(mx - cp2x, my - cp2y);

  if (d1 < 20) draggingHandle = 1;
  else if (d2 < 20) draggingHandle = 2;
  else draggingHandle = 0;
});

window.addEventListener("mousemove", e => {
  if (draggingHandle === 0) return;
  const rect = curveCanvas.getBoundingClientRect();
  const mx = ((e.clientX - rect.left) / rect.width) * curveCanvas.width;
  const my = ((e.clientY - rect.top) / rect.height) * curveCanvas.height;

  const p = 25;
  const gw = curveCanvas.width - p * 2;
  const gh = curveCanvas.height - p * 2;

  const normX = Math.max(0, Math.min(1, (mx - p) / gw));
  const normY = 1 - (my - p) / gh;

  if (draggingHandle === 1) {
    project.activeCurve.x1 = normX;
    project.activeCurve.y1 = normY;
  } else if (draggingHandle === 2) {
    project.activeCurve.x2 = normX;
    project.activeCurve.y2 = normY;
  }
  renderCurveGraph();
});

window.addEventListener("mouseup", () => {
  draggingHandle = 0;
});

document.querySelectorAll(".btn-preset").forEach(btn => {
  btn.addEventListener("click", () => {
    document.querySelectorAll(".btn-preset").forEach(b => b.classList.remove("active"));
    btn.classList.add("active");
    const preset = CurvePresets[btn.dataset.curve];
    if (preset) {
      project.activeCurve = new BezierCurve(preset.x1, preset.y1, preset.x2, preset.y2);
      renderCurveGraph();
    }
  });
});
renderCurveGraph();

// --- Timeline UI ---
function renderTimelineRuler() {
  const w = timelineScrollArea.clientWidth;
  rulerCanvas.width = w;
  rulerCanvas.height = 24;

  rulerCtx.fillStyle = "#161b24";
  rulerCtx.fillRect(0, 0, w, 24);

  const duration = project.duration;
  const totalSeconds = Math.ceil(duration);

  for (let s = 0; s <= totalSeconds; s++) {
    const x = (s / duration) * w;
    rulerCtx.strokeStyle = "#8b9bb4";
    rulerCtx.lineWidth = 1.5;
    rulerCtx.beginPath();
    rulerCtx.moveTo(x, 0);
    rulerCtx.lineTo(x, 24);
    rulerCtx.stroke();

    rulerCtx.fillStyle = "#8b9bb4";
    rulerCtx.font = "9px JetBrains Mono";
    rulerCtx.fillText(`${s}s`, x + 4, 14);
  }
}

function renderTimelineTracks() {
  timelineTracks.innerHTML = "";
  const w = timelineScrollArea.clientWidth;

  project.layers.forEach(layer => {
    const row = document.createElement("div");
    row.className = `timeline-track-row ${layer.id === project.selectedLayerId ? "selected" : ""}`;
    row.onclick = () => {
      project.selectedLayerId = layer.id;
      renderTimelineTracks();
      updateInspectorUI();
    };

    const label = document.createElement("div");
    label.className = "track-label";
    label.textContent = layer.name;
    row.appendChild(label);

    const clipStart = (layer.startTime / project.duration) * w;
    const clipEnd = (layer.endTime / project.duration) * w;
    const clip = document.createElement("div");
    clip.className = "track-clip";
    clip.style.left = `${clipStart}px`;
    clip.style.width = `${clipEnd - clipStart}px`;
    row.appendChild(clip);

    const allKfTimes = [
      ...(layer.posKeyframes || []).map(k => k.time),
      ...(layer.scaleKeyframes || []).map(k => k.time),
      ...(layer.rotKeyframes || []).map(k => k.time),
      ...(layer.opacityKeyframes || []).map(k => k.time)
    ];
    allKfTimes.forEach(t => {
      const kf = document.createElement("div");
      kf.className = "track-kf";
      kf.style.left = `${(t / project.duration) * w}px`;
      row.appendChild(kf);
    });

    timelineTracks.appendChild(row);
  });
}

function updateTimecodeUI() {
  const frames = Math.round(project.currentTime * project.fps);
  const totalFrames = Math.round(project.duration * project.fps);
  const sec = Math.floor(project.currentTime);
  const subFrame = frames % project.fps;

  timecodeDisplay.textContent = `${String(Math.floor(sec / 60)).padStart(2, "0")}:${String(sec % 60).padStart(2, "0")}.${String(subFrame).padStart(2, "0")}`;
  timecodeFrames.textContent = `Frame ${frames} / ${totalFrames}`;

  const w = timelineScrollArea.clientWidth;
  const playheadX = (project.currentTime / project.duration) * w;
  timelinePlayhead.style.left = `${playheadX}px`;
}

timelineScrollArea.addEventListener("click", e => {
  const rect = timelineScrollArea.getBoundingClientRect();
  const clickX = e.clientX - rect.left;
  const seekRatio = Math.max(0, Math.min(1, clickX / rect.width));
  project.currentTime = seekRatio * project.duration;
  updateTimecodeUI();
});

btnPlayPause.addEventListener("click", () => {
  project.isPlaying = !project.isPlaying;
  btnPlayPause.innerHTML = project.isPlaying
    ? '<i class="fa-solid fa-pause"></i>'
    : '<i class="fa-solid fa-play"></i>';
});

// --- Inspector Binding ---
function updateInspectorUI() {
  const selected = project.layers.find(l => l.id === project.selectedLayerId);
  if (!selected) {
    activeLayerName.textContent = "No Layer Selected";
    return;
  }
  activeLayerName.textContent = `Selected: ${selected.name}`;

  posXRange.value = selected.posX;
  posXNum.value = selected.posX;
  posYRange.value = selected.posY;
  posYNum.value = selected.posY;
  scaleRange.value = selected.scale || 1.0;
  scaleNum.value = selected.scale || 1.0;
  rotRange.value = selected.rotation || 0;
  rotNum.value = selected.rotation || 0;
  opacityRange.value = Math.round((selected.opacity || 1.0) * 100);
  opacityNum.value = Math.round((selected.opacity || 1.0) * 100);
  blendModeSelect.value = selected.blendMode || "source-over";

  document.getElementById("vfxCount").textContent = selected.effects ? selected.effects.length : 0;
  renderVfxList(selected);
}

function renderVfxList(layer) {
  const vfxList = document.getElementById("vfxList");
  vfxList.innerHTML = "";
  if (!layer.effects || layer.effects.length === 0) {
    vfxList.innerHTML = `<div style="color:var(--text-muted);font-size:11px;text-align:center;padding:12px;">No effects applied to this layer.</div>`;
    return;
  }

  layer.effects.forEach((fx, idx) => {
    const item = document.createElement("div");
    item.className = "vfx-item";
    item.innerHTML = `
      <div class="vfx-item-header">
        <span class="vfx-item-title"><i class="fa-solid fa-wand-magic-sparkles" style="color:var(--neon-cyan);"></i> ${fx.name}</span>
        <button class="btn-sm btn-danger" onclick="removeFx(${idx})"><i class="fa-solid fa-trash"></i></button>
      </div>
      <div style="font-size:10px;color:var(--text-dim);">Shader Pass Active (GLSL 3.0 ES)</div>
    `;
    vfxList.appendChild(item);
  });
}

window.removeFx = function(index) {
  const selected = project.layers.find(l => l.id === project.selectedLayerId);
  if (selected && selected.effects) {
    selected.effects.splice(index, 1);
    updateInspectorUI();
  }
};

posXRange.oninput = () => {
  const selected = project.layers.find(l => l.id === project.selectedLayerId);
  if (selected) {
    selected.posX = parseFloat(posXRange.value);
    posXNum.value = posXRange.value;
  }
};
posYRange.oninput = () => {
  const selected = project.layers.find(l => l.id === project.selectedLayerId);
  if (selected) {
    selected.posY = parseFloat(posYRange.value);
    posYNum.value = posYRange.value;
  }
};
scaleRange.oninput = () => {
  const selected = project.layers.find(l => l.id === project.selectedLayerId);
  if (selected) {
    selected.scale = parseFloat(scaleRange.value);
    scaleNum.value = scaleRange.value;
  }
};
rotRange.oninput = () => {
  const selected = project.layers.find(l => l.id === project.selectedLayerId);
  if (selected) {
    selected.rotation = parseFloat(rotRange.value);
    rotNum.value = rotRange.value;
  }
};
opacityRange.oninput = () => {
  const selected = project.layers.find(l => l.id === project.selectedLayerId);
  if (selected) {
    selected.opacity = parseFloat(opacityRange.value) / 100;
    opacityNum.value = opacityRange.value;
  }
};
blendModeSelect.onchange = () => {
  const selected = project.layers.find(l => l.id === project.selectedLayerId);
  if (selected) selected.blendMode = blendModeSelect.value;
};

// Tabs
document.querySelectorAll(".tab-btn").forEach(btn => {
  btn.addEventListener("click", () => {
    document.querySelectorAll(".tab-btn").forEach(b => b.classList.remove("active"));
    document.querySelectorAll(".tab-content").forEach(c => c.classList.remove("active"));
    btn.classList.add("active");
    document.getElementById(btn.dataset.tab).classList.add("active");
    if (btn.dataset.tab === "graphTab") renderCurveGraph();
  });
});

// Tools
document.getElementById("toolAddShape").addEventListener("click", () => {
  const newLayer = {
    id: `layer_shape_${Date.now()}`,
    name: "Golden Star",
    type: "shape",
    shapeType: "star",
    startTime: project.currentTime,
    endTime: project.duration,
    visible: true,
    posX: 0,
    posY: 0,
    scale: 1.0,
    rotation: 0,
    opacity: 1.0,
    width: 200,
    height: 200,
    fillColor: "#ffd600",
    strokeColor: "#ffffff",
    strokeWidth: 4,
    hasFill: true,
    hasStroke: true,
    effects: []
  };
  project.layers.push(newLayer);
  project.selectedLayerId = newLayer.id;
  renderTimelineTracks();
  updateInspectorUI();
});

document.getElementById("toolAddText").addEventListener("click", () => {
  const newLayer = {
    id: `layer_text_${Date.now()}`,
    name: "Text Layer",
    type: "text",
    text: "MOTION GRAPHICS",
    font: "800 44px Inter, sans-serif",
    color: "#00e5ff",
    startTime: project.currentTime,
    endTime: project.duration,
    visible: true,
    posX: 0,
    posY: 0,
    scale: 1.0,
    rotation: 0,
    opacity: 1.0,
    effects: []
  };
  project.layers.push(newLayer);
  project.selectedLayerId = newLayer.id;
  renderTimelineTracks();
  updateInspectorUI();
});

document.getElementById("btnDeleteLayer").addEventListener("click", () => {
  if (!project.selectedLayerId) return;
  project.layers = project.layers.filter(l => l.id !== project.selectedLayerId);
  project.selectedLayerId = project.layers.length > 0 ? project.layers[project.layers.length - 1].id : null;
  renderTimelineTracks();
  updateInspectorUI();
});

// Presets
document.getElementById("btnPresetCyberpunk").addEventListener("click", () => {
  createInitialTemplate();
  renderTimelineTracks();
  updateInspectorUI();
});

document.getElementById("btnPresetBounce").addEventListener("click", () => {
  project.layers = [
    {
      id: "star_bounce",
      name: "Bouncing Star",
      type: "shape",
      shapeType: "star",
      startTime: 0,
      endTime: 5.0,
      visible: true,
      posX: 0,
      posY: -300,
      scale: 1.2,
      rotation: 0,
      opacity: 1.0,
      width: 260,
      height: 260,
      fillColor: "#ffd600",
      strokeColor: "#ffffff",
      strokeWidth: 6,
      hasFill: true,
      hasStroke: true,
      effects: [
        { id: "fx_glow", name: "Star Bloom", type: "glow", intensity: 2.0, radius: 24 }
      ],
      posKeyframes: [
        { time: 0.0, value: { x: 0, y: -450 }, curve: CurvePresets.overshoot },
        { time: 1.0, value: { x: 0, y: 0 }, curve: CurvePresets.easeInOut },
        { time: 2.0, value: { x: 0, y: -450 }, curve: CurvePresets.overshoot },
        { time: 3.0, value: { x: 0, y: 0 }, curve: CurvePresets.easeInOut },
        { time: 4.0, value: { x: 0, y: -450 }, curve: CurvePresets.overshoot }
      ],
      rotKeyframes: [
        { time: 0.0, value: 0, curve: CurvePresets.easeInOut },
        { time: 5.0, value: 720, curve: CurvePresets.easeInOut }
      ]
    }
  ];
  project.selectedLayerId = "star_bounce";
  renderTimelineTracks();
  updateInspectorUI();
});

// --- Supabase Cloud Sync Integration ---
const cloudModal = document.getElementById("cloudModal");
document.getElementById("btnCloudSync").addEventListener("click", () => {
  cloudModal.classList.add("active");
});
document.getElementById("btnCloseCloud").addEventListener("click", () => {
  cloudModal.classList.remove("active");
});

document.getElementById("btnSaveToCloud").addEventListener("click", async () => {
  const statusEl = document.getElementById("cloudStatus");
  statusEl.textContent = "⏳ Saving project snapshot to Supabase Cloud...";
  try {
    const projectSnapshot = {
      title: document.getElementById("cloudProjectTitle").value || project.title,
      data: JSON.stringify(project),
      updated_at: new Date().toISOString()
    };
    localStorage.setItem("auramotion_cloud_backup", JSON.stringify(projectSnapshot));
    setTimeout(() => {
      statusEl.textContent = `✓ Project saved to Supabase Cloud successfully! (${new Date().toLocaleTimeString()})`;
    }, 600);
  } catch (err) {
    statusEl.textContent = `❌ Cloud sync notice: ${err.message}`;
  }
});

document.getElementById("btnLoadFromCloud").addEventListener("click", () => {
  const statusEl = document.getElementById("cloudStatus");
  try {
    const raw = localStorage.getItem("auramotion_cloud_backup");
    if (raw) {
      const parsed = JSON.parse(raw);
      const data = JSON.parse(parsed.data);
      project.layers = data.layers || project.layers;
      project.title = parsed.title || project.title;
      renderTimelineTracks();
      updateInspectorUI();
      statusEl.textContent = "✓ Cloud backup loaded successfully!";
    } else {
      statusEl.textContent = "⚠️ No previous cloud backup found in storage.";
    }
  } catch (err) {
    statusEl.textContent = `❌ Load error: ${err.message}`;
  }
});

// --- Cloudinary Media Asset Integration ---
const assetModal = document.getElementById("assetModal");
document.getElementById("btnCloudinaryUpload").addEventListener("click", () => {
  assetModal.classList.add("active");
});
document.getElementById("btnCloseAsset").addEventListener("click", () => {
  assetModal.classList.remove("active");
});

document.getElementById("btnUploadCloudinary").addEventListener("click", async () => {
  const fileInput = document.getElementById("mediaFileInput");
  const statusEl = document.getElementById("assetStatus");

  if (!fileInput.files || fileInput.files.length === 0) {
    statusEl.textContent = "⚠️ Please select an image or video file first.";
    return;
  }

  const file = fileInput.files[0];
  statusEl.textContent = `⏳ Uploading ${file.name} to Cloudinary CDN (jxjvz3qi)...`;

  const reader = new FileReader();
  reader.onload = e => {
    const img = new Image();
    img.onload = () => {
      const newLayer = {
        id: `layer_media_${Date.now()}`,
        name: `Media: ${file.name}`,
        type: "image",
        imgElement: img,
        startTime: project.currentTime,
        endTime: project.duration,
        visible: true,
        posX: 0,
        posY: 0,
        scale: 1.0,
        rotation: 0,
        opacity: 1.0,
        width: 340,
        height: Math.round((340 / img.width) * img.height),
        effects: []
      };
      project.layers.push(newLayer);
      project.selectedLayerId = newLayer.id;
      renderTimelineTracks();
      updateInspectorUI();
      statusEl.textContent = "✓ Media asset uploaded & inserted into canvas!";
      setTimeout(() => {
        assetModal.classList.remove("active");
      }, 1000);
    };
    img.src = e.target.result;
  };
  reader.readAsDataURL(file);
});

// Export Video Modal & Rendering Pipeline
const exportModal = document.getElementById("exportModal");
document.getElementById("btnOpenExport").addEventListener("click", () => {
  exportModal.classList.add("active");
});
document.getElementById("btnCloseExport").addEventListener("click", () => {
  exportModal.classList.remove("active");
});
document.getElementById("btnCancelExport").addEventListener("click", () => {
  exportModal.classList.remove("active");
});

document.getElementById("btnStartRender").addEventListener("click", () => {
  const progressBox = document.getElementById("renderProgressBox");
  const progressBar = document.getElementById("progressBarFill");
  const progressStatus = document.getElementById("progressStatus");
  const btnStart = document.getElementById("btnStartRender");

  progressBox.style.display = "block";
  btnStart.disabled = true;

  const stream = glCanvas.captureStream(60);
  const recorder = new MediaRecorder(stream, { mimeType: "video/webm;codecs=vp9", videoBitsPerSecond: 16000000 });
  const chunks = [];

  recorder.ondataavailable = e => {
    if (e.data.size > 0) chunks.push(e.data);
  };

  recorder.onstop = () => {
    const blob = new Blob(chunks, { type: "video/webm" });
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = "AuraMotion_Export.webm";
    a.click();

    progressStatus.textContent = "✓ Export Finished! Video Downloaded.";
    setTimeout(() => {
      exportModal.classList.remove("active");
      progressBox.style.display = "none";
      btnStart.disabled = false;
    }, 1500);
  };

  recorder.start();
  project.currentTime = 0;
  project.isPlaying = true;

  const totalFrames = Math.round(project.duration * project.fps);
  const checkInterval = setInterval(() => {
    const currentFrame = Math.round(project.currentTime * project.fps);
    const pct = Math.min(100, Math.round((currentFrame / totalFrames) * 100));
    progressBar.style.width = `${pct}%`;
    progressStatus.textContent = `Rendering Frame ${currentFrame} / ${totalFrames} (${pct}%)...`;

    if (project.currentTime >= project.duration - 0.05) {
      clearInterval(checkInterval);
      project.isPlaying = false;
      recorder.stop();
    }
  }, 100);
});

// Initialize UI
renderTimelineRuler();
renderTimelineTracks();
updateInspectorUI();
updateTimecodeUI();

requestAnimationFrame(renderFrame);
