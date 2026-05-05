package ElSilencioKoffee_Backend;

import ElSilencioKoffee_Backend.cart.dto.AddCartItemRequest;
import ElSilencioKoffee_Backend.cart.repositories.CartRepository;
import ElSilencioKoffee_Backend.cart.services.ICartService;
import ElSilencioKoffee_Backend.checkout.dto.CheckoutRequest;
import ElSilencioKoffee_Backend.checkout.dto.PaymentSimulationRequest;
import ElSilencioKoffee_Backend.checkout.dto.ShippingInformationRequest;
import ElSilencioKoffee_Backend.checkout.services.ICheckoutService;
import ElSilencioKoffee_Backend.inventory.repositories.InventoryRepository;
import ElSilencioKoffee_Backend.orders.entities.DeliveryStatus;
import ElSilencioKoffee_Backend.orders.entities.Order;
import ElSilencioKoffee_Backend.orders.entities.OrderStatus;
import ElSilencioKoffee_Backend.orders.repositories.OrderRepository;
import ElSilencioKoffee_Backend.products.entities.Product;
import ElSilencioKoffee_Backend.products.repositories.ProductRepository;
import ElSilencioKoffee_Backend.users.entities.Usuario;
import ElSilencioKoffee_Backend.users.repositories.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class CheckoutServiceImplTests {

    @Autowired
    private ICheckoutService checkoutService;

    @Autowired
    private ICartService cartService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void authenticatedCheckoutCreatesPaidOrderPaymentDeliveryAndClearsCart() {
        Usuario usuario = createUser("checkout-user", "checkout-user@example.com");
        Product coffee = createProduct(1, "Checkout Coffee", "26.00", 8);
        Product capsules = createProduct(2, "Checkout Capsules", "18.00", 6);

        cartService.addItem(usuario.getUsername(), addRequest(coffee.getId(), 2));
        cartService.addItem(usuario.getUsername(), addRequest(capsules.getId(), 1));

        Order order = checkoutService.checkout(usuario.getUsername(), checkoutRequest("12/2099"));

        assertNotNull(order.getId());
        assertEquals(OrderStatus.PAID, order.getStatus());
        assertEquals(new BigDecimal("70.00"), order.getTotalAmount());
        assertEquals(2, order.getOrderDetails().size());
        assertEquals("Bogota", order.getShippingInformation().getCity());
        assertEquals("**** **** **** 4242", order.getPayment().getMaskedCardNumber());
        assertEquals("APPROVED", order.getPayment().getStatus().toJson());
        assertTrue(order.getPayment().getTransactionReference().startsWith("SIM-"));
        assertEquals(DeliveryStatus.OUT_FOR_SHIPMENT, order.getDeliveryOrder().getStatus());
        assertEquals(6, inventoryRepository.findByProductId(coffee.getId()).orElseThrow().getStockQuantity());
        assertEquals(5, inventoryRepository.findByProductId(capsules.getId()).orElseThrow().getStockQuantity());
        assertTrue(cartRepository.findByUsuarioUsername(usuario.getUsername()).isPresent());
        assertTrue(cartRepository.findByUsuarioUsername(usuario.getUsername()).orElseThrow().getItems().isEmpty());

        Integer paymentColumnCount = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM INFORMATION_SCHEMA.COLUMNS
                WHERE TABLE_NAME = 'ORDER_PAYMENTS'
                  AND COLUMN_NAME IN ('CARD_NUMBER', 'CVV', 'CARDHOLDER_NAME')
                """,
                Integer.class
        );
        assertEquals(0, paymentColumnCount);
    }

    @Test
    void checkoutRejectsUnauthenticatedUsersWithoutPersistedCart() {
        assertThrows(
                NoSuchElementException.class,
                () -> checkoutService.checkout("missing-user", checkoutRequest("12/2099"))
        );
    }

    @Test
    void checkoutRejectsEmptyCart() {
        Usuario usuario = createUser("empty-cart-user", "empty-cart-user@example.com");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> checkoutService.checkout(usuario.getUsername(), checkoutRequest("12/2099"))
        );

        assertEquals("Cart is empty.", exception.getMessage());
    }

    @Test
    void checkoutRejectsInsufficientStock() {
        Usuario usuario = createUser("stock-user", "stock-user@example.com");
        Product coffee = createProduct(3, "Stock Coffee", "20.00", 1);
        cartService.addItem(usuario.getUsername(), addRequest(coffee.getId(), 1));
        var inventory = inventoryRepository.findByProductId(coffee.getId()).orElseThrow();
        inventory.setStockQuantity(0);
        inventoryRepository.save(inventory);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> checkoutService.checkout(usuario.getUsername(), checkoutRequest("12/2099"))
        );

        assertTrue(exception.getMessage().contains("Insufficient stock for product Stock Coffee"));
    }

    @Test
    void checkoutRejectsInvalidCardNumber() {
        Usuario usuario = createUser("invalid-card-user", "invalid-card-user@example.com");
        Product coffee = createProduct(4, "Invalid Card Coffee", "20.00", 3);
        cartService.addItem(usuario.getUsername(), addRequest(coffee.getId(), 1));

        CheckoutRequest request = checkoutRequest("12/2099");
        request.getPayment().setCardNumber("1234");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> checkoutService.checkout(usuario.getUsername(), request)
        );

        assertEquals("Card number must contain between 13 and 19 digits", exception.getMessage());
    }

    @Test
    void checkoutRejectsExpiredCard() {
        Usuario usuario = createUser("expired-card-user", "expired-card-user@example.com");
        Product coffee = createProduct(5, "Expired Card Coffee", "20.00", 3);
        cartService.addItem(usuario.getUsername(), addRequest(coffee.getId(), 1));

        String expiredDate = LocalDate.now().minusMonths(1).format(DateTimeFormatter.ofPattern("MM/yy"));
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> checkoutService.checkout(usuario.getUsername(), checkoutRequest(expiredDate))
        );

        assertEquals("Card is expired", exception.getMessage());
    }

    private Usuario createUser(String username, String email) {
        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setPassword("secret");
        usuario.setEmail(email);
        usuario.setActivo(true);
        return usuarioRepository.save(usuario);
    }

    private Product createProduct(int key, String name, String price, int stockQuantity) {
        seedCatalogReferences(key);

        Product product = new Product();
        product.setName(name);
        product.setPrice(new BigDecimal(price));
        product.setPresentationId(key);
        product.setProductionId(key);
        Product savedProduct = productRepository.save(product);
        jdbcTemplate.update(
                "INSERT INTO inventory (id_product, stock_quantity) VALUES (?, ?)",
                savedProduct.getId(),
                stockQuantity
        );
        return savedProduct;
    }

    private AddCartItemRequest addRequest(Long productId, Integer quantity) {
        AddCartItemRequest request = new AddCartItemRequest();
        request.setProductId(productId);
        request.setQuantity(quantity);
        return request;
    }

    private CheckoutRequest checkoutRequest(String expirationDate) {
        ShippingInformationRequest shipping = new ShippingInformationRequest();
        shipping.setAddress("Street 123 # 45-67");
        shipping.setCountry("Colombia");
        shipping.setCity("Bogota");
        shipping.setNeighborhood("Chapinero");
        shipping.setReferenceDetails("Blue door");

        PaymentSimulationRequest payment = new PaymentSimulationRequest();
        payment.setPaymentMethod(ElSilencioKoffee_Backend.orders.entities.PaymentMethod.CREDIT_CARD);
        payment.setCardholderName("Test Buyer");
        payment.setCardNumber("4242424242424242");
        payment.setExpirationDate(expirationDate);
        payment.setCvv("123");

        CheckoutRequest request = new CheckoutRequest();
        request.setShippingInformation(shipping);
        request.setPayment(payment);
        request.setNotes("Leave the package with the receptionist.");
        return request;
    }

    private void seedCatalogReferences(int key) {
        jdbcTemplate.update(
                "INSERT INTO sections (id_section, name, location, capacity) VALUES (?, ?, ?, ?)",
                key,
                "Section " + key,
                "Location " + key,
                100
        );
        jdbcTemplate.update(
                "INSERT INTO varieties (id_variety, name, description) VALUES (?, ?, ?)",
                key,
                "Variety " + key,
                "Description " + key
        );
        jdbcTemplate.update(
                """
                INSERT INTO production (id_production, id_section, date_collection, quantity_kg, id_variety)
                VALUES (?, ?, DATE '2026-01-01', ?, ?)
                """,
                key,
                key,
                new BigDecimal("30.00"),
                key
        );
        jdbcTemplate.update(
                "INSERT INTO product_presentations (id_presentation, name, description) VALUES (?, ?, ?)",
                key,
                "Presentation " + key,
                "Description " + key
        );
    }
}
