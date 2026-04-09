package ElSilencioKoffee_Backend.controllers;

import ElSilencioKoffee_Backend.dto.RolCreateRequest;
import ElSilencioKoffee_Backend.dto.RolResponse;
import ElSilencioKoffee_Backend.dto.RolUpdateRequest;
import ElSilencioKoffee_Backend.entities.Rol;
import ElSilencioKoffee_Backend.services.IRolService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RolController {

    private final IRolService rolService;

    @GetMapping
    public ResponseEntity<List<RolResponse>> findAll() {
        List<RolResponse> response = rolService.findAll().stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RolResponse> findById(@PathVariable Long id) {
        return rolService.findById(id)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<RolResponse> save(@RequestBody RolCreateRequest request) {
        Rol rol = new Rol();
        rol.setNombre(request.getNombre());

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(rolService.save(rol)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RolResponse> update(@PathVariable Long id, @RequestBody RolUpdateRequest request) {
        Rol rol = new Rol();
        rol.setNombre(request.getNombre());

        return rolService.update(id, rol)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return rolService.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    private RolResponse toResponse(Rol rol) {
        RolResponse response = new RolResponse();
        response.setId(rol.getId());
        response.setNombre(rol.getNombre());
        return response;
    }
}
