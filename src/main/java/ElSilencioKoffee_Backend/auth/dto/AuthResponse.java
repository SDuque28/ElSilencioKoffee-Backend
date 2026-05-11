package ElSilencioKoffee_Backend.auth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class AuthResponse {

    private String token;
    private String username;
    private String email;
    private List<String> roles;
}

