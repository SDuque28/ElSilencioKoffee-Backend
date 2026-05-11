package ElSilencioKoffee_Backend.checkout.controllers;

import ElSilencioKoffee_Backend.checkout.dto.CheckoutRequest;
import ElSilencioKoffee_Backend.checkout.dto.CheckoutResponse;
import ElSilencioKoffee_Backend.checkout.services.ICheckoutService;
import ElSilencioKoffee_Backend.orders.dto.OrderResponseMapper;
import ElSilencioKoffee_Backend.orders.entities.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final ICheckoutService checkoutService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CheckoutResponse> checkout(
            Authentication authentication,
            @RequestBody CheckoutRequest request
    ) {
        Order order = checkoutService.checkout(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(OrderResponseMapper.toCheckoutResponse(order));
    }
}
