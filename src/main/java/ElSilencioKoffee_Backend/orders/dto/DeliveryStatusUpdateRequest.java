package ElSilencioKoffee_Backend.orders.dto;

import ElSilencioKoffee_Backend.orders.entities.DeliveryStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeliveryStatusUpdateRequest {

    private DeliveryStatus status;
}
