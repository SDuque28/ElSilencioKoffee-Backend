package ElSilencioKoffee_Backend.services.impl;

import ElSilencioKoffee_Backend.entities.UsuarioRol;
import ElSilencioKoffee_Backend.entities.UsuarioRolId;
import ElSilencioKoffee_Backend.repository.UsuarioRolRepository;
import ElSilencioKoffee_Backend.services.IUsuarioRolService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioRolServiceImpl implements IUsuarioRolService {

    private final UsuarioRolRepository usuarioRolRepository;

    @Override
    public List<UsuarioRol> findByUsuarioId(Long usuarioId) {
        return usuarioRolRepository.findByIdUsuarioId(usuarioId);
    }

    @Override
    public List<UsuarioRol> findByRolId(Long rolId) {
        return usuarioRolRepository.findByIdRolId(rolId);
    }

    @Override
    public UsuarioRol save(UsuarioRol usuarioRol) {
        UsuarioRolId id = new UsuarioRolId(
                usuarioRol.getUsuario().getId(),
                usuarioRol.getRol().getId()
        );
        usuarioRol.setId(id);
        return usuarioRolRepository.save(usuarioRol);
    }

    @Override
    public boolean delete(UsuarioRolId id) {
        if (!usuarioRolRepository.existsById(id)) return false;
        usuarioRolRepository.deleteById(id);
        return true;
    }
}
