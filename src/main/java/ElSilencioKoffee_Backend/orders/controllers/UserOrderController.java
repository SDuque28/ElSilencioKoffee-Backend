package ElSilencioKoffee_Backend.orders.controllers;

import ElSilencioKoffee_Backend.orders.dto.OrderResponse;
import ElSilencioKoffee_Backend.orders.dto.OrderResponseMapper;
import ElSilencioKoffee_Backend.orders.entities.Order;
import ElSilencioKoffee_Backend.orders.services.IOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users/me/orders")
@RequiredArgsConstructor
public class UserOrderController {

    private final IOrderService orderService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<OrderResponse>> findMyOrders(Authentication authentication) {
        List<OrderResponse> response = orderService.findOrdersByUsername(authentication.getName()).stream()
                .map(OrderResponseMapper::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }
}

