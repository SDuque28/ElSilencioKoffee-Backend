package ElSilencioKoffee_Backend.inventory.controllers;

import ElSilencioKoffee_Backend.inventory.dto.InventoryMovementCreateRequest;
import ElSilencioKoffee_Backend.inventory.dto.InventoryMovementResponse;
import ElSilencioKoffee_Backend.inventory.dto.InventoryResponse;
import ElSilencioKoffee_Backend.inventory.dto.StockAdjustmentRequest;
import ElSilencioKoffee_Backend.inventory.services.IInventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class InventoryController {

    private final IInventoryService inventoryService;

    @GetMapping
    public ResponseEntity<List<InventoryResponse>> findAll() {
        return ResponseEntity.ok(inventoryService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventoryResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(inventoryService.findById(id));
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<InventoryResponse> findByProductId(@PathVariable Long productId) {
        return ResponseEntity.ok(inventoryService.findByProductId(productId));
    }

    @GetMapping("/{id}/movements")
    public ResponseEntity<List<InventoryMovementResponse>> findMovements(@PathVariable Long id) {
        return ResponseEntity.ok(inventoryService.findMovements(id));
    }

    @PostMapping("/{id}/increase")
    public ResponseEntity<InventoryResponse> increase(
            @PathVariable Long id,
            @RequestBody StockAdjustmentRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(inventoryService.increaseStock(id, request, authentication.getName()));
    }

    @PostMapping("/{id}/decrease")
    public ResponseEntity<InventoryResponse> decrease(
            @PathVariable Long id,
            @RequestBody StockAdjustmentRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(inventoryService.decreaseStock(id, request, authentication.getName()));
    }

    @PostMapping("/{id}/movements")
    public ResponseEntity<InventoryResponse> createMovement(
            @PathVariable Long id,
            @RequestBody InventoryMovementCreateRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(inventoryService.registerMovement(id, request, authentication.getName()));
    }
}
