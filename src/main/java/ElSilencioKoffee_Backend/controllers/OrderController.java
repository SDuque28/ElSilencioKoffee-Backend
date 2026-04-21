package ElSilencioKoffee_Backend.controllers;

import ElSilencioKoffee_Backend.dto.OrderCreateResponse;
import ElSilencioKoffee_Backend.dto.OrderCreateRequest;
import ElSilencioKoffee_Backend.dto.OrderItemResponse;
import ElSilencioKoffee_Backend.dto.OrderResponse;
import ElSilencioKoffee_Backend.dto.OrderStatusUpdateRequest;
import ElSilencioKoffee_Backend.entities.OrderDetail;
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
        return ResponseEntity.status(HttpStatus.CREATED).body(toCreateResponse(order));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> findOrders(@RequestParam(required = false) Integer page,
                                        @RequestParam(required = false) Integer limit) {
        if (page == null && limit == null) {
            List<OrderResponse> response = orderService.findAllOrders().stream()
                    .map(this::toResponse)
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

    private OrderCreateResponse toCreateResponse(Order order) {
        OrderCreateResponse response = new OrderCreateResponse();
        response.setId(order.getId());
        response.setUserId(order.getUsuario().getId());
        response.setOrderDate(order.getOrderDate());
        response.setTotalAmount(order.getTotalAmount());
        response.setStatus(order.getStatus());
        response.setItems(order.getOrderDetails().stream()
                .map(this::toItemResponse)
                .toList());
        return response;
    }

    private OrderItemResponse toItemResponse(OrderDetail orderDetail) {
        OrderItemResponse response = new OrderItemResponse();
        response.setProductId(orderDetail.getProduct().getId());
        response.setQuantity(orderDetail.getQuantity().intValueExact());
        response.setUnitPrice(orderDetail.getUnitPrice());
        response.setSubtotal(orderDetail.getUnitPrice().multiply(orderDetail.getQuantity()));
        return response;
    }
}
