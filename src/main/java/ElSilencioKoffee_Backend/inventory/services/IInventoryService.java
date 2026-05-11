package ElSilencioKoffee_Backend.inventory.services;

import ElSilencioKoffee_Backend.inventory.dto.InventoryMovementCreateRequest;
import ElSilencioKoffee_Backend.inventory.dto.InventoryMovementResponse;
import ElSilencioKoffee_Backend.inventory.dto.InventoryResponse;
import ElSilencioKoffee_Backend.inventory.dto.StockAdjustmentRequest;
import ElSilencioKoffee_Backend.orders.entities.Order;

import java.util.Map;
import java.util.List;

public interface IInventoryService {

    List<InventoryResponse> findAll();

    InventoryResponse findById(Long inventoryId);

    InventoryResponse findByProductId(Long productId);

    List<InventoryMovementResponse> findMovements(Long inventoryId);

    InventoryResponse increaseStock(Long inventoryId, StockAdjustmentRequest request, String username);

    InventoryResponse decreaseStock(Long inventoryId, StockAdjustmentRequest request, String username);

    InventoryResponse registerMovement(Long inventoryId, InventoryMovementCreateRequest request, String username);

    void ensureSufficientStock(Map<Long, Integer> requestedQuantities);

    void consumeStockForOrder(Order order, String username);
}
