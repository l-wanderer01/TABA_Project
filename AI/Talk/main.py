from fastapi import FastAPI, File, UploadFile, HTTPException
from pydantic import BaseModel
from PIL import Image
import cv2
import numpy as np
from deepface import DeepFace
import tempfile
import io
import mediapipe as mp

app = FastAPI()

# MediaPipe Face Detection 초기화
mp_face_detection = mp.solutions.face_detection

class Detection(BaseModel):
    gender: str
    age: int
    emotion: str
    emotion_confidence: float

class PredictionResponse(BaseModel):
    detections: list[Detection]

def compress_image(image_bytes: bytes, target_size_kb: int = 40):
    """
    이미지 크기를 target_size_kb 이하로 압축
    """
    image = Image.open(io.BytesIO(image_bytes))
    quality = 95  # 초기 품질
    buffer = io.BytesIO()

    # 품질을 반복적으로 낮춰 용량 줄이기
    while quality > 40:
        buffer.seek(0)
        buffer.truncate()
        image.save(buffer, format="JPEG", quality=quality)
        size_kb = len(buffer.getvalue()) // 1024
        if size_kb <= target_size_kb:
            break
        quality -= 5

    if quality <= 40 and len(buffer.getvalue()) > target_size_kb * 1024:
        raise Exception("Cannot compress image to target size without significant quality loss.")
    
    return buffer.getvalue()

def detect_faces_mediapipe(image_bytes: bytes):
    """
    MediaPipe를 사용해 얼굴을 감지하고 얼굴 영역을 반환
    """
    image_array = np.frombuffer(image_bytes, np.uint8)
    img = cv2.imdecode(image_array, cv2.IMREAD_COLOR)

    if img is None:
        raise Exception("Invalid image format")

    # MediaPipe Face Detection 사용
    with mp_face_detection.FaceDetection(model_selection=0, min_detection_confidence=0.5) as face_detection:
        img_rgb = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)
        results = face_detection.process(img_rgb)

        if not results.detections:
            return []

        faces_cropped = []
        for detection in results.detections:
            bbox = detection.location_data.relative_bounding_box
            h, w, _ = img.shape

            # Bounding Box 좌표 계산
            x_min = int(bbox.xmin * w)
            y_min = int(bbox.ymin * h)
            width = int(bbox.width * w)
            height = int(bbox.height * h)

            # 얼굴 영역 잘라내기 및 리사이즈
            x_min, y_min = max(0, x_min), max(0, y_min)
            face = img[y_min:y_min + height, x_min:x_min + width]
            face_resized = cv2.resize(face, (224, 224))
            faces_cropped.append(cv2.cvtColor(face_resized, cv2.COLOR_BGR2RGB))

    return faces_cropped

@app.post("/predict/", response_model=PredictionResponse)
async def predict(file: UploadFile = File(...)):
    """
    얼굴을 감지한 후 분석하여 성별, 감정상태, 연령대를 반환
    """
    try:
        # 파일 데이터를 읽고 압축
        image_bytes = await file.read()
        compressed_image_bytes = compress_image(image_bytes)

        # MediaPipe 얼굴 감지
        faces = detect_faces_mediapipe(compressed_image_bytes)
        if not faces:
            raise HTTPException(status_code=400, detail="No face detected in the image.")

        detections = []
        for face in faces:
            with tempfile.NamedTemporaryFile(suffix=".jpg", delete=False) as temp_face_file:
                temp_face_path = temp_face_file.name
                cv2.imwrite(temp_face_path, face)

            # DeepFace 분석 수행
            analysis = DeepFace.analyze(img_path=temp_face_path, actions=["gender", "age", "emotion"], enforce_detection=False)
            emotion_data = analysis[0]["emotion"]
            dominant_emotion = max(emotion_data, key=emotion_data.get)
            emotion_confidence = round(emotion_data[dominant_emotion], 2)

            detections.append({
                "gender": analysis[0]["dominant_gender"],
                "age": analysis[0]["age"],
                "emotion": dominant_emotion,
                "emotion_confidence": emotion_confidence
            })

        return {"detections": detections}

    except HTTPException as he:
        raise he
    except Exception as e:
        return {"error": str(e)}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=5001)