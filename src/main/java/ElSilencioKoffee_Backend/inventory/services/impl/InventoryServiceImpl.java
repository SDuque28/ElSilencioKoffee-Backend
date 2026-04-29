package ElSilencioKoffee_Backend.inventory.services.impl;

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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements IInventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final ProductRepository productRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public List<InventoryResponse> findAll() {
        return inventoryRepository.findAll(Sort.by(Sort.Direction.ASC, "id")).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryResponse findById(Long inventoryId) {
        return toResponse(findInventory(inventoryId));
    }

    @Override
    @Transactional
    public InventoryResponse findByProductId(Long productId) {
        Product product = findProduct(productId);
        return toResponse(findOrCreateInventory(product));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryMovementResponse> findMovements(Long inventoryId) {
        Inventory inventory = findInventory(inventoryId);
        return inventoryMovementRepository.findByProductIdOrderByCreatedAtDescIdDesc(inventory.getProduct().getId()).stream()
                .map(movement -> toMovementResponse(inventory.getId().longValue(), movement))
                .toList();
    }

    @Override
    @Transactional
    public InventoryResponse increaseStock(Long inventoryId, StockAdjustmentRequest request, String username) {
        applyIncrease(findInventory(inventoryId), request, findUserByUsername(username));
        return toResponse(findInventory(inventoryId));
    }

    @Override
    @Transactional
    public InventoryResponse decreaseStock(Long inventoryId, StockAdjustmentRequest request, String username) {
        applyDecrease(findInventory(inventoryId), request, findUserByUsername(username));
        return toResponse(findInventory(inventoryId));
    }

    @Override
    @Transactional
    public InventoryResponse registerMovement(Long inventoryId, InventoryMovementCreateRequest request, String username) {
        if (request.getMovementType() == null) {
            throw new IllegalArgumentException("Movement type is required");
        }

        Inventory inventory = findInventory(inventoryId);
        Usuario actor = findUserByUsername(username);

        switch (request.getMovementType()) {
            case IN -> applyIncrease(inventory, request, actor);
            case OUT -> applyDecrease(inventory, request, actor);
            case ADJUSTMENT -> applyAdjustment(inventory, request, actor);
            default -> throw new IllegalArgumentException("Unsupported movement type: " + request.getMovementType());
        }

        return toResponse(inventory);
    }

    private Inventory findInventory(Long inventoryId) {
        if (inventoryId == null || inventoryId <= 0) {
            throw new IllegalArgumentException("Inventory ID must be greater than 0");
        }

        return inventoryRepository.findById(Math.toIntExact(inventoryId))
                .orElseThrow(() -> new NoSuchElementException("Inventory not found: " + inventoryId));
    }

    private Product findProduct(Long productId) {
        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException("Product ID must be greater than 0");
        }

        return productRepository.findById(productId)
                .orElseThrow(() -> new NoSuchElementException("Product not found: " + productId));
    }

    private Usuario findUserByUsername(String username) {
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + username));
    }

    private Inventory findOrCreateInventory(Product product) {
        return inventoryRepository.findByProductId(product.getId())
                .orElseGet(() -> {
                    Inventory inventory = new Inventory();
                    inventory.setProduct(product);
                    inventory.setStockQuantity(0);
                    return inventoryRepository.save(inventory);
                });
    }

    private void applyIncrease(Inventory inventory, StockAdjustmentRequest request, Usuario actor) {
        int quantity = requirePositiveQuantity(request.getQuantity(), "Quantity");
        inventory.setStockQuantity(inventory.getStockQuantity() + quantity);
        inventoryRepository.save(inventory);
        recordMovement(inventory, InventoryMovementType.IN, quantity, request.getReferenceId(), request.getReferenceType(), actor);
    }

    private void applyDecrease(Inventory inventory, StockAdjustmentRequest request, Usuario actor) {
        int quantity = requirePositiveQuantity(request.getQuantity(), "Quantity");
        if (inventory.getStockQuantity() < quantity) {
            throw new IllegalArgumentException("Stock cannot go below zero");
        }

        inventory.setStockQuantity(inventory.getStockQuantity() - quantity);
        inventoryRepository.save(inventory);
        recordMovement(inventory, InventoryMovementType.OUT, quantity, request.getReferenceId(), request.getReferenceType(), actor);
    }

    private void applyAdjustment(Inventory inventory, StockAdjustmentRequest request, Usuario actor) {
        int targetQuantity = requireNonNegativeQuantity(request.getQuantity(), "Quantity");
        int currentQuantity = inventory.getStockQuantity();
        int delta = Math.abs(targetQuantity - currentQuantity);
        if (delta == 0) {
            throw new IllegalArgumentException("Adjustment must change stock quantity");
        }

        inventory.setStockQuantity(targetQuantity);
        inventoryRepository.save(inventory);
        recordMovement(inventory, InventoryMovementType.ADJUSTMENT, delta, request.getReferenceId(), request.getReferenceType(), actor);
    }

    private void recordMovement(
            Inventory inventory,
            InventoryMovementType movementType,
            int quantity,
            Long referenceId,
            InventoryReferenceType referenceType,
            Usuario actor
    ) {
        InventoryMovement movement = new InventoryMovement();
        movement.setProduct(inventory.getProduct());
        movement.setMovementType(movementType);
        movement.setQuantity(BigDecimal.valueOf(quantity).setScale(2));
        movement.setReferenceId(validateReferenceId(referenceId));
        movement.setReferenceType(normalizeReferenceType(referenceId, referenceType));
        movement.setCreatedBy(actor);
        inventoryMovementRepository.save(movement);
    }

    private Long validateReferenceId(Long referenceId) {
        if (referenceId == null) {
            return null;
        }
        if (referenceId <= 0) {
            throw new IllegalArgumentException("Reference ID must be greater than 0");
        }
        return referenceId;
    }

    private InventoryReferenceType normalizeReferenceType(Long referenceId, InventoryReferenceType referenceType) {
        if (referenceId == null) {
            if (referenceType == null) {
                return InventoryReferenceType.MANUAL;
            }
            if (referenceType != InventoryReferenceType.MANUAL) {
                throw new IllegalArgumentException("Reference ID is required for reference type " + referenceType);
            }
            return referenceType;
        }

        if (referenceType == null) {
            throw new IllegalArgumentException("Reference type is required when reference ID is provided");
        }

        return referenceType;
    }

    private int requirePositiveQuantity(Integer quantity, String label) {
        if (quantity == null) {
            throw new IllegalArgumentException(label + " is required");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException(label + " must be greater than 0");
        }
        return quantity;
    }

    private int requireNonNegativeQuantity(Integer quantity, String label) {
        if (quantity == null) {
            throw new IllegalArgumentException(label + " is required");
        }
        if (quantity < 0) {
            throw new IllegalArgumentException(label + " must be greater than or equal to 0");
        }
        return quantity;
    }

    private InventoryResponse toResponse(Inventory inventory) {
        InventoryResponse response = new InventoryResponse();
        response.setId(inventory.getId().longValue());
        response.setProductId(inventory.getProduct().getId());
        response.setProductName(inventory.getProduct().getName());
        response.setProductImageUrl(inventory.getProduct().getImageUrl());
        response.setStockQuantity(inventory.getStockQuantity());
        return response;
    }

    private InventoryMovementResponse toMovementResponse(Long inventoryId, InventoryMovement movement) {
        InventoryMovementResponse response = new InventoryMovementResponse();
        response.setId(movement.getId());
        response.setInventoryId(inventoryId);
        response.setProductId(movement.getProduct().getId());
        response.setMovementType(movement.getMovementType());
        response.setQuantity(movement.getQuantity());
        response.setReferenceId(movement.getReferenceId());
        response.setReferenceType(movement.getReferenceType());
        response.setCreatedAt(movement.getCreatedAt());
        response.setCreatedByUserId(movement.getCreatedBy().getId());
        response.setCreatedByUsername(movement.getCreatedBy().getUsername());
        return response;
    }
}
