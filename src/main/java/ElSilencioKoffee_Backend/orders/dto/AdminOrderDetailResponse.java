package ElSilencioKoffee_Backend.orders.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AdminOrderDetailResponse extends OrderResponse {

    private OrderCustomerResponse customer;
}
