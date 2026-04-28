package ElSilencioKoffee_Backend.environment.services;

import ElSilencioKoffee_Backend.environment.dto.EnvironmentMetricCreateRequest;
import ElSilencioKoffee_Backend.environment.dto.EnvironmentMetricResponse;
import ElSilencioKoffee_Backend.environment.dto.EnvironmentMetricUpdateRequest;

import java.time.LocalDateTime;
import java.util.List;

public interface IEnvironmentMetricService {

    EnvironmentMetricResponse create(EnvironmentMetricCreateRequest request);

    List<EnvironmentMetricResponse> findAll(String type, Long sectionId, LocalDateTime fromDate, LocalDateTime toDate);

    EnvironmentMetricResponse findById(Long id);

    EnvironmentMetricResponse update(Long id, EnvironmentMetricUpdateRequest request);

    void delete(Long id);
}
