package ElSilencioKoffee_Backend.products.repositories;

import ElSilencioKoffee_Backend.products.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}

