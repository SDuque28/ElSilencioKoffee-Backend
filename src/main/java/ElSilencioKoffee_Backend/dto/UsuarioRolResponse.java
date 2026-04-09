package ElSilencioKoffee_Backend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UsuarioRolResponse {

    private Long usuarioId;
    private String username;
    private Long rolId;
    private String rolNombre;
}
