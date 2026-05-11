package ElSilencioKoffee_Backend;

import ElSilencioKoffee_Backend.inventory.entities.Inventory;
import ElSilencioKoffee_Backend.inventory.repositories.InventoryRepository;
import ElSilencioKoffee_Backend.products.dto.ProductCreateRequest;
import ElSilencioKoffee_Backend.products.dto.ProductResponse;
import ElSilencioKoffee_Backend.products.dto.ProductUpdateRequest;
import ElSilencioKoffee_Backend.products.repositories.ProductRepository;
import ElSilencioKoffee_Backend.products.services.IProductService;
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
class ProductServiceImplTests {

    @Autowired
    private IProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void createProductSuccessfully() {
        seedCatalogReferences(1);

        ProductResponse response = productService.create(createRequest(
                "Caturra Honey",
                "https://cdn.example.com/products/caturra-honey.png",
                "24.50",
                1L,
                1L
        ));

        assertTrue(productRepository.existsById(response.getId()));
        assertEquals("Caturra Honey", response.getName());
        assertEquals("https://cdn.example.com/products/caturra-honey.png", response.getImageUrl());
        assertEquals(new BigDecimal("24.50"), response.getPrice());
        assertEquals(1L, response.getPresentationId());
        assertEquals(1L, response.getProductionId());
        assertEquals(0, response.getStockQuantity());
    }

    @Test
    void listProductsReturnsPersistedProductsWithStock() {
        seedCatalogReferences(1);
        seedCatalogReferences(2);

        ProductResponse geisha = productService.create(createRequest("Geisha", null, "35.00", 1L, 1L));
        ProductResponse bourbon = productService.create(createRequest("Bourbon", null, "21.00", 2L, 2L));
        createInventory(bourbon.getId(), 9);

        List<ProductResponse> products = productService.findAll();

        assertEquals(2, products.size());
        assertEquals("Geisha", products.getFirst().getName());
        assertEquals(0, products.getFirst().getStockQuantity());
        assertEquals("Bourbon", products.get(1).getName());
        assertEquals(9, products.get(1).getStockQuantity());
    }

    @Test
    void findProductByIdReturnsPersistedProduct() {
        seedCatalogReferences(1);
        ProductResponse created = productService.create(createRequest("Pacamara", null, "29.90", 1L, 1L));

        ProductResponse found = productService.findById(created.getId());

        assertEquals(created.getId(), found.getId());
        assertEquals("Pacamara", found.getName());
        assertEquals(new BigDecimal("29.90"), found.getPrice());
    }

    @Test
    void updateProductChangesPersistedFields() {
        seedCatalogReferences(1);
        seedCatalogReferences(2);
        ProductResponse created = productService.create(createRequest("Castillo", null, "19.00", 1L, 1L));

        ProductUpdateRequest updateRequest = new ProductUpdateRequest();
        updateRequest.setName("Castillo Reserva");
        updateRequest.setImageUrl("https://cdn.example.com/products/castillo-reserva.png");
        updateRequest.setPrice(new BigDecimal("22.00"));
        updateRequest.setPresentationId(2L);
        updateRequest.setProductionId(2L);
        updateRequest.setStockQuantity(14);

        ProductResponse updated = productService.update(created.getId(), updateRequest);

        assertEquals(created.getId(), updated.getId());
        assertEquals("Castillo Reserva", updated.getName());
        assertEquals("https://cdn.example.com/products/castillo-reserva.png", updated.getImageUrl());
        assertEquals(new BigDecimal("22.00"), updated.getPrice());
        assertEquals(2L, updated.getPresentationId());
        assertEquals(2L, updated.getProductionId());
        assertEquals(14, updated.getStockQuantity());
    }

    @Test
    void deleteProductRemovesRow() {
        seedCatalogReferences(1);
        ProductResponse created = productService.create(createRequest("Typica", null, "18.00", 1L, 1L));

        productService.delete(created.getId());

        assertFalse(productRepository.existsById(created.getId()));
    }

    @Test
    void productOperationsRejectMissingRecords() {
        NoSuchElementException findException = assertThrows(
                NoSuchElementException.class,
                () -> productService.findById(999L)
        );
        assertEquals("Product not found: 999", findException.getMessage());

        ProductUpdateRequest updateRequest = new ProductUpdateRequest();
        updateRequest.setName("Missing");
        updateRequest.setPrice(new BigDecimal("10.00"));
        updateRequest.setPresentationId(1L);
        updateRequest.setProductionId(1L);

        NoSuchElementException updateException = assertThrows(
                NoSuchElementException.class,
                () -> productService.update(999L, updateRequest)
        );
        assertEquals("Product not found: 999", updateException.getMessage());
    }

    @Test
    void createProductRejectsInvalidData() {
        seedCatalogReferences(1);

        ProductCreateRequest request = createRequest("   ", null, "12.00", 1L, 1L);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productService.create(request)
        );

        assertEquals("Product name is required", exception.getMessage());
    }

    @Test
    void updateProductRejectsNegativeStockQuantity() {
        seedCatalogReferences(1);
        ProductResponse created = productService.create(createRequest("Bourbon", null, "18.50", 1L, 1L));

        ProductUpdateRequest request = new ProductUpdateRequest();
        request.setName("Bourbon");
        request.setPrice(new BigDecimal("18.50"));
        request.setPresentationId(1L);
        request.setProductionId(1L);
        request.setStockQuantity(-1);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productService.update(created.getId(), request)
        );

        assertEquals("Stock quantity must be greater than or equal to 0", exception.getMessage());
    }

    private ProductCreateRequest createRequest(
            String name,
            String imageUrl,
            String price,
            Long presentationId,
            Long productionId
    ) {
        ProductCreateRequest request = new ProductCreateRequest();
        request.setName(name);
        request.setImageUrl(imageUrl);
        request.setPrice(new BigDecimal(price));
        request.setPresentationId(presentationId);
        request.setProductionId(productionId);
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

    private void createInventory(Long productId, int stockQuantity) {
        Inventory inventory = new Inventory();
        inventory.setProduct(productRepository.findById(productId).orElseThrow());
        inventory.setStockQuantity(stockQuantity);
        inventoryRepository.save(inventory);
    }
}
