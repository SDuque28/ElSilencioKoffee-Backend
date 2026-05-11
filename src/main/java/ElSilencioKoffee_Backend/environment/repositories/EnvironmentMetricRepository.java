package ElSilencioKoffee_Backend.environment.repositories;

import ElSilencioKoffee_Backend.environment.entities.EnvironmentMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface EnvironmentMetricRepository
        extends JpaRepository<EnvironmentMetric, Long>, JpaSpecificationExecutor<EnvironmentMetric> {
}
