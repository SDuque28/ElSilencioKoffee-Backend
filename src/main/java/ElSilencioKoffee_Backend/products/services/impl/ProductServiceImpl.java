package ElSilencioKoffee_Backend.products.services.impl;

import ElSilencioKoffee_Backend.inventory.entities.Inventory;
import ElSilencioKoffee_Backend.inventory.repositories.InventoryRepository;
import ElSilencioKoffee_Backend.products.dto.ProductCreateRequest;
import ElSilencioKoffee_Backend.products.dto.ProductResponse;
import ElSilencioKoffee_Backend.products.dto.ProductUpdateRequest;
import ElSilencioKoffee_Backend.products.entities.Product;
import ElSilencioKoffee_Backend.products.repositories.ProductRepository;
import ElSilencioKoffee_Backend.products.services.IProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements IProductService {

    private static final String INVALID_REFERENCE_MESSAGE =
            "Product references a presentation or production record that does not exist";

    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;

    @Override
    @Transactional
    public ProductResponse create(ProductCreateRequest request) {
        Product product = new Product();
        applyRequest(product, request);
        return toResponse(saveProduct(product));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> findAll() {
        return productRepository.findAll(Sort.by(Sort.Direction.ASC, "id")).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse findById(Long id) {
        return toResponse(findProduct(id));
    }

    @Override
    @Transactional
    public ProductResponse update(Long id, ProductUpdateRequest request) {
        Product product = findProduct(id);
        applyRequest(product, request);
        Product savedProduct = saveProduct(product);
        syncInventory(savedProduct, request.getStockQuantity());
        return toResponse(savedProduct);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Product product = findProduct(id);
        try {
            productRepository.delete(product);
            productRepository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new IllegalArgumentException("Product cannot be deleted because it is referenced by other records");
        }
    }

    private Product findProduct(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Product ID must be greater than 0");
        }

        return productRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Product not found: " + id));
    }

    private Product saveProduct(Product product) {
        try {
            return productRepository.save(product);
        } catch (DataIntegrityViolationException exception) {
            throw new IllegalArgumentException(INVALID_REFERENCE_MESSAGE);
        }
    }

    private void applyRequest(Product product, ProductCreateRequest request) {
        product.setName(normalizeName(request.getName()));
        product.setImageUrl(normalizeImageUrl(request.getImageUrl()));
        product.setPrice(validatePrice(request.getPrice()));
        product.setPresentationId(validateReferenceId(request.getPresentationId(), "Presentation ID"));
        product.setProductionId(validateReferenceId(request.getProductionId(), "Production ID"));
    }

    private void applyRequest(Product product, ProductUpdateRequest request) {
        product.setName(normalizeName(request.getName()));
        product.setImageUrl(normalizeImageUrl(request.getImageUrl()));
        product.setPrice(validatePrice(request.getPrice()));
        product.setPresentationId(validateReferenceId(request.getPresentationId(), "Presentation ID"));
        product.setProductionId(validateReferenceId(request.getProductionId(), "Production ID"));
    }

    private void syncInventory(Product product, Integer stockQuantity) {
        if (stockQuantity == null) {
            return;
        }

        Inventory inventory = inventoryRepository.findByProductId(product.getId())
                .orElseGet(() -> {
                    Inventory newInventory = new Inventory();
                    newInventory.setProduct(product);
                    return newInventory;
                });

        inventory.setStockQuantity(validateStockQuantity(stockQuantity));
        inventoryRepository.save(inventory);
    }

    private String normalizeName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Product name is required");
        }

        String normalized = name.trim();
        if (normalized.length() > 100) {
            throw new IllegalArgumentException("Product name must be at most 100 characters");
        }

        return normalized;
    }

    private String normalizeImageUrl(String imageUrl) {
        if (imageUrl == null) {
            return null;
        }

        String normalized = imageUrl.trim();
        if (normalized.isEmpty()) {
            return null;
        }
        if (normalized.length() > 255) {
            throw new IllegalArgumentException("Image URL must be at most 255 characters");
        }

        return normalized;
    }

    private BigDecimal validatePrice(BigDecimal price) {
        if (price == null) {
            throw new IllegalArgumentException("Product price is required");
        }
        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Product price must be greater than 0");
        }
        return price;
    }

    private Integer validateReferenceId(Long id, String label) {
        if (id == null) {
            throw new IllegalArgumentException(label + " is required");
        }
        if (id <= 0) {
            throw new IllegalArgumentException(label + " must be greater than 0");
        }
        return Math.toIntExact(id);
    }

    private Integer validateStockQuantity(Integer stockQuantity) {
        if (stockQuantity < 0) {
            throw new IllegalArgumentException("Stock quantity must be greater than or equal to 0");
        }
        return stockQuantity;
    }

    private ProductResponse toResponse(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setImageUrl(product.getImageUrl());
        response.setPrice(product.getPrice());
        response.setPresentationId(product.getPresentationId().longValue());
        response.setProductionId(product.getProductionId().longValue());
        response.setStockQuantity(
                inventoryRepository.findByProductId(product.getId())
                        .map(inventory -> inventory.getStockQuantity())
                        .orElse(0)
        );
        return response;
    }
}
