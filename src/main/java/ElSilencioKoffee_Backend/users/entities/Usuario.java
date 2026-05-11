package ElSilencioKoffee_Backend.users.entities;

import ElSilencioKoffee_Backend.userroles.entities.UsuarioRol;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "usuario")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@ToString(exclude = "usuariosRoles")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean activo;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "usuario")
    private List<UsuarioRol> usuariosRoles = new ArrayList<>();
}

