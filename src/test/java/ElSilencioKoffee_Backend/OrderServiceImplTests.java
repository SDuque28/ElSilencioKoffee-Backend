package ElSilencioKoffee_Backend;

import ElSilencioKoffee_Backend.orders.dto.OrderCreateItemRequest;
import ElSilencioKoffee_Backend.orders.entities.Order;
import ElSilencioKoffee_Backend.orders.entities.OrderStatus;
import ElSilencioKoffee_Backend.orders.repositories.OrderRepository;
import ElSilencioKoffee_Backend.orders.services.IOrderService;
import ElSilencioKoffee_Backend.products.entities.Product;
import ElSilencioKoffee_Backend.users.entities.Usuario;
import ElSilencioKoffee_Backend.users.repositories.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

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
    private JdbcTemplate jdbcTemplate;

    @Test
    void createOrderBuildsTotalFromProductsAndPersistsDetails() {
        Usuario usuario = createUser("checkout-user", "checkout@example.com");

        Product productOne = new Product();
        productOne.setId(1L);
        productOne.setName("Ethiopian Yirgacheffe");
        productOne.setPrice(new BigDecimal("26.00"));
        persistProduct(productOne, 1, 1, 1, 1);

        Product productTwo = new Product();
        productTwo.setId(2L);
        productTwo.setName("Espresso Capsules");
        productTwo.setPrice(new BigDecimal("18.00"));
        persistProduct(productTwo, 2, 2, 2, 2);

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
        assertEquals(2L, order.getOrderDetails().get(1).getProduct().getId());
        assertEquals(new BigDecimal("1"), order.getOrderDetails().get(1).getQuantity());
        assertEquals(new BigDecimal("18.00"), order.getOrderDetails().get(1).getUnitPrice());
    }

    @Test
    void createOrderRejectsEmptyItemList() {
        assertThrows(
                IllegalArgumentException.class,
                () -> orderService.createOrder("checkout-user", List.of())
        );
    }

    @Test
    void createOrderRejectsMissingProductReferences() {
        Usuario usuario = createUser("missing-product-user", "missing-product@example.com");
        OrderCreateItemRequest item = new OrderCreateItemRequest();
        item.setProductId(999L);
        item.setQuantity(1);

        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> orderService.createOrder(usuario.getUsername(), List.of(item))
        );

        assertEquals("Product not found: 999", exception.getMessage());
    }

    @Test
    void createOrderRejectsNonPositiveQuantities() {
        OrderCreateItemRequest item = new OrderCreateItemRequest();
        item.setProductId(1L);
        item.setQuantity(0);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> orderService.createOrder("checkout-user", List.of(item))
        );

        assertEquals("Item quantity must be greater than 0", exception.getMessage());
    }

    @Test
    void createOrderRejectsInvalidProductIdValues() {
        OrderCreateItemRequest item = new OrderCreateItemRequest();
        item.setProductId(0L);
        item.setQuantity(1);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> orderService.createOrder("checkout-user", List.of(item))
        );

        assertEquals("Product ID must be greater than 0", exception.getMessage());
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
    void findOrderByIdReturnsAnyOrderForAdminScopedLookups() {
        Order order = createPersistedOrder(OrderStatus.NON_PAID);

        Order foundOrder = orderService.findOrderById(order.getId());

        assertEquals(order.getId(), foundOrder.getId());
        assertEquals(order.getUsuario().getId(), foundOrder.getUsuario().getId());
    }

    @Test
    void findOrderByIdReturnsPersistedLineItems() {
        Usuario usuario = createUser("history-user", "history@example.com");

        Product productOne = new Product();
        productOne.setId(1L);
        productOne.setName("Colombia Supremo");
        productOne.setPrice(new BigDecimal("24.00"));
        persistProduct(productOne, 1, 1, 1, 1);

        Product productTwo = new Product();
        productTwo.setId(2L);
        productTwo.setName("Kenya AA");
        productTwo.setPrice(new BigDecimal("28.00"));
        persistProduct(productTwo, 2, 2, 2, 2);

        OrderCreateItemRequest firstItem = new OrderCreateItemRequest();
        firstItem.setProductId(1L);
        firstItem.setQuantity(1);

        OrderCreateItemRequest secondItem = new OrderCreateItemRequest();
        secondItem.setProductId(2L);
        secondItem.setQuantity(3);

        Order created = orderService.createOrder(usuario.getUsername(), List.of(firstItem, secondItem));

        Order found = orderService.findOrderById(created.getId());

        assertEquals(2, found.getOrderDetails().size());
        assertEquals("Colombia Supremo", found.getOrderDetails().getFirst().getProduct().getName());
        assertEquals(new BigDecimal("24.00"), found.getOrderDetails().getFirst().getUnitPrice());
        assertEquals(new BigDecimal("108.00"), found.getTotalAmount());
    }

    @Test
    void findOrderByIdForUsernameRejectsOrdersOwnedByAnotherUser() {
        Usuario owner = new Usuario();
        owner.setUsername("owner-user");
        owner.setPassword("secret");
        owner.setEmail("owner@example.com");
        owner.setActivo(true);
        owner = usuarioRepository.save(owner);

        Usuario otherUser = new Usuario();
        otherUser.setUsername("other-user");
        otherUser.setPassword("secret");
        otherUser.setEmail("other@example.com");
        otherUser.setActivo(true);
        usuarioRepository.save(otherUser);

        Order order = new Order();
        order.setUsuario(owner);
        order.setStatus(OrderStatus.NON_PAID);
        order.setTotalAmount(new BigDecimal("18.00"));
        Order savedOrder = orderRepository.save(order);

        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> orderService.findOrderByIdForUsername(savedOrder.getId(), otherUser.getUsername())
        );

        assertEquals("Order not found: " + savedOrder.getId(), exception.getMessage());
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
        Usuario usuario = createUser(
                "status-user-" + status.name(),
                "status-" + status.name().toLowerCase() + "@example.com"
        );

        Order order = new Order();
        order.setUsuario(usuario);
        order.setStatus(status);
        order.setTotalAmount(new BigDecimal("18.00"));

        return orderRepository.save(order);
    }

    private Usuario createUser(String username, String email) {
        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setPassword("secret");
        usuario.setEmail(email);
        usuario.setActivo(true);
        return usuarioRepository.save(usuario);
    }

    private void persistProduct(
            Product product,
            int sectionId,
            int varietyId,
            int productionId,
            int presentationId
    ) {
        jdbcTemplate.update(
                "INSERT INTO sections (id_section, name, location, capacity) VALUES (?, ?, ?, ?)",
                sectionId,
                "Section " + sectionId,
                "Location " + sectionId,
                100
        );
        jdbcTemplate.update(
                "INSERT INTO varieties (id_variety, name, description) VALUES (?, ?, ?)",
                varietyId,
                "Variety " + varietyId,
                "Description " + varietyId
        );
        jdbcTemplate.update(
                """
                INSERT INTO production (id_production, id_section, date_collection, quantity_kg, id_variety)
                VALUES (?, ?, DATE '2026-01-01', ?, ?)
                """,
                productionId,
                sectionId,
                new BigDecimal("25.00"),
                varietyId
        );
        jdbcTemplate.update(
                "INSERT INTO product_presentations (id_presentation, name, description) VALUES (?, ?, ?)",
                presentationId,
                "Presentation " + presentationId,
                "Description " + presentationId
        );
        jdbcTemplate.update(
                """
                INSERT INTO products (id_product, name, price, id_presentation, id_production)
                VALUES (?, ?, ?, ?, ?)
                """,
                product.getId(),
                product.getName(),
                product.getPrice(),
                presentationId,
                productionId
        );
    }
}
