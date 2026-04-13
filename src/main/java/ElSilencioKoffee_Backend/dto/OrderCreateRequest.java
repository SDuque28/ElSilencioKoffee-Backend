package ElSilencioKoffee_Backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class OrderCreateRequest {

    private BigDecimal totalAmount;
}
