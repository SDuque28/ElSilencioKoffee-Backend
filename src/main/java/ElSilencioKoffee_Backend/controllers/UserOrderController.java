package ElSilencioKoffee_Backend.controllers;

import ElSilencioKoffee_Backend.dto.OrderResponse;
import ElSilencioKoffee_Backend.entities.Order;
import ElSilencioKoffee_Backend.services.IOrderService;
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
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(response);
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
