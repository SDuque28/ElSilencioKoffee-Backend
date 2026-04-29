package ElSilencioKoffee_Backend.inventory.dto;

import ElSilencioKoffee_Backend.inventory.entities.InventoryReferenceType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class StockAdjustmentRequest {

    private Integer quantity;
    private Long referenceId;
    private InventoryReferenceType referenceType;
}
