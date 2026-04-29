package ElSilencioKoffee_Backend.orders.dto;

import ElSilencioKoffee_Backend.orders.entities.OrderStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class OrderCreateResponse {

    private Long id;
    private Long userId;
    private LocalDateTime orderDate;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private List<OrderDetailResponse> items;
}

