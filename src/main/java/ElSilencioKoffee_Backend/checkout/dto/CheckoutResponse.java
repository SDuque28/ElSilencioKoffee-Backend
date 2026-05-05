package ElSilencioKoffee_Backend.checkout.dto;

import ElSilencioKoffee_Backend.orders.dto.DeliveryOrderResponse;
import ElSilencioKoffee_Backend.orders.dto.OrderDetailResponse;
import ElSilencioKoffee_Backend.orders.dto.PaymentSummaryResponse;
import ElSilencioKoffee_Backend.orders.dto.ShippingInformationResponse;
import ElSilencioKoffee_Backend.orders.entities.OrderStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class CheckoutResponse {

    private Long orderId;
    private LocalDateTime orderDate;
    private OrderStatus orderStatus;
    private BigDecimal totalAmount;
    private String notes;
    private List<OrderDetailResponse> items;
    private ShippingInformationResponse shippingInformation;
    private PaymentSummaryResponse payment;
    private DeliveryOrderResponse deliveryOrder;
}
