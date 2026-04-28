package ElSilencioKoffee_Backend;

import ElSilencioKoffee_Backend.production.dto.ProductionCreateRequest;
import ElSilencioKoffee_Backend.production.dto.ProductionResponse;
import ElSilencioKoffee_Backend.production.dto.ProductionUpdateRequest;
import ElSilencioKoffee_Backend.production.entities.Production;
import ElSilencioKoffee_Backend.production.repositories.ProductionRepository;
import ElSilencioKoffee_Backend.production.services.IProductionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class ProductionServiceImplTests {

    @Autowired
    private IProductionService productionService;

    @Autowired
    private ProductionRepository productionRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void createProductionRecordSuccessfully() {
        seedSection(1);
        seedVariety(1);

        ProductionResponse response = productionService.create(createRequest(1L, 1L, "42.50", LocalDate.of(2026, 4, 20)));

        assertTrue(productionRepository.existsById(response.getId()));
        assertEquals(1L, response.getSectionId());
        assertEquals("Section 1", response.getSectionName());
        assertEquals(1L, response.getVarietyId());
        assertEquals("Variety 1", response.getVarietyName());
        assertEquals(new BigDecimal("42.50"), response.getQuantityKg());
        assertEquals(LocalDate.of(2026, 4, 20), response.getCollectionDate());
    }

    @Test
    void listProductionRecords() {
        seedSection(1);
        seedVariety(1);
        seedSection(2);
        seedVariety(2);

        productionService.create(createRequest(1L, 1L, "25.00", LocalDate.of(2026, 4, 10)));
        productionService.create(createRequest(2L, 2L, "18.00", LocalDate.of(2026, 4, 15)));

        List<ProductionResponse> productions = productionService.findAll();

        assertEquals(2, productions.size());
        assertEquals(LocalDate.of(2026, 4, 15), productions.getFirst().getCollectionDate());
        assertEquals("Variety 2", productions.getFirst().getVarietyName());
        assertEquals("Section 1", productions.get(1).getSectionName());
    }

    @Test
    void getProductionById() {
        seedSection(1);
        seedVariety(1);
        Production created = productionRepository.save(toEntity(1L, 1L, "30.00", LocalDate.of(2026, 4, 5)));

        ProductionResponse response = productionService.findById(created.getId());

        assertEquals(created.getId(), response.getId());
        assertEquals(1L, response.getSectionId());
        assertEquals(1L, response.getVarietyId());
        assertEquals(new BigDecimal("30.00"), response.getQuantityKg());
    }

    @Test
    void updateProductionRecord() {
        seedSection(1);
        seedVariety(1);
        seedSection(2);
        seedVariety(2);
        Production created = productionRepository.save(toEntity(1L, 1L, "30.00", LocalDate.of(2026, 4, 5)));

        ProductionUpdateRequest request = new ProductionUpdateRequest();
        request.setSectionId(2L);
        request.setVarietyId(2L);
        request.setQuantityKg(new BigDecimal("55.75"));
        request.setCollectionDate(LocalDate.of(2026, 4, 22));

        ProductionResponse response = productionService.update(created.getId(), request);

        assertEquals(created.getId(), response.getId());
        assertEquals(2L, response.getSectionId());
        assertEquals("Section 2", response.getSectionName());
        assertEquals(2L, response.getVarietyId());
        assertEquals("Variety 2", response.getVarietyName());
        assertEquals(new BigDecimal("55.75"), response.getQuantityKg());
        assertEquals(LocalDate.of(2026, 4, 22), response.getCollectionDate());
    }

    @Test
    void deleteProductionRecord() {
        seedSection(1);
        seedVariety(1);
        Production created = productionRepository.save(toEntity(1L, 1L, "30.00", LocalDate.of(2026, 4, 5)));

        productionService.delete(created.getId());

        assertFalse(productionRepository.existsById(created.getId()));
    }

    @Test
    void rejectInvalidQuantity() {
        seedSection(1);
        seedVariety(1);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productionService.create(createRequest(1L, 1L, "0.00", LocalDate.of(2026, 4, 20)))
        );

        assertEquals("Production quantity must be greater than 0", exception.getMessage());
    }

    @Test
    void rejectMissingSectionReference() {
        seedVariety(1);

        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> productionService.create(createRequest(999L, 1L, "12.00", LocalDate.of(2026, 4, 20)))
        );

        assertEquals("Section not found: 999", exception.getMessage());
    }

    @Test
    void rejectMissingVarietyReference() {
        seedSection(1);

        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> productionService.create(createRequest(1L, 999L, "12.00", LocalDate.of(2026, 4, 20)))
        );

        assertEquals("Variety not found: 999", exception.getMessage());
    }

    @Test
    void notFoundBehaviorForMissingProduction() {
        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> productionService.findById(999L)
        );

        assertEquals("Production not found: 999", exception.getMessage());
    }

    private ProductionCreateRequest createRequest(Long sectionId, Long varietyId, String quantityKg, LocalDate collectionDate) {
        ProductionCreateRequest request = new ProductionCreateRequest();
        request.setSectionId(sectionId);
        request.setVarietyId(varietyId);
        request.setQuantityKg(new BigDecimal(quantityKg));
        request.setCollectionDate(collectionDate);
        return request;
    }

    private Production toEntity(Long sectionId, Long varietyId, String quantityKg, LocalDate collectionDate) {
        ProductionCreateRequest request = createRequest(sectionId, varietyId, quantityKg, collectionDate);
        ProductionResponse created = productionService.create(request);
        return productionRepository.findById(created.getId()).orElseThrow();
    }

    private void seedSection(int id) {
        jdbcTemplate.update(
                "INSERT INTO sections (id_section, name, location, capacity) VALUES (?, ?, ?, ?)",
                id,
                "Section " + id,
                "Location " + id,
                100
        );
    }

    private void seedVariety(int id) {
        jdbcTemplate.update(
                "INSERT INTO varieties (id_variety, name, description) VALUES (?, ?, ?)",
                id,
                "Variety " + id,
                "Description " + id
        );
    }
}
