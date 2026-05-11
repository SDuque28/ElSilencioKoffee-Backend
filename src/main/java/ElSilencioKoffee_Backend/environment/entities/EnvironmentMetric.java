package ElSilencioKoffee_Backend.environment.entities;

import ElSilencioKoffee_Backend.production.entities.ProductionSection;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "environment_metrics")
@Getter
@Setter
@NoArgsConstructor
public class EnvironmentMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_metric")
    private Long id;

    @Column(name = "metric_type", nullable = false, length = 50)
    private String metricType;

    @Column(name = "metric_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal metricValue;

    @Column(nullable = false, length = 30)
    private String unit;

    @Column(name = "measured_at", nullable = false)
    private LocalDateTime measuredAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_section")
    private ProductionSection section;

    @PrePersist
    void onCreate() {
        if (measuredAt == null) {
            measuredAt = LocalDateTime.now();
        }
    }
}
