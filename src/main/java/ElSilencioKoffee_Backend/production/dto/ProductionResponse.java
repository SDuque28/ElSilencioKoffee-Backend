package ElSilencioKoffee_Backend.production.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class ProductionResponse {

    private Long id;
    private Long sectionId;
    private String sectionName;
    private String sectionLocation;
    private Long varietyId;
    private String varietyName;
    private BigDecimal quantityKg;
    private LocalDate collectionDate;
}
