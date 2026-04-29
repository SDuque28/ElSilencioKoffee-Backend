CREATE TABLE environment_metrics (
    id_metric BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    metric_type VARCHAR(50) NOT NULL,
    metric_value NUMERIC(10,2) NOT NULL,
    unit VARCHAR(30) NOT NULL,
    measured_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id_section INT UNSIGNED NULL,
    FOREIGN KEY (id_section) REFERENCES sections(id_section)
);

CREATE INDEX idx_environment_metrics_type ON environment_metrics(metric_type);
CREATE INDEX idx_environment_metrics_measured_at ON environment_metrics(measured_at);
CREATE INDEX idx_environment_metrics_section ON environment_metrics(id_section);
