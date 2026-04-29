package ElSilencioKoffee_Backend.products.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class ProductUpdateRequest {

    private String name;
    private String imageUrl;
    private BigDecimal price;
    private Long presentationId;
    private Long productionId;
}
