package ElSilencioKoffee_Backend.inventory.dto;

import ElSilencioKoffee_Backend.inventory.entities.InventoryMovementType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class InventoryMovementCreateRequest extends StockAdjustmentRequest {

    private InventoryMovementType movementType;
}
