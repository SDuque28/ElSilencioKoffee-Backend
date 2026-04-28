package ElSilencioKoffee_Backend.production.controllers;

import ElSilencioKoffee_Backend.production.dto.ProductionCreateRequest;
import ElSilencioKoffee_Backend.production.dto.ProductionResponse;
import ElSilencioKoffee_Backend.production.dto.ProductionUpdateRequest;
import ElSilencioKoffee_Backend.production.services.IProductionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/production")
@RequiredArgsConstructor
public class ProductionController {

    private final IProductionService productionService;

    @GetMapping
    public ResponseEntity<List<ProductionResponse>> findAll() {
        return ResponseEntity.ok(productionService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductionResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(productionService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductionResponse> create(@RequestBody ProductionCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productionService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductionResponse> update(@PathVariable Long id, @RequestBody ProductionUpdateRequest request) {
        return ResponseEntity.ok(productionService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
