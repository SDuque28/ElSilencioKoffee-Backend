package ElSilencioKoffee_Backend.users.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private Boolean activo;
    private LocalDateTime createdAt;
}

