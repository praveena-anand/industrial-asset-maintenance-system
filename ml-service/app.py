"""
Anomaly detection service for equipment sensor readings.

An Isolation Forest learns what normal readings look like (temperature, vibration
and oil pressure together) and flags readings that do not fit, even when no single
value has crossed a fixed threshold.

Note: this educational version trains on synthetic "normal" data that mirrors the
sensor simulator. A production system would train on historical readings from
machines known to be healthy, and retrain regularly.
"""

import numpy as np
from fastapi import FastAPI
from pydantic import BaseModel, Field
from sklearn.ensemble import IsolationForest

MODEL_VERSION = "isolation-forest-v1"
RANDOM_SEED = 42
TRAINING_SAMPLES = 5000


def generate_normal_readings(count: int, rng: np.random.Generator) -> np.ndarray:
    """Synthetic readings from healthy machines: [temperature, vibration, oil pressure]."""
    temperature = rng.normal(80.0, 1.5, count)
    vibration = np.clip(rng.normal(3.0, 0.4, count), 0.0, None)
    oil_pressure = np.clip(rng.normal(4.2, 0.15, count), 0.0, None)
    return np.column_stack([temperature, vibration, oil_pressure])


# Train once, when the service starts
rng = np.random.default_rng(RANDOM_SEED)
model = IsolationForest(n_estimators=200, contamination=0.005, random_state=RANDOM_SEED)
model.fit(generate_normal_readings(TRAINING_SAMPLES, rng))

app = FastAPI(title="Equipment Anomaly Detection", version="1.0.0")


class Reading(BaseModel):
    temperatureCelsius: float = Field(..., ge=-50, le=200)
    vibrationMmPerSec: float = Field(..., ge=0, le=100)
    oilPressureBar: float = Field(..., ge=0, le=50)


class Prediction(BaseModel):
    anomaly: bool
    anomalyScore: float      # higher = more unusual
    modelVersion: str


@app.get("/health")
def health() -> dict:
    return {"status": "UP", "modelVersion": MODEL_VERSION}


@app.post("/predict", response_model=Prediction)
def predict(reading: Reading) -> Prediction:
    features = np.array([[reading.temperatureCelsius,
                          reading.vibrationMmPerSec,
                          reading.oilPressureBar]])
    label = model.predict(features)[0]                     # 1 = normal, -1 = anomaly
    score = float(-model.score_samples(features)[0])       # flip the sign so higher = more unusual
    return Prediction(anomaly=bool(label == -1),
                      anomalyScore=round(score, 4),
                      modelVersion=MODEL_VERSION)