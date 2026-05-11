package ElSilencioKoffee_Backend.environment.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class EnvironmentMetricResponse {

    private Long id;
    private String metricType;
    private BigDecimal value;
    private String unit;
    private LocalDateTime measuredAt;
    private Long sectionId;
    private String sectionName;
    private String sectionLocation;
}
