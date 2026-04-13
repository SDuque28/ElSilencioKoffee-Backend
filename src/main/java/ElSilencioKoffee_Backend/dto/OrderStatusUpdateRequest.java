package ElSilencioKoffee_Backend.dto;

import ElSilencioKoffee_Backend.entities.OrderStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderStatusUpdateRequest {

    private OrderStatus status;
}
