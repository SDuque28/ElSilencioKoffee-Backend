package ElSilencioKoffee_Backend.orders.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OrderCustomerResponse {

    private Long id;
    private String username;
    private String email;
}
