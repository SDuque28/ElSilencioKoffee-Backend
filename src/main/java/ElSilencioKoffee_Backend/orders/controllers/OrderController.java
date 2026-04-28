package ElSilencioKoffee_Backend.orders.controllers;

import ElSilencioKoffee_Backend.orders.dto.OrderCreateRequest;
import ElSilencioKoffee_Backend.orders.dto.OrderCreateResponse;
import ElSilencioKoffee_Backend.orders.dto.OrderResponse;
import ElSilencioKoffee_Backend.orders.dto.OrderResponseMapper;
import ElSilencioKoffee_Backend.orders.dto.OrderStatusUpdateRequest;
import ElSilencioKoffee_Backend.orders.entities.Order;
import ElSilencioKoffee_Backend.orders.services.IOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final IOrderService orderService;

    @PostMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<OrderCreateResponse> createOrder(Authentication authentication,
                                                           @RequestBody OrderCreateRequest request) {
        Order order = orderService.createOrder(authentication.getName(), request.getItems());
        return ResponseEntity.status(HttpStatus.CREATED).body(OrderResponseMapper.toCreateResponse(order));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> findOrders(@RequestParam(required = false) Integer page,
                                        @RequestParam(required = false) Integer limit) {
        if (page == null && limit == null) {
            List<OrderResponse> response = orderService.findAllOrders().stream()
                    .map(OrderResponseMapper::toResponse)
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
                Sort.by(Sort.Direction.DESC, "orderDate", "id")
        );

        Page<OrderResponse> response = orderService.findAllOrders(pageable)
                .map(OrderResponseMapper::toResponse);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OrderResponse> findOrderById(Authentication authentication, @PathVariable Long id) {
        Order order = isAdmin(authentication)
                ? orderService.findOrderById(id)
                : orderService.findOrderByIdForUsername(id, authentication.getName());
        return ResponseEntity.ok(OrderResponseMapper.toResponse(order));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> updateStatus(@PathVariable Long id,
                                                      @RequestBody OrderStatusUpdateRequest request) {
        Order order = orderService.updateStatus(id, request.getStatus());
        return ResponseEntity.ok(OrderResponseMapper.toResponse(order));
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
    }
}

