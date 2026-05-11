package ElSilencioKoffee_Backend.production.services;

import ElSilencioKoffee_Backend.production.dto.ProductionCreateRequest;
import ElSilencioKoffee_Backend.production.dto.ProductionResponse;
import ElSilencioKoffee_Backend.production.dto.ProductionUpdateRequest;

import java.util.List;

public interface IProductionService {

    ProductionResponse create(ProductionCreateRequest request);

    List<ProductionResponse> findAll();

    ProductionResponse findById(Long id);

    ProductionResponse update(Long id, ProductionUpdateRequest request);

    void delete(Long id);
}
