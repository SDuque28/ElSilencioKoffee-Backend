package ElSilencioKoffee_Backend.cart.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class CartResponse {

    private Long id;
    private Long userId;
    private Integer totalItems;
    private BigDecimal totalAmount;
    private List<CartItemResponse> items = new ArrayList<>();
}
