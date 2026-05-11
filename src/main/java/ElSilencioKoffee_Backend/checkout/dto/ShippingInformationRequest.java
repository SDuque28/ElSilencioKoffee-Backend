package ElSilencioKoffee_Backend.checkout.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ShippingInformationRequest {

    private String address;
    private String country;
    private String city;
    private String neighborhood;
    private String referenceDetails;
}
