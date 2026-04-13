package ElSilencioKoffee_Backend.controllers;

import ElSilencioKoffee_Backend.dto.OrderCreateRequest;
import ElSilencioKoffee_Backend.dto.OrderResponse;
import ElSilencioKoffee_Backend.dto.OrderStatusUpdateRequest;
import ElSilencioKoffee_Backend.entities.Order;
import ElSilencioKoffee_Backend.services.IOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final IOrderService orderService;

    @PostMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<OrderResponse> createOrder(Authentication authentication,
                                                     @RequestBody OrderCreateRequest request) {
        Order order = orderService.createOrder(authentication.getName(), request.getTotalAmount());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(order));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<Page<OrderResponse>> findOrders(Authentication authentication,
                                                          @RequestParam(defaultValue = "1") int page,
                                                          @RequestParam(defaultValue = "10") int limit) {
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

        Page<OrderResponse> response = orderService.findOrdersByUsername(authentication.getName(), pageable)
                .map(this::toResponse);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<OrderResponse> findOrderById(Authentication authentication, @PathVariable Long id) {
        Order order = orderService.findOrderByIdForUsername(id, authentication.getName());
        return ResponseEntity.ok(toResponse(order));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<OrderResponse> updateStatus(@PathVariable Long id,
                                                      @RequestBody OrderStatusUpdateRequest request) {
        Order order = orderService.updateStatus(id, request.getStatus());
        return ResponseEntity.ok(toResponse(order));
    }

    private OrderResponse toResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setUserId(order.getUsuario().getId());
        response.setOrderDate(order.getOrderDate());
        response.setTotalAmount(order.getTotalAmount());
        response.setStatus(order.getStatus());
        return response;
    }
}
