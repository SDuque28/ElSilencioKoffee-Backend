package ElSilencioKoffee_Backend.userroles.controllers;

import ElSilencioKoffee_Backend.roles.entities.Rol;
import ElSilencioKoffee_Backend.userroles.dto.UsuarioRolCreateRequest;
import ElSilencioKoffee_Backend.userroles.dto.UsuarioRolResponse;
import ElSilencioKoffee_Backend.userroles.entities.UsuarioRol;
import ElSilencioKoffee_Backend.userroles.entities.UsuarioRolId;
import ElSilencioKoffee_Backend.userroles.services.IUsuarioRolService;
import ElSilencioKoffee_Backend.users.entities.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/usuario-roles")
@RequiredArgsConstructor
public class UsuarioRolController {

    private final IUsuarioRolService usuarioRolService;

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<UsuarioRolResponse>> findByUsuarioId(@PathVariable Long usuarioId) {
        List<UsuarioRolResponse> response = usuarioRolService.findByUsuarioId(usuarioId).stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/rol/{rolId}")
    public ResponseEntity<List<UsuarioRolResponse>> findByRolId(@PathVariable Long rolId) {
        List<UsuarioRolResponse> response = usuarioRolService.findByRolId(rolId).stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<UsuarioRolResponse> save(@RequestBody UsuarioRolCreateRequest request) {
        Usuario usuario = new Usuario();
        usuario.setId(request.getUsuarioId());

        Rol rol = new Rol();
        rol.setId(request.getRolId());

        UsuarioRol usuarioRol = new UsuarioRol();
        usuarioRol.setUsuario(usuario);
        usuarioRol.setRol(rol);

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(usuarioRolService.save(usuarioRol)));
    }

    @DeleteMapping("/{usuarioId}/{rolId}")
    public ResponseEntity<Void> delete(@PathVariable Long usuarioId, @PathVariable Long rolId) {
        UsuarioRolId id = new UsuarioRolId(usuarioId, rolId);
        return usuarioRolService.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    private UsuarioRolResponse toResponse(UsuarioRol ur) {
        UsuarioRolResponse response = new UsuarioRolResponse();
        if (ur.getId() != null) {
            response.setUsuarioId(ur.getId().getUsuarioId());
            response.setRolId(ur.getId().getRolId());
        }
        if (ur.getUsuario() != null) {
            response.setUsername(ur.getUsuario().getUsername());
        }
        if (ur.getRol() != null) {
            response.setRolNombre(ur.getRol().getNombre());
        }
        return response;
    }
}

