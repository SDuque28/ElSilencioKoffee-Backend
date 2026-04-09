package ElSilencioKoffee_Backend.services;

import ElSilencioKoffee_Backend.entities.Rol;

import java.util.List;
import java.util.Optional;

public interface IRolService {

    List<Rol> findAll();

    Optional<Rol> findById(Long id);

    Rol save(Rol rol);

    Optional<Rol> update(Long id, Rol rol);

    boolean delete(Long id);
}
