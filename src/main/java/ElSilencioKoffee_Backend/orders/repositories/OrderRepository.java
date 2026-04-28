package ElSilencioKoffee_Backend.orders.repositories;

import ElSilencioKoffee_Backend.orders.entities.Order;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Override
    @EntityGraph(attributePaths = {"usuario", "orderDetails", "orderDetails.product"})
    Page<Order> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"usuario", "orderDetails", "orderDetails.product"})
    List<Order> findAll(Sort sort);

    @EntityGraph(attributePaths = {"usuario", "orderDetails", "orderDetails.product"})
    Page<Order> findByUsuarioId(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"usuario", "orderDetails", "orderDetails.product"})
    Optional<Order> findByIdAndUsuarioId(Long id, Long userId);

    @EntityGraph(attributePaths = {"usuario", "orderDetails", "orderDetails.product"})
    Optional<Order> findById(Long id);

    @EntityGraph(attributePaths = {"usuario", "orderDetails", "orderDetails.product"})
    List<Order> findAllByUsuarioId(Long userId, Sort sort);
}

