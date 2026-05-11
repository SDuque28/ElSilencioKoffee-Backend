package ElSilencioKoffee_Backend.users.repositories;

import ElSilencioKoffee_Backend.users.entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsername(String username);

    Optional<Usuario> findByEmail(String email);

    Optional<Usuario> findByUsernameAndEmail(String username, String email);

    boolean existsByUsernameAndIdNot(String username, Long id);

    boolean existsByEmailAndIdNot(String email, Long id);

    @Query("SELECT u FROM Usuario u LEFT JOIN FETCH u.usuariosRoles ur LEFT JOIN FETCH ur.rol WHERE u.username = :username")
    Optional<Usuario> findByUsernameWithRoles(@Param("username") String username);
}

