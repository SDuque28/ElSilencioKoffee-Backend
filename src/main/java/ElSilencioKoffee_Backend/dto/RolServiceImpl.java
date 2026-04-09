package ElSilencioKoffee_Backend.dto;

import ElSilencioKoffee_Backend.entities.Rol;
import ElSilencioKoffee_Backend.repository.RolRepository;
import ElSilencioKoffee_Backend.services.IRolService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RolServiceImpl implements IRolService {

    private final RolRepository rolRepository;

    @Override
    public List<Rol> findAll() {
        return rolRepository.findAll();
    }

    @Override
    public Optional<Rol> findById(Long id) {
        return rolRepository.findById(id);
    }

    @Override
    public Rol save(Rol rol) {
        return rolRepository.save(rol);
    }

    @Override
    public Optional<Rol> update(Long id, Rol rol) {
        return rolRepository.findById(id).map(existing -> {
            existing.setNombre(rol.getNombre());
            return rolRepository.save(existing);
        });
    }

    @Override
    public boolean delete(Long id) {
        if (!rolRepository.existsById(id)) return false;
        rolRepository.deleteById(id);
        return true;
    }
}
