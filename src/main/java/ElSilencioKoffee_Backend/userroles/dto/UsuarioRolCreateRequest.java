package ElSilencioKoffee_Backend.userroles.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UsuarioRolCreateRequest {

    private Long usuarioId;
    private Long rolId;
}

