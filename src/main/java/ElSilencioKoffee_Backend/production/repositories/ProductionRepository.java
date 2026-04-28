package ElSilencioKoffee_Backend.production.repositories;

import ElSilencioKoffee_Backend.production.entities.Production;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductionRepository extends JpaRepository<Production, Long> {

    @Override
    @EntityGraph(attributePaths = {"section", "variety"})
    List<Production> findAll();

    @Override
    @EntityGraph(attributePaths = {"section", "variety"})
    List<Production> findAll(org.springframework.data.domain.Sort sort);

    @Override
    @EntityGraph(attributePaths = {"section", "variety"})
    Optional<Production> findById(Long id);
}
