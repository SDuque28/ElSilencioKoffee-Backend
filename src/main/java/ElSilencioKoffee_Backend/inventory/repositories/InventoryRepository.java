package ElSilencioKoffee_Backend.inventory.repositories;

import ElSilencioKoffee_Backend.inventory.entities.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByProductId(Long productId);
}
