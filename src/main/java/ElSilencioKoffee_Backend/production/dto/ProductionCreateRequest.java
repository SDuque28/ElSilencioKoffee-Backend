package ElSilencioKoffee_Backend.production.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class ProductionCreateRequest {

    private Long sectionId;
    private Long varietyId;
    private LocalDate collectionDate;
    private BigDecimal quantityKg;
}
