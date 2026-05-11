package ElSilencioKoffee_Backend.cart.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AddCartItemRequest {

    private Long productId;
    private Integer quantity;
}
