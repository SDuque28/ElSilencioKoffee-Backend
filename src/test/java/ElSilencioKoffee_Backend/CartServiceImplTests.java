package ElSilencioKoffee_Backend;

import ElSilencioKoffee_Backend.cart.dto.AddCartItemRequest;
import ElSilencioKoffee_Backend.cart.dto.CartResponse;
import ElSilencioKoffee_Backend.cart.dto.UpdateCartItemRequest;
import ElSilencioKoffee_Backend.cart.entities.Cart;
import ElSilencioKoffee_Backend.cart.entities.CartItem;
import ElSilencioKoffee_Backend.cart.repositories.CartItemRepository;
import ElSilencioKoffee_Backend.cart.repositories.CartRepository;
import ElSilencioKoffee_Backend.cart.services.ICartService;
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
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class CartServiceImplTests {

    @Autowired
    private ICartService cartService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void authenticatedUserCanGetOwnCart() {
        Usuario usuario = createUser("cart-user");

        CartResponse response = cartService.getCartForUsername(usuario.getUsername());

        assertEquals(usuario.getId(), response.getUserId());
        assertEquals(0, response.getTotalItems());
        assertEquals(BigDecimal.ZERO, response.getTotalAmount());
        assertTrue(response.getItems().isEmpty());
    }

    @Test
    void addProductToCartCreatesPersistentCartAndItem() {
        Usuario usuario = createUser("cart-create-user");
        Product product = createProduct(1, "Cart Coffee", "26.00");

        CartResponse response = cartService.addItem(usuario.getUsername(), addRequest(product.getId(), 2));

        assertNotNull(response.getId());
        assertEquals(2, response.getTotalItems());
        assertEquals(new BigDecimal("52.00"), response.getTotalAmount());
        assertEquals(1, response.getItems().size());
        assertEquals(product.getId(), response.getItems().getFirst().getProductId());
        assertTrue(cartRepository.findByUsuarioUsername(usuario.getUsername()).isPresent());
    }

    @Test
    void addSameProductTwiceIncrementsQuantity() {
        Usuario usuario = createUser("cart-repeat-user");
        Product product = createProduct(1, "Repeat Coffee", "18.00");

        cartService.addItem(usuario.getUsername(), addRequest(product.getId(), 1));
        CartResponse response = cartService.addItem(usuario.getUsername(), addRequest(product.getId(), 3));

        assertEquals(4, response.getTotalItems());
        assertEquals(1, response.getItems().size());
        assertEquals(4, response.getItems().getFirst().getQuantity());
        assertEquals(new BigDecimal("72.00"), response.getTotalAmount());
    }

    @Test
    void updateItemQuantityChangesCartTotals() {
        Usuario usuario = createUser("cart-update-user");
        Product product = createProduct(1, "Update Coffee", "12.50");
        CartResponse created = cartService.addItem(usuario.getUsername(), addRequest(product.getId(), 1));

        UpdateCartItemRequest request = new UpdateCartItemRequest();
        request.setQuantity(5);

        CartResponse updated = cartService.updateItem(
                usuario.getUsername(),
                created.getItems().getFirst().getId(),
                request
        );

        assertEquals(5, updated.getTotalItems());
        assertEquals(5, updated.getItems().getFirst().getQuantity());
        assertEquals(new BigDecimal("62.50"), updated.getTotalAmount());
    }

    @Test
    void rejectInvalidQuantity() {
        Usuario usuario = createUser("cart-invalid-user");
        Product product = createProduct(1, "Invalid Coffee", "15.00");

        IllegalArgumentException addException = assertThrows(
                IllegalArgumentException.class,
                () -> cartService.addItem(usuario.getUsername(), addRequest(product.getId(), 0))
        );
        assertEquals("Quantity must be greater than 0", addException.getMessage());

        CartResponse created = cartService.addItem(usuario.getUsername(), addRequest(product.getId(), 1));
        UpdateCartItemRequest updateRequest = new UpdateCartItemRequest();
        updateRequest.setQuantity(-2);

        IllegalArgumentException updateException = assertThrows(
                IllegalArgumentException.class,
                () -> cartService.updateItem(usuario.getUsername(), created.getItems().getFirst().getId(), updateRequest)
        );
        assertEquals("Quantity must be greater than 0", updateException.getMessage());
    }

    @Test
    void removeItemDeletesOnlyOwnedCartItem() {
        Usuario usuario = createUser("cart-remove-user");
        Product product = createProduct(1, "Remove Coffee", "11.00");
        CartResponse created = cartService.addItem(usuario.getUsername(), addRequest(product.getId(), 2));

        CartResponse updated = cartService.removeItem(usuario.getUsername(), created.getItems().getFirst().getId());

        assertEquals(0, updated.getTotalItems());
        assertTrue(updated.getItems().isEmpty());
        assertEquals(BigDecimal.ZERO, updated.getTotalAmount());
        assertEquals(0L, cartItemRepository.count());
    }

    @Test
    void clearCartDeletesPersistedCartState() {
        Usuario usuario = createUser("cart-clear-user");
        Product product = createProduct(1, "Clear Coffee", "9.00");
        cartService.addItem(usuario.getUsername(), addRequest(product.getId(), 4));

        CartResponse cleared = cartService.clearCart(usuario.getUsername());

        assertEquals(usuario.getId(), cleared.getUserId());
        assertEquals(0, cleared.getTotalItems());
        assertTrue(cleared.getItems().isEmpty());
        assertFalse(cartRepository.findByUsuarioUsername(usuario.getUsername()).isPresent());
    }

    @Test
    void rejectModifyingItemNotOwnedByUser() {
        Usuario owner = createUser("cart-owner");
        Usuario other = createUser("cart-other");
        Product product = createProduct(1, "Owner Coffee", "13.00");
        CartResponse created = cartService.addItem(owner.getUsername(), addRequest(product.getId(), 1));

        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> cartService.removeItem(other.getUsername(), created.getItems().getFirst().getId())
        );

        assertEquals("Cart item not found: " + created.getItems().getFirst().getId(), exception.getMessage());
    }

    private Usuario createUser(String username) {
        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setPassword("secret");
        usuario.setEmail(username + "@example.com");
        usuario.setActivo(true);
        return usuarioRepository.save(usuario);
    }

    private Product createProduct(int key, String name, String price) {
        seedCatalogReferences(key);

        Product product = new Product();
        product.setName(name);
        product.setPrice(new BigDecimal(price));
        product.setPresentationId((long) key);
        product.setProductionId((long) key);
        return productRepository.save(product);
    }

    private AddCartItemRequest addRequest(Long productId, Integer quantity) {
        AddCartItemRequest request = new AddCartItemRequest();
        request.setProductId(productId);
        request.setQuantity(quantity);
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
