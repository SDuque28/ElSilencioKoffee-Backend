package ElSilencioKoffee_Backend.environment.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class EnvironmentMetricCreateRequest {

    private String metricType;
    private BigDecimal value;
    private String unit;
    private LocalDateTime measuredAt;
    private Long sectionId;
}
