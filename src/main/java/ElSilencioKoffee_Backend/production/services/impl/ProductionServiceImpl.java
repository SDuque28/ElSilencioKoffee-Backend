package ElSilencioKoffee_Backend.production.services.impl;

import ElSilencioKoffee_Backend.production.dto.ProductionCreateRequest;
import ElSilencioKoffee_Backend.production.dto.ProductionResponse;
import ElSilencioKoffee_Backend.production.dto.ProductionUpdateRequest;
import ElSilencioKoffee_Backend.production.entities.Production;
import ElSilencioKoffee_Backend.production.entities.ProductionSection;
import ElSilencioKoffee_Backend.production.entities.ProductionVariety;
import ElSilencioKoffee_Backend.production.repositories.ProductionRepository;
import ElSilencioKoffee_Backend.production.repositories.ProductionSectionRepository;
import ElSilencioKoffee_Backend.production.repositories.ProductionVarietyRepository;
import ElSilencioKoffee_Backend.production.services.IProductionService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ProductionServiceImpl implements IProductionService {

    private final ProductionRepository productionRepository;
    private final ProductionSectionRepository sectionRepository;
    private final ProductionVarietyRepository varietyRepository;

    @Override
    @Transactional
    public ProductionResponse create(ProductionCreateRequest request) {
        Production production = new Production();
        applyRequest(production, request);
        return toResponse(productionRepository.save(production));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductionResponse> findAll() {
        return productionRepository.findAll(Sort.by(Sort.Direction.DESC, "collectionDate", "id")).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductionResponse findById(Long id) {
        return toResponse(findProduction(id));
    }

    @Override
    @Transactional
    public ProductionResponse update(Long id, ProductionUpdateRequest request) {
        Production production = findProduction(id);
        applyRequest(production, request);
        return toResponse(productionRepository.save(production));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Production production = findProduction(id);
        try {
            productionRepository.delete(production);
            productionRepository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new IllegalArgumentException(
                    "Production cannot be deleted because it is referenced by other records"
            );
        }
    }

    private Production findProduction(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Production ID must be greater than 0");
        }

        return productionRepository.findById(Math.toIntExact(id))
                .orElseThrow(() -> new NoSuchElementException("Production not found: " + id));
    }

    private void applyRequest(Production production, ProductionCreateRequest request) {
        production.setSection(findSection(requireReferenceId(request.getSectionId(), "Section ID")));
        production.setVariety(findVariety(requireReferenceId(request.getVarietyId(), "Variety ID")));
        production.setCollectionDate(requireCollectionDate(request.getCollectionDate()));
        production.setQuantityKg(requirePositiveQuantity(request.getQuantityKg()));
    }

    private Integer requireReferenceId(Long id, String label) {
        if (id == null) {
            throw new IllegalArgumentException(label + " is required");
        }
        if (id <= 0) {
            throw new IllegalArgumentException(label + " must be greater than 0");
        }
        return Math.toIntExact(id);
    }

    private LocalDate requireCollectionDate(LocalDate collectionDate) {
        if (collectionDate == null) {
            throw new IllegalArgumentException("Collection date is required");
        }
        return collectionDate;
    }

    private BigDecimal requirePositiveQuantity(BigDecimal quantityKg) {
        if (quantityKg == null) {
            throw new IllegalArgumentException("Production quantity is required");
        }
        if (quantityKg.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Production quantity must be greater than 0");
        }
        return quantityKg;
    }

    private ProductionSection findSection(Integer id) {
        return sectionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Section not found: " + id.longValue()));
    }

    private ProductionVariety findVariety(Integer id) {
        return varietyRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Variety not found: " + id.longValue()));
    }

    private ProductionResponse toResponse(Production production) {
        ProductionResponse response = new ProductionResponse();
        response.setId(production.getId().longValue());
        response.setSectionId(production.getSection().getId().longValue());
        response.setSectionName(production.getSection().getName());
        response.setSectionLocation(production.getSection().getLocation());
        response.setVarietyId(production.getVariety().getId().longValue());
        response.setVarietyName(production.getVariety().getName());
        response.setQuantityKg(production.getQuantityKg());
        response.setCollectionDate(production.getCollectionDate());
        return response;
    }
}
