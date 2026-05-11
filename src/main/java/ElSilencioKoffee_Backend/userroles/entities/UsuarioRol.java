package ElSilencioKoffee_Backend.userroles.entities;

import ElSilencioKoffee_Backend.roles.entities.Rol;
import ElSilencioKoffee_Backend.users.entities.Usuario;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "usuario_rol")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@ToString(exclude = {"usuario", "rol"})
public class UsuarioRol {

    @EmbeddedId
    private UsuarioRolId id;

    @ManyToOne
    @MapsId("usuarioId")
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne
    @MapsId("rolId")
    @JoinColumn(name = "rol_id")
    private Rol rol;
}

