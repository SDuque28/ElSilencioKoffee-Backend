package ElSilencioKoffee_Backend.repository;

import ElSilencioKoffee_Backend.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
