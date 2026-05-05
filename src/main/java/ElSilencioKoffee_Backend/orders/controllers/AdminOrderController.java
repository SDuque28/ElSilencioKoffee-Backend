package ElSilencioKoffee_Backend.orders.controllers;

import ElSilencioKoffee_Backend.orders.dto.AdminOrderDetailResponse;
import ElSilencioKoffee_Backend.orders.dto.OrderResponseMapper;
import ElSilencioKoffee_Backend.orders.services.IOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final IOrderService orderService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AdminOrderDetailResponse>> findOrders() {
        List<AdminOrderDetailResponse> response = orderService.findAllOrders().stream()
                .map(OrderResponseMapper::toAdminDetailResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminOrderDetailResponse> findOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(OrderResponseMapper.toAdminDetailResponse(orderService.findOrderById(id)));
    }
}
