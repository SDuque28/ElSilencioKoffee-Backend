package ElSilencioKoffee_Backend.userroles.repositories;

import ElSilencioKoffee_Backend.userroles.entities.UsuarioRol;
import ElSilencioKoffee_Backend.userroles.entities.UsuarioRolId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UsuarioRolRepository extends JpaRepository<UsuarioRol, UsuarioRolId> {

    List<UsuarioRol> findByIdUsuarioId(Long usuarioId);

    List<UsuarioRol> findByIdRolId(Long rolId);
}

