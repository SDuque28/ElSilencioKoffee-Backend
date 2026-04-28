package ElSilencioKoffee_Backend;

import ElSilencioKoffee_Backend.inventory.dto.InventoryMovementCreateRequest;
import ElSilencioKoffee_Backend.inventory.dto.InventoryMovementResponse;
import ElSilencioKoffee_Backend.inventory.dto.InventoryResponse;
import ElSilencioKoffee_Backend.inventory.dto.StockAdjustmentRequest;
import ElSilencioKoffee_Backend.inventory.entities.Inventory;
import ElSilencioKoffee_Backend.inventory.entities.InventoryMovement;
import ElSilencioKoffee_Backend.inventory.entities.InventoryMovementType;
import ElSilencioKoffee_Backend.inventory.entities.InventoryReferenceType;
import ElSilencioKoffee_Backend.inventory.repositories.InventoryMovementRepository;
import ElSilencioKoffee_Backend.inventory.repositories.InventoryRepository;
import ElSilencioKoffee_Backend.inventory.services.IInventoryService;
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
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class InventoryServiceImplTests {

    @Autowired
    private IInventoryService inventoryService;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private InventoryMovementRepository inventoryMovementRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void listInventoryRecordsReturnsPersistedRows() {
        Product firstProduct = createProduct(1, "Geisha");
        Product secondProduct = createProduct(2, "Bourbon");
        createInventory(firstProduct, 8);
        createInventory(secondProduct, 3);

        List<InventoryResponse> inventory = inventoryService.findAll();

        assertEquals(2, inventory.size());
        assertEquals(firstProduct.getId(), inventory.getFirst().getProductId());
        assertEquals(8, inventory.getFirst().getStockQuantity());
        assertEquals(secondProduct.getId(), inventory.get(1).getProductId());
        assertEquals(3, inventory.get(1).getStockQuantity());
    }

    @Test
    void getInventoryByProductCreatesMissingRecord() {
        Product product = createProduct(1, "Pacamara");

        InventoryResponse response = inventoryService.findByProductId(product.getId());

        assertNotNullInventory(response.getId());
        assertEquals(product.getId(), response.getProductId());
        assertEquals("Pacamara", response.getProductName());
        assertEquals(0, response.getStockQuantity());
        assertTrue(inventoryRepository.findByProductId(product.getId()).isPresent());
    }

    @Test
    void getInventoryByIdReturnsPersistedRecord() {
        Product product = createProduct(1, "Castillo");
        Inventory inventory = createInventory(product, 11);

        InventoryResponse response = inventoryService.findById(inventory.getId());

        assertEquals(inventory.getId(), response.getId());
        assertEquals(product.getId(), response.getProductId());
        assertEquals(11, response.getStockQuantity());
    }

    @Test
    void increaseStockCreatesMovementRecord() {
        Product product = createProduct(1, "Typica");
        Inventory inventory = createInventory(product, 4);
        createUser("admin-increase", "increase@example.com");

        InventoryResponse response = inventoryService.increaseStock(
                inventory.getId(),
                stockRequest(6, null, null),
                "admin-increase"
        );

        assertEquals(10, response.getStockQuantity());
        List<InventoryMovement> movements = inventoryMovementRepository.findByProductIdOrderByCreatedAtDescIdDesc(product.getId());
        assertEquals(1, movements.size());
        assertEquals(InventoryMovementType.IN, movements.getFirst().getMovementType());
        assertEquals(new BigDecimal("6.00"), movements.getFirst().getQuantity());
        assertEquals(InventoryReferenceType.MANUAL, movements.getFirst().getReferenceType());
        assertEquals("admin-increase", movements.getFirst().getCreatedBy().getUsername());
    }

    @Test
    void decreaseStockCreatesMovementRecord() {
        Product product = createProduct(1, "Caturra");
        Inventory inventory = createInventory(product, 12);
        createUser("admin-decrease", "decrease@example.com");

        InventoryResponse response = inventoryService.decreaseStock(
                inventory.getId(),
                stockRequest(5, 77L, InventoryReferenceType.ORDER),
                "admin-decrease"
        );

        assertEquals(7, response.getStockQuantity());
        List<InventoryMovementResponse> movements = inventoryService.findMovements(inventory.getId());
        assertEquals(1, movements.size());
        assertEquals(InventoryMovementType.OUT, movements.getFirst().getMovementType());
        assertEquals(new BigDecimal("5.00"), movements.getFirst().getQuantity());
        assertEquals(77L, movements.getFirst().getReferenceId());
        assertEquals(InventoryReferenceType.ORDER, movements.getFirst().getReferenceType());
    }

    @Test
    void rejectDecreaseBelowZero() {
        Product product = createProduct(1, "Maragogipe");
        Inventory inventory = createInventory(product, 2);
        createUser("admin-stock", "stock@example.com");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> inventoryService.decreaseStock(
                        inventory.getId(),
                        stockRequest(3, null, null),
                        "admin-stock"
                )
        );

        assertEquals("Stock cannot go below zero", exception.getMessage());
        assertTrue(inventoryMovementRepository.findByProductIdOrderByCreatedAtDescIdDesc(product.getId()).isEmpty());
    }

    @Test
    void rejectInvalidQuantity() {
        Product product = createProduct(1, "Java");
        Inventory inventory = createInventory(product, 9);
        createUser("admin-quantity", "quantity@example.com");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> inventoryService.increaseStock(
                        inventory.getId(),
                        stockRequest(0, null, null),
                        "admin-quantity"
                )
        );

        assertEquals("Quantity must be greater than 0", exception.getMessage());
    }

    @Test
    void rejectInvalidMovementType() {
        Product product = createProduct(1, "Pink Bourbon");
        Inventory inventory = createInventory(product, 5);
        createUser("admin-movement", "movement@example.com");

        InventoryMovementCreateRequest request = new InventoryMovementCreateRequest();
        request.setQuantity(2);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> inventoryService.registerMovement(inventory.getId(), request, "admin-movement")
        );

        assertEquals("Movement type is required", exception.getMessage());
    }

    @Test
    void registerAdjustmentUpdatesStockAndStoresAuditRecord() {
        Product product = createProduct(1, "SL28");
        Inventory inventory = createInventory(product, 9);
        createUser("admin-adjust", "adjust@example.com");

        InventoryMovementCreateRequest request = new InventoryMovementCreateRequest();
        request.setMovementType(InventoryMovementType.ADJUSTMENT);
        request.setQuantity(4);
        request.setReferenceType(InventoryReferenceType.MANUAL);

        InventoryResponse response = inventoryService.registerMovement(inventory.getId(), request, "admin-adjust");

        assertEquals(4, response.getStockQuantity());
        List<InventoryMovementResponse> movements = inventoryService.findMovements(inventory.getId());
        assertEquals(1, movements.size());
        assertEquals(InventoryMovementType.ADJUSTMENT, movements.getFirst().getMovementType());
        assertEquals(new BigDecimal("5.00"), movements.getFirst().getQuantity());
    }

    @Test
    void inventoryOperationsRejectMissingRecords() {
        NoSuchElementException missingProduct = assertThrows(
                NoSuchElementException.class,
                () -> inventoryService.findByProductId(999L)
        );
        assertEquals("Product not found: 999", missingProduct.getMessage());

        NoSuchElementException missingInventory = assertThrows(
                NoSuchElementException.class,
                () -> inventoryService.findById(999L)
        );
        assertEquals("Inventory not found: 999", missingInventory.getMessage());
    }

    private Usuario createUser(String username, String email) {
        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setPassword("$2a$10$inventorytestpasswordhashplaceholder1234567890");
        usuario.setEmail(email);
        usuario.setActivo(true);
        return usuarioRepository.save(usuario);
    }

    private Product createProduct(int key, String name) {
        seedCatalogReferences(key);

        Product product = new Product();
        product.setName(name);
        product.setImageUrl("https://cdn.example.com/" + name.toLowerCase().replace(" ", "-") + ".png");
        product.setPrice(new BigDecimal("20.00"));
        product.setPresentationId((long) key);
        product.setProductionId((long) key);
        return productRepository.save(product);
    }

    private Inventory createInventory(Product product, int stockQuantity) {
        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setStockQuantity(stockQuantity);
        return inventoryRepository.save(inventory);
    }

    private StockAdjustmentRequest stockRequest(Integer quantity, Long referenceId, InventoryReferenceType referenceType) {
        StockAdjustmentRequest request = new StockAdjustmentRequest();
        request.setQuantity(quantity);
        request.setReferenceId(referenceId);
        request.setReferenceType(referenceType);
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
                new BigDecimal("50.00"),
                key
        );
        jdbcTemplate.update(
                "INSERT INTO product_presentations (id_presentation, name, description) VALUES (?, ?, ?)",
                key,
                "Presentation " + key,
                "Description " + key
        );
    }

    private void assertNotNullInventory(Long inventoryId) {
        assertTrue(inventoryId != null && inventoryId > 0);
    }
}
