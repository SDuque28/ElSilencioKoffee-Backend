package ElSilencioKoffee_Backend.orders.dto;

import ElSilencioKoffee_Backend.orders.entities.DeliveryStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class DeliveryOrderResponse {

    private Long id;
    private DeliveryStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
