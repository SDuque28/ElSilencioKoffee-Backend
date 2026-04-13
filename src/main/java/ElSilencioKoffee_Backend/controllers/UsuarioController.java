package ElSilencioKoffee_Backend.controllers;

import ElSilencioKoffee_Backend.dto.OrderResponse;
import ElSilencioKoffee_Backend.dto.UserResponse;
import ElSilencioKoffee_Backend.dto.UserUpdateRequest;
import ElSilencioKoffee_Backend.entities.Order;
import ElSilencioKoffee_Backend.entities.Usuario;
import ElSilencioKoffee_Backend.services.IUsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UsuarioController {

    private final IUsuarioService usuarioService;

    @GetMapping
    public ResponseEntity<?> findAll(@RequestParam(required = false) Integer page,
                                     @RequestParam(required = false) Integer limit) {
        if (page == null && limit == null) {
            List<UserResponse> response = usuarioService.findAll().stream()
                    .map(this::toUserResponse)
                    .toList();
            return ResponseEntity.ok(response);
        }

        if (page == null || limit == null) {
            throw new IllegalArgumentException("Page and limit must be provided together");
        }
        if (page < 1) {
            throw new IllegalArgumentException("Page must be greater than or equal to 1");
        }
        if (limit < 1) {
            throw new IllegalArgumentException("Limit must be greater than 0");
        }

        Pageable pageable = PageRequest.of(
                page - 1,
                limit,
                Sort.by(Sort.Direction.ASC, "id")
        );

        Page<UserResponse> response = usuarioService.findAll(pageable).map(this::toUserResponse);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(toUserResponse(usuarioService.findById(id)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserResponse> update(@PathVariable Long id, @RequestBody UserUpdateRequest request) {
        Usuario usuario = usuarioService.update(id, request.getUsername(), request.getEmail(), request.getActivo());
        return ResponseEntity.ok(toUserResponse(usuario));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDelete(@PathVariable Long id) {
        usuarioService.softDelete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/orders")
    public ResponseEntity<List<OrderResponse>> findOrdersByUserId(@PathVariable Long id) {
        List<OrderResponse> response = usuarioService.findOrdersByUserId(id).stream()
                .map(this::toOrderResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    private UserResponse toUserResponse(Usuario usuario) {
        UserResponse response = new UserResponse();
        response.setId(usuario.getId());
        response.setUsername(usuario.getUsername());
        response.setEmail(usuario.getEmail());
        response.setActivo(usuario.getActivo());
        response.setCreatedAt(usuario.getCreatedAt());
        return response;
    }

    private OrderResponse toOrderResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setUserId(order.getUsuario().getId());
        response.setOrderDate(order.getOrderDate());
        response.setTotalAmount(order.getTotalAmount());
        response.setStatus(order.getStatus());
        return response;
    }
}
