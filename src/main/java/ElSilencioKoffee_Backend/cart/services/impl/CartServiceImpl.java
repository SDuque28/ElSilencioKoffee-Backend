package ElSilencioKoffee_Backend.cart.services.impl;

import ElSilencioKoffee_Backend.cart.dto.AddCartItemRequest;
import ElSilencioKoffee_Backend.cart.dto.CartItemResponse;
import ElSilencioKoffee_Backend.cart.dto.CartResponse;
import ElSilencioKoffee_Backend.cart.dto.UpdateCartItemRequest;
import ElSilencioKoffee_Backend.cart.entities.Cart;
import ElSilencioKoffee_Backend.cart.entities.CartItem;
import ElSilencioKoffee_Backend.cart.repositories.CartItemRepository;
import ElSilencioKoffee_Backend.cart.repositories.CartRepository;
import ElSilencioKoffee_Backend.cart.services.ICartService;
import ElSilencioKoffee_Backend.products.entities.Product;
import ElSilencioKoffee_Backend.products.repositories.ProductRepository;
import ElSilencioKoffee_Backend.users.entities.Usuario;
import ElSilencioKoffee_Backend.users.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements ICartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public CartResponse getCartForUsername(String username) {
        Usuario usuario = findUserByUsername(username);
        return cartRepository.findByUsuarioUsername(username)
                .map(this::toResponse)
                .orElseGet(() -> emptyCartResponse(usuario));
    }

    @Override
    @Transactional
    public CartResponse addItem(String username, AddCartItemRequest request) {
        Usuario usuario = findUserByUsername(username);
        Long productId = requireProductId(request.getProductId());
        Integer quantity = requireQuantity(request.getQuantity());
        Product product = findProduct(productId);
        Cart cart = findOrCreateCart(usuario);

        Optional<CartItem> existingItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId);
        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            cartItemRepository.save(item);
        } else {
            CartItem item = new CartItem();
            item.setProduct(product);
            item.setQuantity(quantity);
            cart.addItem(item);
        }

        return toResponse(cartRepository.save(cart));
    }

    @Override
    @Transactional
    public CartResponse updateItem(String username, Long itemId, UpdateCartItemRequest request) {
        CartItem item = findOwnedItem(itemId, username);
        item.setQuantity(requireQuantity(request.getQuantity()));
        cartItemRepository.save(item);
        return toResponse(item.getCart());
    }

    @Override
    @Transactional
    public CartResponse removeItem(String username, Long itemId) {
        CartItem item = findOwnedItem(itemId, username);
        Cart cart = item.getCart();
        cart.getItems().remove(item);
        cartItemRepository.delete(item);
        return toResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse clearCart(String username) {
        Usuario usuario = findUserByUsername(username);
        Optional<Cart> cart = cartRepository.findByUsuarioUsername(username);
        if (cart.isEmpty()) {
            return emptyCartResponse(usuario);
        }

        cartRepository.delete(cart.get());
        return emptyCartResponse(usuario);
    }

    private Usuario findUserByUsername(String username) {
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + username));
    }

    private Product findProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new NoSuchElementException("Product not found: " + productId));
    }

    private Cart findOrCreateCart(Usuario usuario) {
        return cartRepository.findByUsuarioUsername(usuario.getUsername())
                .orElseGet(() -> {
                    Cart cart = new Cart();
                    cart.setUsuario(usuario);
                    return cartRepository.save(cart);
                });
    }

    private CartItem findOwnedItem(Long itemId, String username) {
        if (itemId == null || itemId <= 0) {
            throw new IllegalArgumentException("Cart item ID must be greater than 0");
        }

        return cartItemRepository.findByIdAndCartUsuarioUsername(itemId, username)
                .orElseThrow(() -> new NoSuchElementException("Cart item not found: " + itemId));
    }

    private Long requireProductId(Long productId) {
        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException("Product ID must be greater than 0");
        }
        return productId;
    }

    private Integer requireQuantity(Integer quantity) {
        if (quantity == null) {
            throw new IllegalArgumentException("Quantity is required");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        return quantity;
    }

    private CartResponse emptyCartResponse(Usuario usuario) {
        CartResponse response = new CartResponse();
        response.setUserId(usuario.getId());
        response.setTotalItems(0);
        response.setTotalAmount(BigDecimal.ZERO);
        return response;
    }

    private CartResponse toResponse(Cart cart) {
        CartResponse response = new CartResponse();
        response.setId(cart.getId());
        response.setUserId(cart.getUsuario().getId());

        List<CartItemResponse> items = new ArrayList<>();
        int totalItems = 0;
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartItem item : cart.getItems()) {
            CartItemResponse itemResponse = new CartItemResponse();
            itemResponse.setId(item.getId());
            itemResponse.setProductId(item.getProduct().getId());
            itemResponse.setProductName(item.getProduct().getName());
            itemResponse.setImageUrl(item.getProduct().getImageUrl());
            itemResponse.setUnitPrice(item.getProduct().getPrice());
            itemResponse.setQuantity(item.getQuantity());

            BigDecimal subtotal = item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            itemResponse.setSubtotal(subtotal);
            items.add(itemResponse);

            totalItems += item.getQuantity();
            totalAmount = totalAmount.add(subtotal);
        }

        response.setItems(items);
        response.setTotalItems(totalItems);
        response.setTotalAmount(totalAmount);
        return response;
    }
}
