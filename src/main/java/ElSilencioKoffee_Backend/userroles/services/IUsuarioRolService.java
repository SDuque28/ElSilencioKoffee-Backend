package ElSilencioKoffee_Backend.userroles.services;

import ElSilencioKoffee_Backend.userroles.entities.UsuarioRol;
import ElSilencioKoffee_Backend.userroles.entities.UsuarioRolId;

import java.util.List;

public interface IUsuarioRolService {

    List<UsuarioRol> findByUsuarioId(Long usuarioId);

    List<UsuarioRol> findByRolId(Long rolId);

    UsuarioRol save(UsuarioRol usuarioRol);

    boolean delete(UsuarioRolId id);
}

