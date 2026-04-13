package ElSilencioKoffee_Backend.services;

import ElSilencioKoffee_Backend.entities.Order;
import ElSilencioKoffee_Backend.entities.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IUsuarioService {

    List<Usuario> findAll();

    Page<Usuario> findAll(Pageable pageable);

    Usuario findById(Long id);

    Usuario update(Long id, String username, String email, Boolean activo);

    void softDelete(Long id);

    List<Order> findOrdersByUserId(Long id);
}
