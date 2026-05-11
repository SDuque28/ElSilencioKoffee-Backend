package ElSilencioKoffee_Backend.checkout.dto;

import ElSilencioKoffee_Backend.orders.entities.PaymentMethod;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PaymentSimulationRequest {

    private PaymentMethod paymentMethod;
    private String cardholderName;
    private String cardNumber;
    private String expirationDate;
    private String cvv;
}
