package ElSilencioKoffee_Backend.products.services;

import ElSilencioKoffee_Backend.products.dto.ProductCreateRequest;
import ElSilencioKoffee_Backend.products.dto.ProductResponse;
import ElSilencioKoffee_Backend.products.dto.ProductUpdateRequest;

import java.util.List;

public interface IProductService {

    ProductResponse create(ProductCreateRequest request);

    List<ProductResponse> findAll();

    ProductResponse findById(Long id);

    ProductResponse update(Long id, ProductUpdateRequest request);

    void delete(Long id);
}
