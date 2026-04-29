package ElSilencioKoffee_Backend.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordRecoveryRequest {

    private String username;
    private String email;
    private String newPassword;
    private String confirmPassword;
}

