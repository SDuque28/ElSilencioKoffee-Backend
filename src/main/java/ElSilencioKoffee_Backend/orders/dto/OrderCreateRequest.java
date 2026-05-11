package ElSilencioKoffee_Backend.orders.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OrderCreateRequest {

    private List<OrderCreateItemRequest> items;
}

