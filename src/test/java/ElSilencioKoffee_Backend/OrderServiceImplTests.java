package ElSilencioKoffee_Backend;

import ElSilencioKoffee_Backend.dto.OrderCreateItemRequest;
import ElSilencioKoffee_Backend.entities.Order;
import ElSilencioKoffee_Backend.entities.OrderStatus;
import ElSilencioKoffee_Backend.entities.Product;
import ElSilencioKoffee_Backend.entities.Usuario;
import ElSilencioKoffee_Backend.repository.OrderRepository;
import ElSilencioKoffee_Backend.repository.ProductRepository;
import ElSilencioKoffee_Backend.repository.UsuarioRepository;
import ElSilencioKoffee_Backend.services.IOrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class OrderServiceImplTests {

    @Autowired
    private IOrderService orderService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void createOrderBuildsTotalFromProductsAndPersistsDetails() {
        Usuario usuario = new Usuario();
        usuario.setUsername("checkout-user");
        usuario.setPassword("secret");
        usuario.setEmail("checkout@example.com");
        usuario.setActivo(true);
        usuario = usuarioRepository.save(usuario);

        Product productOne = new Product();
        productOne.setId(1L);
        productOne.setName("Ethiopian Yirgacheffe");
        productOne.setPrice(new BigDecimal("26.00"));
        productRepository.save(productOne);

        Product productTwo = new Product();
        productTwo.setId(2L);
        productTwo.setName("Espresso Capsules");
        productTwo.setPrice(new BigDecimal("18.00"));
        productRepository.save(productTwo);

        OrderCreateItemRequest firstItem = new OrderCreateItemRequest();
        firstItem.setProductId(1L);
        firstItem.setQuantity(2);

        OrderCreateItemRequest secondItem = new OrderCreateItemRequest();
        secondItem.setProductId(2L);
        secondItem.setQuantity(1);

        Order order = orderService.createOrder(usuario.getUsername(), List.of(firstItem, secondItem));

        assertEquals(usuario.getId(), order.getUsuario().getId());
        assertEquals(OrderStatus.NON_PAID, order.getStatus());
        assertEquals(new BigDecimal("70.00"), order.getTotalAmount());
        assertEquals(2, order.getOrderDetails().size());
        assertEquals(1L, order.getOrderDetails().getFirst().getProduct().getId());
        assertEquals(new BigDecimal("2"), order.getOrderDetails().getFirst().getQuantity());
        assertEquals(new BigDecimal("26.00"), order.getOrderDetails().getFirst().getUnitPrice());
    }

    @Test
    void createOrderRejectsEmptyItemList() {
        assertThrows(
                IllegalArgumentException.class,
                () -> orderService.createOrder("checkout-user", List.of())
        );
    }

    @Test
    void updateStatusAllowsTransitionFromNonPaidToPaid() {
        Order order = createPersistedOrder(OrderStatus.NON_PAID);

        Order updatedOrder = orderService.updateStatus(order.getId(), OrderStatus.PAID);

        assertEquals(OrderStatus.PAID, updatedOrder.getStatus());
    }

    @Test
    void updateStatusRejectsSameStatusTransition() {
        Order order = createPersistedOrder(OrderStatus.NON_PAID);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> orderService.updateStatus(order.getId(), OrderStatus.NON_PAID)
        );

        assertEquals("Order is already in status: NON PAID", exception.getMessage());
    }

    @Test
    void updateStatusRejectsChangesFromTerminalPaidStatus() {
        Order order = createPersistedOrder(OrderStatus.PAID);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> orderService.updateStatus(order.getId(), OrderStatus.NON_PAID)
        );

        assertEquals("Invalid order status transition: PAID -> NON PAID", exception.getMessage());
    }

    @Test
    void orderStatusRejectsUnknownValues() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> OrderStatus.fromValue("REFUNDED")
        );

        assertTrue(exception.getMessage().contains("Invalid order status: REFUNDED"));
    }

    private Order createPersistedOrder(OrderStatus status) {
        Usuario usuario = new Usuario();
        usuario.setUsername("status-user-" + status.name());
        usuario.setPassword("secret");
        usuario.setEmail("status-" + status.name().toLowerCase() + "@example.com");
        usuario.setActivo(true);
        usuario = usuarioRepository.save(usuario);

        Order order = new Order();
        order.setUsuario(usuario);
        order.setStatus(status);
        order.setTotalAmount(new BigDecimal("18.00"));

        return orderRepository.save(order);
    }
}
