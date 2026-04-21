package ElSilencioKoffee_Backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderCreateItemRequest {

    private Long productId;
    private Integer quantity;
}
