package ElSilencioKoffee_Backend;

import ElSilencioKoffee_Backend.inventory.entities.InventoryMovement;
import ElSilencioKoffee_Backend.inventory.entities.InventoryMovementType;
import ElSilencioKoffee_Backend.inventory.entities.InventoryReferenceType;
import ElSilencioKoffee_Backend.inventory.repositories.InventoryMovementRepository;
import ElSilencioKoffee_Backend.inventory.repositories.InventoryRepository;
import ElSilencioKoffee_Backend.orders.dto.OrderCreateItemRequest;
import ElSilencioKoffee_Backend.orders.entities.DeliveryOrder;
import ElSilencioKoffee_Backend.orders.entities.DeliveryStatus;
import ElSilencioKoffee_Backend.orders.entities.Order;
import ElSilencioKoffee_Backend.orders.entities.OrderDetail;
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
    private InventoryRepository inventoryRepository;

    @Autowired
    private InventoryMovementRepository inventoryMovementRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void createOrderBuildsTotalFromProductsAndPersistsDetails() {
        Usuario usuario = createUser("checkout-user", "checkout@example.com");

        Product productOne = createCatalogProduct(1L, "Ethiopian Yirgacheffe", "26.00");
        Product productTwo = createCatalogProduct(2L, "Espresso Capsules", "18.00");
        createInventory(productOne.getId(), 10);
        createInventory(productTwo.getId(), 10);

        OrderCreateItemRequest firstItem = new OrderCreateItemRequest();
        firstItem.setProductId(1L);
        firstItem.setQuantity(2);

        OrderCreateItemRequest secondItem = new OrderCreateItemRequest();
        secondItem.setProductId(2L);
        secondItem.setQuantity(1);

        Order order = orderService.createOrder(usuario.getUsername(), List.of(firstItem, secondItem));

        assertEquals(usuario.getId(), order.getUsuario().getId());
        assertEquals(OrderStatus.PENDING, order.getStatus());
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
    void createOrderRejectsInsufficientStock() {
        Usuario usuario = createUser("low-stock-user", "low-stock@example.com");
        Product product = createCatalogProduct(1L, "Limited Batch", "26.00");
        createInventory(product.getId(), 1);

        OrderCreateItemRequest item = new OrderCreateItemRequest();
        item.setProductId(product.getId());
        item.setQuantity(2);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> orderService.createOrder(usuario.getUsername(), List.of(item))
        );

        assertEquals(
                "Insufficient stock for product Limited Batch. Available: 1, requested: 2",
                exception.getMessage()
        );
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
    void updateStatusAllowsTransitionFromPendingToPaidAndConsumesStock() {
        Order order = createPersistedOrder(OrderStatus.PENDING, 5);

        Order updatedOrder = orderService.updateStatus(
                order.getId(),
                OrderStatus.PAID,
                order.getUsuario().getUsername()
        );

        assertEquals(OrderStatus.PAID, updatedOrder.getStatus());
        assertEquals(4, inventoryRepository.findByProductId(1L).orElseThrow().getStockQuantity());

        List<InventoryMovement> movements =
                inventoryMovementRepository.findByProductIdOrderByCreatedAtDescIdDesc(1L);
        assertEquals(1, movements.size());
        assertEquals(InventoryMovementType.OUT, movements.getFirst().getMovementType());
        assertEquals(InventoryReferenceType.ORDER, movements.getFirst().getReferenceType());
        assertEquals(order.getId(), movements.getFirst().getReferenceId());
    }

    @Test
    void updateStatusRejectsSameStatusTransition() {
        Order order = createPersistedOrder(OrderStatus.PENDING, 5);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> orderService.updateStatus(order.getId(), OrderStatus.PENDING, order.getUsuario().getUsername())
        );

        assertEquals("Order is already in status: PENDING", exception.getMessage());
    }

    @Test
    void updateStatusRejectsChangesFromTerminalPaidStatus() {
        Order order = createPersistedOrder(OrderStatus.PAID, 5);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> orderService.updateStatus(order.getId(), OrderStatus.PENDING, order.getUsuario().getUsername())
        );

        assertEquals("Invalid order status transition: PAID -> PENDING", exception.getMessage());
    }

    @Test
    void updateDeliveryStatusAllowsShipmentProgressForPaidOrders() {
        Order order = createPersistedOrderWithDelivery(OrderStatus.PAID, DeliveryStatus.PENDING, 5);

        Order updatedOrder = orderService.updateDeliveryStatus(order.getId(), DeliveryStatus.OUT_FOR_SHIPMENT);

        assertEquals(DeliveryStatus.OUT_FOR_SHIPMENT, updatedOrder.getDeliveryOrder().getStatus());
    }

    @Test
    void updateDeliveryStatusRejectsUnpaidOrders() {
        Order order = createPersistedOrderWithDelivery(OrderStatus.PENDING, DeliveryStatus.PENDING, 5);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> orderService.updateDeliveryStatus(order.getId(), DeliveryStatus.OUT_FOR_SHIPMENT)
        );

        assertEquals("Delivery status can only be updated for paid orders", exception.getMessage());
    }

    @Test
    void payOrderRejectsOrdersOwnedByAnotherUser() {
        Order order = createPersistedOrder(OrderStatus.PENDING, 5);
        createUser("someone-else", "someone-else@example.com");

        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> orderService.payOrder(order.getId(), "someone-else", false)
        );

        assertEquals("Order not found: " + order.getId(), exception.getMessage());
    }

    @Test
    void findOrderByIdReturnsAnyOrderForAdminScopedLookups() {
        Order order = createPersistedOrder(OrderStatus.PENDING, 5);

        Order foundOrder = orderService.findOrderById(order.getId());

        assertEquals(order.getId(), foundOrder.getId());
        assertEquals(order.getUsuario().getId(), foundOrder.getUsuario().getId());
    }

    @Test
    void findOrderByIdReturnsPersistedLineItems() {
        Usuario usuario = createUser("history-user", "history@example.com");

        Product productOne = createCatalogProduct(1L, "Colombia Supremo", "24.00");
        Product productTwo = createCatalogProduct(2L, "Kenya AA", "28.00");
        createInventory(productOne.getId(), 10);
        createInventory(productTwo.getId(), 10);

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
        Usuario owner = createUser("owner-user", "owner@example.com");
        Usuario otherUser = createUser("other-user", "other@example.com");

        Order order = new Order();
        order.setUsuario(owner);
        order.setStatus(OrderStatus.PENDING);
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

    private Order createPersistedOrder(OrderStatus status, int stockQuantity) {
        Usuario usuario = createUser(
                "status-user-" + status.name(),
                "status-" + status.name().toLowerCase() + "@example.com"
        );
        Product product = createCatalogProduct(1L, "Checkout Product", "18.00");
        createInventory(product.getId(), stockQuantity);

        Order order = new Order();
        order.setUsuario(usuario);
        order.setStatus(status);
        order.setTotalAmount(new BigDecimal("18.00"));
        order.addOrderDetail(createOrderDetail(product, "18.00", 1));

        return orderRepository.save(order);
    }

    private Order createPersistedOrderWithDelivery(OrderStatus status, DeliveryStatus deliveryStatus, int stockQuantity) {
        Order order = createPersistedOrder(status, stockQuantity);
        DeliveryOrder deliveryOrder = new DeliveryOrder();
        deliveryOrder.setStatus(deliveryStatus);
        order.setDeliveryOrder(deliveryOrder);
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

    private Product createCatalogProduct(Long productId, String name, String price) {
        Product product = new Product();
        product.setId(productId);
        product.setName(name);
        product.setPrice(new BigDecimal(price));
        persistProduct(product, productId.intValue(), productId.intValue(), productId.intValue(), productId.intValue());
        return product;
    }

    private OrderDetail createOrderDetail(Product product, String price, int quantity) {
        OrderDetail detail = new OrderDetail();
        detail.setProduct(product);
        detail.setQuantity(BigDecimal.valueOf(quantity));
        detail.setUnitPrice(new BigDecimal(price));
        return detail;
    }

    private void createInventory(Long productId, int stockQuantity) {
        jdbcTemplate.update(
                "INSERT INTO inventory (id_product, stock_quantity) VALUES (?, ?)",
                productId,
                stockQuantity
        );
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
