package ElSilencioKoffee_Backend.cart.repositories;

import ElSilencioKoffee_Backend.cart.entities.Cart;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    @EntityGraph(attributePaths = {"usuario", "items", "items.product"})
    Optional<Cart> findByUsuarioUsername(String username);
}
