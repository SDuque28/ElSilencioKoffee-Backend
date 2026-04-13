package ElSilencioKoffee_Backend.repository;

import ElSilencioKoffee_Backend.entities.Order;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Override
    @EntityGraph(attributePaths = "usuario")
    Page<Order> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = "usuario")
    List<Order> findAll(Sort sort);

    @EntityGraph(attributePaths = "usuario")
    Page<Order> findByUsuarioId(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = "usuario")
    Optional<Order> findByIdAndUsuarioId(Long id, Long userId);

    @EntityGraph(attributePaths = "usuario")
    Optional<Order> findById(Long id);

    @EntityGraph(attributePaths = "usuario")
    List<Order> findAllByUsuarioId(Long userId, Sort sort);
}
