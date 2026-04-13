package ElSilencioKoffee_Backend.services;

import ElSilencioKoffee_Backend.entities.Order;
import ElSilencioKoffee_Backend.entities.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface IOrderService {

    Order createOrder(String username, BigDecimal totalAmount);

    List<Order> findAllOrders();

    Page<Order> findAllOrders(Pageable pageable);

    List<Order> findOrdersByUsername(String username);

    Page<Order> findOrdersByUsername(String username, Pageable pageable);

    Order findOrderByIdForUsername(Long id, String username);

    Order updateStatus(Long id, OrderStatus status);
}
