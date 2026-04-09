package ElSilencioKoffee_Backend.services;

import ElSilencioKoffee_Backend.entities.UsuarioRol;
import ElSilencioKoffee_Backend.entities.UsuarioRolId;

import java.util.List;

public interface IUsuarioRolService {

    List<UsuarioRol> findByUsuarioId(Long usuarioId);

    List<UsuarioRol> findByRolId(Long rolId);

    UsuarioRol save(UsuarioRol usuarioRol);

    boolean delete(UsuarioRolId id);
}
