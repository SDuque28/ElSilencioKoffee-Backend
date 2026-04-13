package ElSilencioKoffee_Backend.services.impl;

import ElSilencioKoffee_Backend.entities.Order;
import ElSilencioKoffee_Backend.entities.OrderStatus;
import ElSilencioKoffee_Backend.entities.Usuario;
import ElSilencioKoffee_Backend.repository.OrderRepository;
import ElSilencioKoffee_Backend.repository.UsuarioRepository;
import ElSilencioKoffee_Backend.services.IOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements IOrderService {

    private final OrderRepository orderRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional
    public Order createOrder(String username, BigDecimal totalAmount) {
        validateTotalAmount(totalAmount);

        Usuario usuario = findUserByUsername(username);

        Order order = new Order();
        order.setUsuario(usuario);
        order.setTotalAmount(totalAmount);
        order.setStatus(OrderStatus.NON_PAID);

        return orderRepository.save(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findAllOrders() {
        return orderRepository.findAll(Sort.by(Sort.Direction.DESC, "orderDate", "id"));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Order> findAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findOrdersByUsername(String username) {
        Usuario usuario = findUserByUsername(username);
        return orderRepository.findAllByUsuarioId(
                usuario.getId(),
                Sort.by(Sort.Direction.DESC, "orderDate", "id")
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Order> findOrdersByUsername(String username, Pageable pageable) {
        Usuario usuario = findUserByUsername(username);
        return orderRepository.findByUsuarioId(usuario.getId(), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Order findOrderByIdForUsername(Long id, String username) {
        Usuario usuario = findUserByUsername(username);
        return orderRepository.findByIdAndUsuarioId(id, usuario.getId())
                .orElseThrow(() -> new NoSuchElementException("Order not found: " + id));
    }

    @Override
    @Transactional
    public Order updateStatus(Long id, OrderStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Order status is required");
        }

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Order not found: " + id));

        order.setStatus(status);
        return orderRepository.save(order);
    }

    private Usuario findUserByUsername(String username) {
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + username));
    }

    private void validateTotalAmount(BigDecimal totalAmount) {
        if (totalAmount == null) {
            throw new IllegalArgumentException("Total amount is required");
        }
        if (totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Total amount must be greater than 0");
        }
    }
}
