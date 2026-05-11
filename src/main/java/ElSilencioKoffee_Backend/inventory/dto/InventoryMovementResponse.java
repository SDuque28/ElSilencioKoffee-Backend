package ElSilencioKoffee_Backend.inventory.dto;

import ElSilencioKoffee_Backend.inventory.entities.InventoryMovementType;
import ElSilencioKoffee_Backend.inventory.entities.InventoryReferenceType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class InventoryMovementResponse {

    private Long id;
    private Long inventoryId;
    private Long productId;
    private InventoryMovementType movementType;
    private BigDecimal quantity;
    private Long referenceId;
    private InventoryReferenceType referenceType;
    private LocalDateTime createdAt;
    private Long createdByUserId;
    private String createdByUsername;
}
