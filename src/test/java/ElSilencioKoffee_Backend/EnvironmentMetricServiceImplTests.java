package ElSilencioKoffee_Backend;

import ElSilencioKoffee_Backend.environment.dto.EnvironmentMetricCreateRequest;
import ElSilencioKoffee_Backend.environment.dto.EnvironmentMetricResponse;
import ElSilencioKoffee_Backend.environment.dto.EnvironmentMetricUpdateRequest;
import ElSilencioKoffee_Backend.environment.repositories.EnvironmentMetricRepository;
import ElSilencioKoffee_Backend.environment.services.IEnvironmentMetricService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class EnvironmentMetricServiceImplTests {

    @Autowired
    private IEnvironmentMetricService environmentMetricService;

    @Autowired
    private EnvironmentMetricRepository environmentMetricRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void createMetricSuccessfully() {
        seedSection(1);

        EnvironmentMetricResponse response = environmentMetricService.create(
                createRequest("temperature", "21.50", "C", LocalDateTime.of(2026, 4, 20, 8, 0), 1L)
        );

        assertTrue(environmentMetricRepository.existsById(response.getId()));
        assertEquals("temperature", response.getMetricType());
        assertEquals(new BigDecimal("21.50"), response.getValue());
        assertEquals("C", response.getUnit());
        assertEquals(LocalDateTime.of(2026, 4, 20, 8, 0), response.getMeasuredAt());
        assertEquals(1L, response.getSectionId());
        assertEquals("Section 1", response.getSectionName());
    }

    @Test
    void createMetricDefaultsMeasuredAtToServerTime() {
        EnvironmentMetricResponse response = environmentMetricService.create(
                createRequest("humidity", "65.00", "%", null, null)
        );

        assertNotNull(response.getMeasuredAt());
    }

    @Test
    void listMetrics() {
        seedSection(1);
        seedSection(2);
        environmentMetricService.create(createRequest("temperature", "21.50", "C", LocalDateTime.of(2026, 4, 20, 8, 0), 1L));
        environmentMetricService.create(createRequest("humidity", "65.00", "%", LocalDateTime.of(2026, 4, 20, 10, 0), 2L));

        List<EnvironmentMetricResponse> metrics = environmentMetricService.findAll(null, null, null, null);

        assertEquals(2, metrics.size());
        assertEquals("humidity", metrics.getFirst().getMetricType());
        assertEquals("temperature", metrics.get(1).getMetricType());
    }

    @Test
    void getMetricById() {
        EnvironmentMetricResponse created = environmentMetricService.create(
                createRequest("soil-moisture", "41.25", "%", LocalDateTime.of(2026, 4, 19, 9, 30), null)
        );

        EnvironmentMetricResponse found = environmentMetricService.findById(created.getId());

        assertEquals(created.getId(), found.getId());
        assertEquals("soil-moisture", found.getMetricType());
        assertEquals(new BigDecimal("41.25"), found.getValue());
    }

    @Test
    void updateMetric() {
        seedSection(1);
        seedSection(2);
        EnvironmentMetricResponse created = environmentMetricService.create(
                createRequest("temperature", "20.10", "C", LocalDateTime.of(2026, 4, 18, 7, 0), 1L)
        );

        EnvironmentMetricUpdateRequest request = new EnvironmentMetricUpdateRequest();
        request.setMetricType("humidity");
        request.setValue(new BigDecimal("58.00"));
        request.setUnit("%");
        request.setMeasuredAt(LocalDateTime.of(2026, 4, 18, 11, 0));
        request.setSectionId(2L);

        EnvironmentMetricResponse updated = environmentMetricService.update(created.getId(), request);

        assertEquals(created.getId(), updated.getId());
        assertEquals("humidity", updated.getMetricType());
        assertEquals(new BigDecimal("58.00"), updated.getValue());
        assertEquals("%", updated.getUnit());
        assertEquals(2L, updated.getSectionId());
        assertEquals("Section 2", updated.getSectionName());
    }

    @Test
    void deleteMetric() {
        EnvironmentMetricResponse created = environmentMetricService.create(
                createRequest("light-level", "350.00", "lux", LocalDateTime.of(2026, 4, 17, 12, 0), null)
        );

        environmentMetricService.delete(created.getId());

        assertFalse(environmentMetricRepository.existsById(created.getId()));
    }

    @Test
    void rejectMissingMetricType() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> environmentMetricService.create(createRequest("   ", "21.00", "C", LocalDateTime.of(2026, 4, 20, 8, 0), null))
        );

        assertEquals("Metric type is required", exception.getMessage());
    }

    @Test
    void rejectInvalidNumericValue() {
        EnvironmentMetricCreateRequest request = createRequest("temperature", "21.00", "C", LocalDateTime.of(2026, 4, 20, 8, 0), null);
        request.setValue(null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> environmentMetricService.create(request)
        );

        assertEquals("Metric value is required", exception.getMessage());
    }

    @Test
    void rejectFutureMeasuredAt() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> environmentMetricService.create(
                        createRequest("temperature", "21.00", "C", LocalDateTime.now().plusDays(1), null)
                )
        );

        assertEquals("Measured timestamp cannot be in the future", exception.getMessage());
    }

    @Test
    void rejectMissingReferencedSection() {
        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> environmentMetricService.create(
                        createRequest("temperature", "21.00", "C", LocalDateTime.of(2026, 4, 20, 8, 0), 999L)
                )
        );

        assertEquals("Section not found: 999", exception.getMessage());
    }

    @Test
    void notFoundBehavior() {
        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> environmentMetricService.findById(999L)
        );

        assertEquals("Environment metric not found: 999", exception.getMessage());
    }

    @Test
    void filtersByTypeSectionAndDateRange() {
        seedSection(1);
        seedSection(2);
        environmentMetricService.create(createRequest("temperature", "20.00", "C", LocalDateTime.of(2026, 4, 20, 8, 0), 1L));
        environmentMetricService.create(createRequest("temperature", "22.00", "C", LocalDateTime.of(2026, 4, 20, 10, 0), 2L));
        environmentMetricService.create(createRequest("humidity", "65.00", "%", LocalDateTime.of(2026, 4, 20, 12, 0), 1L));

        List<EnvironmentMetricResponse> filtered = environmentMetricService.findAll(
                "temperature",
                2L,
                LocalDateTime.of(2026, 4, 20, 9, 0),
                LocalDateTime.of(2026, 4, 20, 11, 0)
        );

        assertEquals(1, filtered.size());
        assertEquals(new BigDecimal("22.00"), filtered.getFirst().getValue());
        assertEquals(2L, filtered.getFirst().getSectionId());
    }

    private EnvironmentMetricCreateRequest createRequest(
            String metricType,
            String value,
            String unit,
            LocalDateTime measuredAt,
            Long sectionId
    ) {
        EnvironmentMetricCreateRequest request = new EnvironmentMetricCreateRequest();
        request.setMetricType(metricType);
        request.setValue(new BigDecimal(value));
        request.setUnit(unit);
        request.setMeasuredAt(measuredAt);
        request.setSectionId(sectionId);
        return request;
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
}
