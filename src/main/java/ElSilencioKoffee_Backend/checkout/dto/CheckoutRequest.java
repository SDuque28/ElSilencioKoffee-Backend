package ElSilencioKoffee_Backend.checkout.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CheckoutRequest {

    private ShippingInformationRequest shippingInformation;
    private PaymentSimulationRequest payment;
    private String notes;
}
