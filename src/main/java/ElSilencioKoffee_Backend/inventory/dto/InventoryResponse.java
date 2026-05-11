package ElSilencioKoffee_Backend.inventory.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class InventoryResponse {

    private Long id;
    private Long productId;
    private String productName;
    private String productImageUrl;
    private Integer stockQuantity;
}
