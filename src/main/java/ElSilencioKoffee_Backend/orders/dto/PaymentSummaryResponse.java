package ElSilencioKoffee_Backend.orders.dto;

import ElSilencioKoffee_Backend.orders.entities.PaymentMethod;
import ElSilencioKoffee_Backend.orders.entities.PaymentStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class PaymentSummaryResponse {

    private PaymentMethod paymentMethod;
    private String maskedCardNumber;
    private PaymentStatus status;
    private String transactionReference;
    private LocalDateTime paidAt;
}
