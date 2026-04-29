package ElSilencioKoffee_Backend.cart.services;

import ElSilencioKoffee_Backend.cart.dto.AddCartItemRequest;
import ElSilencioKoffee_Backend.cart.dto.CartResponse;
import ElSilencioKoffee_Backend.cart.dto.UpdateCartItemRequest;

public interface ICartService {

    CartResponse getCartForUsername(String username);

    CartResponse addItem(String username, AddCartItemRequest request);

    CartResponse updateItem(String username, Long itemId, UpdateCartItemRequest request);

    CartResponse removeItem(String username, Long itemId);

    CartResponse clearCart(String username);
}
