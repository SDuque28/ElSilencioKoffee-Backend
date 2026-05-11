package ElSilencioKoffee_Backend.cart.repositories;

import ElSilencioKoffee_Backend.cart.entities.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);

    Optional<CartItem> findByIdAndCartUsuarioUsername(Long id, String username);
}
