package ElSilencioKoffee_Backend.environment.services.impl;

import ElSilencioKoffee_Backend.environment.dto.EnvironmentMetricCreateRequest;
import ElSilencioKoffee_Backend.environment.dto.EnvironmentMetricResponse;
import ElSilencioKoffee_Backend.environment.dto.EnvironmentMetricUpdateRequest;
import ElSilencioKoffee_Backend.environment.entities.EnvironmentMetric;
import ElSilencioKoffee_Backend.environment.repositories.EnvironmentMetricRepository;
import ElSilencioKoffee_Backend.environment.services.IEnvironmentMetricService;
import ElSilencioKoffee_Backend.production.entities.ProductionSection;
import ElSilencioKoffee_Backend.production.repositories.ProductionSectionRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class EnvironmentMetricServiceImpl implements IEnvironmentMetricService {

    private final EnvironmentMetricRepository environmentMetricRepository;
    private final ProductionSectionRepository sectionRepository;

    @Override
    @Transactional
    public EnvironmentMetricResponse create(EnvironmentMetricCreateRequest request) {
        EnvironmentMetric metric = new EnvironmentMetric();
        applyRequest(metric, request);
        return toResponse(environmentMetricRepository.save(metric));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnvironmentMetricResponse> findAll(String type, Long sectionId, LocalDateTime fromDate, LocalDateTime toDate) {
        validateFilterRange(fromDate, toDate);

        Specification<EnvironmentMetric> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (type != null && !type.isBlank()) {
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.lower(root.get("metricType")),
                        type.trim().toLowerCase()
                ));
            }

            if (sectionId != null) {
                if (sectionId <= 0) {
                    throw new IllegalArgumentException("Section ID must be greater than 0");
                }
                predicates.add(criteriaBuilder.equal(root.get("section").get("id"), Math.toIntExact(sectionId)));
            }

            if (fromDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("measuredAt"), fromDate));
            }

            if (toDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("measuredAt"), toDate));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return environmentMetricRepository.findAll(
                        specification,
                        Sort.by(Sort.Direction.DESC, "measuredAt", "id")
                ).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EnvironmentMetricResponse findById(Long id) {
        return toResponse(findMetric(id));
    }

    @Override
    @Transactional
    public EnvironmentMetricResponse update(Long id, EnvironmentMetricUpdateRequest request) {
        EnvironmentMetric metric = findMetric(id);
        applyRequest(metric, request);
        return toResponse(environmentMetricRepository.save(metric));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        EnvironmentMetric metric = findMetric(id);
        environmentMetricRepository.delete(metric);
    }

    private EnvironmentMetric findMetric(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Environment metric ID must be greater than 0");
        }

        return environmentMetricRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Environment metric not found: " + id));
    }

    private void applyRequest(EnvironmentMetric metric, EnvironmentMetricCreateRequest request) {
        metric.setMetricType(normalizeMetricType(request.getMetricType()));
        metric.setMetricValue(requireValue(request.getValue()));
        metric.setUnit(normalizeUnit(request.getUnit()));
        metric.setMeasuredAt(resolveMeasuredAt(request.getMeasuredAt()));
        metric.setSection(resolveSection(request.getSectionId()));
    }

    private String normalizeMetricType(String metricType) {
        if (metricType == null || metricType.isBlank()) {
            throw new IllegalArgumentException("Metric type is required");
        }

        String normalized = metricType.trim();
        if (normalized.length() > 50) {
            throw new IllegalArgumentException("Metric type must be at most 50 characters");
        }
        return normalized;
    }

    private BigDecimal requireValue(BigDecimal value) {
        if (value == null) {
            throw new IllegalArgumentException("Metric value is required");
        }
        return value;
    }

    private String normalizeUnit(String unit) {
        if (unit == null || unit.isBlank()) {
            throw new IllegalArgumentException("Metric unit is required");
        }

        String normalized = unit.trim();
        if (normalized.length() > 30) {
            throw new IllegalArgumentException("Metric unit must be at most 30 characters");
        }
        return normalized;
    }

    private LocalDateTime resolveMeasuredAt(LocalDateTime measuredAt) {
        LocalDateTime resolved = measuredAt == null ? LocalDateTime.now() : measuredAt;
        if (resolved.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Measured timestamp cannot be in the future");
        }
        return resolved;
    }

    private ProductionSection resolveSection(Long sectionId) {
        if (sectionId == null) {
            return null;
        }
        if (sectionId <= 0) {
            throw new IllegalArgumentException("Section ID must be greater than 0");
        }
        return sectionRepository.findById(Math.toIntExact(sectionId))
                .orElseThrow(() -> new NoSuchElementException("Section not found: " + sectionId));
    }

    private void validateFilterRange(LocalDateTime fromDate, LocalDateTime toDate) {
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("From date must be earlier than or equal to to date");
        }
    }

    private EnvironmentMetricResponse toResponse(EnvironmentMetric metric) {
        EnvironmentMetricResponse response = new EnvironmentMetricResponse();
        response.setId(metric.getId());
        response.setMetricType(metric.getMetricType());
        response.setValue(metric.getMetricValue());
        response.setUnit(metric.getUnit());
        response.setMeasuredAt(metric.getMeasuredAt());

        if (metric.getSection() != null) {
            response.setSectionId(metric.getSection().getId().longValue());
            response.setSectionName(metric.getSection().getName());
            response.setSectionLocation(metric.getSection().getLocation());
        }

        return response;
    }
}
