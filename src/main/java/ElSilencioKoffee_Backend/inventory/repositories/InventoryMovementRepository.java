package ElSilencioKoffee_Backend.inventory.repositories;

import ElSilencioKoffee_Backend.inventory.entities.InventoryMovement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Long> {

    List<InventoryMovement> findByProductIdOrderByCreatedAtDescIdDesc(Long productId);
}
