from fastapi import FastAPI, File, UploadFile
from pydantic import BaseModel
from PIL import Image
from ultralytics import YOLO

app = FastAPI()

# 학습된 YOLOv11 모델 로드
MODEL_PATH = "Move/yolo11n.pt"
model = YOLO(MODEL_PATH)

class Detection(BaseModel):
    class_id: int
    confidence: float
    x_min : float
    y_min : float
    x_max : float
    y_max : float

class PredictionResponse(BaseModel):
    detections: list[Detection]

@app.post("/predict/", response_model=PredictionResponse)
async def predict(file: UploadFile = File(...)):
    try:
        # 업로드된 파일을 PIL Image로 읽기
        img = Image.open(file.file)

        # 모델 추론
        results = model.predict(source=img)

        # 탐지 결과를 JSON 형태로 반환
        detections = []
        for detection in results[0].boxes.data.tolist():
            x_min, y_min, x_max, y_max, confidence, class_id = detection
            detections.append({
                "class_id": int(class_id),
                "confidence": float(confidence),
                "x_min": float(x_min),
                "y_min": float(y_min),
                "x_max": float(x_max),
                "y_max": float(y_max)
                
            })

        return {"detections": detections}
    except Exception as e:
        return {"error": str(e)}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=8001)
    
    