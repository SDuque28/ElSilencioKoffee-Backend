package ElSilencioKoffee_Backend.repository;

import ElSilencioKoffee_Backend.entities.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByUsuarioId(Long userId, Pageable pageable);

    Optional<Order> findByIdAndUsuarioId(Long id, Long userId);
}
