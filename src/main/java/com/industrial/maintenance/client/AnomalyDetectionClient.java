package com.industrial.maintenance.client;

import com.industrial.maintenance.config.MlProperties;
import com.industrial.maintenance.entity.SensorReading;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.util.Optional;

@Component
public class AnomalyDetectionClient {

    private static final Logger log = LoggerFactory.getLogger(AnomalyDetectionClient.class);

    /** What we send to the ML service. Field names match the Python model. */
    record AnomalyRequest(Double temperatureCelsius, Double vibrationMmPerSec, Double oilPressureBar) {}

    /** What the ML service sends back. */
    public record AnomalyPrediction(boolean anomaly, double anomalyScore, String modelVersion) {}

    private final MlProperties properties;
    private final RestClient restClient;

    public AnomalyDetectionClient(MlProperties properties) {
        this.properties = properties;

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofMillis(properties.timeoutMillis()));
        requestFactory.setReadTimeout(Duration.ofMillis(properties.timeoutMillis()));

        this.restClient = RestClient.builder()
                .baseUrl(properties.baseUrl() != null ? properties.baseUrl() : "http://localhost:8000")
                .requestFactory(requestFactory)
                .build();
    }

    /**
     * Asks the ML service whether a reading is anomalous.
     * Returns empty if detection is disabled or the service cannot be reached:
     * the caller must work without a prediction.
     */
    public Optional<AnomalyPrediction> predict(SensorReading reading) {
        if (!properties.enabled()) {
            return Optional.empty();
        }
        try {
            AnomalyPrediction prediction = restClient.post()
                    .uri("/predict")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new AnomalyRequest(reading.getTemperatureCelsius(),
                            reading.getVibrationMmPerSec(), reading.getOilPressureBar()))
                    .retrieve()
                    .body(AnomalyPrediction.class);
            return Optional.ofNullable(prediction);
        } catch (RestClientException ex) {
            log.warn("Anomaly detection unavailable ({}); reading saved without a prediction", ex.getMessage());
            return Optional.empty();
        }
    }
}