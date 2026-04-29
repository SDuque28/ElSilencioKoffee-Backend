package ElSilencioKoffee_Backend.cart.controllers;

import ElSilencioKoffee_Backend.cart.dto.AddCartItemRequest;
import ElSilencioKoffee_Backend.cart.dto.CartResponse;
import ElSilencioKoffee_Backend.cart.dto.UpdateCartItemRequest;
import ElSilencioKoffee_Backend.cart.services.ICartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@PreAuthorize("isAuthenticated()")
@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final ICartService cartService;

    @GetMapping
    public ResponseEntity<CartResponse> getCart(Authentication authentication) {
        return ResponseEntity.ok(cartService.getCartForUsername(authentication.getName()));
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItem(
            Authentication authentication,
            @RequestBody AddCartItemRequest request
    ) {
        return ResponseEntity.ok(cartService.addItem(authentication.getName(), request));
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> updateItem(
            Authentication authentication,
            @PathVariable Long itemId,
            @RequestBody UpdateCartItemRequest request
    ) {
        return ResponseEntity.ok(cartService.updateItem(authentication.getName(), itemId, request));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> removeItem(Authentication authentication, @PathVariable Long itemId) {
        return ResponseEntity.ok(cartService.removeItem(authentication.getName(), itemId));
    }

    @DeleteMapping
    public ResponseEntity<CartResponse> clearCart(Authentication authentication) {
        return ResponseEntity.ok(cartService.clearCart(authentication.getName()));
    }
}
