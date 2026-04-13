package ElSilencioKoffee_Backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateRequest {

    private String username;
    private String email;
    private Boolean activo;
}
