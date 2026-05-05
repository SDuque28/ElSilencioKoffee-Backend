package ElSilencioKoffee_Backend.products.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class ProductResponse {

    private Long id;
    private String name;
    private String imageUrl;
    private BigDecimal price;
    private Long presentationId;
    private Long productionId;
    private Integer stockQuantity;
}
