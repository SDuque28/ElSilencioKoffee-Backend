package ElSilencioKoffee_Backend.cart.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateCartItemRequest {

    private Integer quantity;
}
