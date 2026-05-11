package ElSilencioKoffee_Backend.checkout.services.impl;

import ElSilencioKoffee_Backend.cart.entities.Cart;
import ElSilencioKoffee_Backend.cart.entities.CartItem;
import ElSilencioKoffee_Backend.cart.repositories.CartRepository;
import ElSilencioKoffee_Backend.checkout.dto.CheckoutRequest;
import ElSilencioKoffee_Backend.checkout.dto.PaymentSimulationRequest;
import ElSilencioKoffee_Backend.checkout.dto.ShippingInformationRequest;
import ElSilencioKoffee_Backend.checkout.services.ICheckoutService;
import ElSilencioKoffee_Backend.inventory.services.IInventoryService;
import ElSilencioKoffee_Backend.orders.entities.DeliveryOrder;
import ElSilencioKoffee_Backend.orders.entities.DeliveryStatus;
import ElSilencioKoffee_Backend.orders.entities.Order;
import ElSilencioKoffee_Backend.orders.entities.OrderDetail;
import ElSilencioKoffee_Backend.orders.entities.OrderPayment;
import ElSilencioKoffee_Backend.orders.entities.OrderShippingInformation;
import ElSilencioKoffee_Backend.orders.entities.OrderStatus;
import ElSilencioKoffee_Backend.orders.entities.PaymentStatus;
import ElSilencioKoffee_Backend.orders.repositories.OrderRepository;
import ElSilencioKoffee_Backend.products.entities.Product;
import ElSilencioKoffee_Backend.users.entities.Usuario;
import ElSilencioKoffee_Backend.users.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CheckoutServiceImpl implements ICheckoutService {

    private static final DateTimeFormatter EXPIRATION_DATE_FORMAT = DateTimeFormatter.ofPattern("MM/uuuu");

    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final UsuarioRepository usuarioRepository;
    private final IInventoryService inventoryService;

    @Override
    @Transactional
    public Order checkout(String username, CheckoutRequest request) {
        Usuario usuario = findUserByUsername(username);
        Cart cart = findOwnedCart(username);
        validateCartNotEmpty(cart);

        ShippingInformationRequest shippingRequest = requireShippingInformation(request);
        PaymentSimulationRequest paymentRequest = requirePaymentRequest(request);
        PaymentSimulationResult paymentSimulation = simulateApprovedPayment(paymentRequest);
        Map<Long, Integer> quantitiesByProductId = buildQuantitiesByProductId(cart);
        inventoryService.ensureSufficientStock(quantitiesByProductId);

        Order order = new Order();
        order.setUsuario(usuario);
        order.setStatus(OrderStatus.PENDING);
        order.setNotes(normalizeOptionalText(request.getNotes(), 500));
        order.setTotalAmount(attachOrderDetails(order, cart));
        order.setShippingInformation(buildShippingInformation(shippingRequest));
        order.setPayment(buildApprovedPayment(paymentRequest, paymentSimulation));
        order.setDeliveryOrder(buildDeliveryOrder());

        Order savedOrder = orderRepository.save(order);
        inventoryService.consumeStockForOrder(savedOrder, username);
        savedOrder.setStatus(OrderStatus.PAID);
        clearPersistedCart(cart);
        return orderRepository.save(savedOrder);
    }

    private Usuario findUserByUsername(String username) {
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + username));
    }

    private Cart findOwnedCart(String username) {
        return cartRepository.findByUsuarioUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Cart is empty."));
    }

    private void validateCartNotEmpty(Cart cart) {
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cart is empty.");
        }
    }

    private ShippingInformationRequest requireShippingInformation(CheckoutRequest request) {
        if (request == null || request.getShippingInformation() == null) {
            throw new IllegalArgumentException("Shipping information is required");
        }
        return request.getShippingInformation();
    }

    private PaymentSimulationRequest requirePaymentRequest(CheckoutRequest request) {
        if (request == null || request.getPayment() == null) {
            throw new IllegalArgumentException("Payment information is required");
        }
        return request.getPayment();
    }

    private PaymentSimulationResult simulateApprovedPayment(PaymentSimulationRequest request) {
        requireTrimmedText(request.getCardholderName(), "Cardholder name is required", 120);
        String numericCardNumber = normalizeCardNumber(request.getCardNumber());
        parseAndValidateExpirationDate(request.getExpirationDate());
        validateCvv(request.getCvv());

        if (request.getPaymentMethod() == null) {
            throw new IllegalArgumentException("Payment method is required");
        }

        String maskedCardNumber = maskCardNumber(numericCardNumber);
        LocalDateTime paidAt = LocalDateTime.now();
        String transactionReference = "SIM-" + UUID.randomUUID().toString().replace("-", "")
                .substring(0, 12)
                .toUpperCase();

        return new PaymentSimulationResult(
                maskedCardNumber,
                paidAt,
                transactionReference
        );
    }

    private Map<Long, Integer> buildQuantitiesByProductId(Cart cart) {
        Map<Long, Integer> quantitiesByProductId = new LinkedHashMap<>();

        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();
            if (product == null || product.getId() == null) {
                throw new IllegalArgumentException("Cart contains an invalid product reference");
            }

            quantitiesByProductId.merge(product.getId(), item.getQuantity(), Integer::sum);
        }

        return quantitiesByProductId;
    }

    private BigDecimal attachOrderDetails(Order order, Cart cart) {
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            BigDecimal quantity = BigDecimal.valueOf(cartItem.getQuantity());
            BigDecimal unitPrice = product.getPrice();

            OrderDetail detail = new OrderDetail();
            detail.setProduct(product);
            detail.setQuantity(quantity);
            detail.setUnitPrice(unitPrice);
            order.addOrderDetail(detail);

            totalAmount = totalAmount.add(unitPrice.multiply(quantity));
        }

        return totalAmount;
    }

    private OrderShippingInformation buildShippingInformation(ShippingInformationRequest request) {
        OrderShippingInformation shippingInformation = new OrderShippingInformation();
        shippingInformation.setAddress(requireTrimmedText(request.getAddress(), "Shipping address is required", 255));
        shippingInformation.setCountry(requireTrimmedText(request.getCountry(), "Country is required", 100));
        shippingInformation.setCity(requireTrimmedText(request.getCity(), "City is required", 100));
        shippingInformation.setNeighborhood(
                requireTrimmedText(request.getNeighborhood(), "Neighborhood is required", 100)
        );
        shippingInformation.setReferenceDetails(normalizeOptionalText(request.getReferenceDetails(), 255));
        return shippingInformation;
    }

    private OrderPayment buildApprovedPayment(
            PaymentSimulationRequest request,
            PaymentSimulationResult paymentSimulation
    ) {
        OrderPayment payment = new OrderPayment();
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setMaskedCardNumber(paymentSimulation.maskedCardNumber());
        payment.setStatus(PaymentStatus.APPROVED);
        payment.setTransactionReference(paymentSimulation.transactionReference());
        payment.setPaidAt(paymentSimulation.paidAt());
        return payment;
    }

    private DeliveryOrder buildDeliveryOrder() {
        DeliveryOrder deliveryOrder = new DeliveryOrder();
        deliveryOrder.setStatus(DeliveryStatus.OUT_FOR_SHIPMENT);
        return deliveryOrder;
    }

    private void clearPersistedCart(Cart cart) {
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    private String requireTrimmedText(String value, String message, int maxLength) {
        String normalized = normalizeOptionalText(value, maxLength);
        if (normalized == null || normalized.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return normalized;
    }

    private String normalizeOptionalText(String value, int maxLength) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();
        if (normalized.isEmpty()) {
            return null;
        }
        if (normalized.length() > maxLength) {
            throw new IllegalArgumentException("Field exceeds maximum length of " + maxLength + " characters");
        }
        return normalized;
    }

    private String normalizeCardNumber(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Card number is required");
        }

        String normalized = value.replaceAll("\\s+", "");
        if (!normalized.matches("\\d{13,19}")) {
            throw new IllegalArgumentException("Card number must contain between 13 and 19 digits");
        }
        return normalized;
    }

    private YearMonth parseAndValidateExpirationDate(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Expiration date is required");
        }

        String normalized = value.trim();
        YearMonth expirationDate;

        try {
            if (normalized.matches("\\d{2}/\\d{2}")) {
                int month = Integer.parseInt(normalized.substring(0, 2));
                int year = Integer.parseInt("20" + normalized.substring(3, 5));
                expirationDate = YearMonth.of(year, month);
            } else if (normalized.matches("\\d{2}/\\d{4}")) {
                expirationDate = YearMonth.parse(normalized, EXPIRATION_DATE_FORMAT);
            } else {
                throw new IllegalArgumentException("Expiration date must use MM/YY or MM/YYYY format");
            }
        } catch (DateTimeParseException | NumberFormatException exception) {
            throw new IllegalArgumentException("Expiration date must use MM/YY or MM/YYYY format");
        }

        if (expirationDate.getMonthValue() < 1 || expirationDate.getMonthValue() > 12) {
            throw new IllegalArgumentException("Expiration date month is invalid");
        }

        if (expirationDate.atEndOfMonth().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Card is expired");
        }

        return expirationDate;
    }

    private void validateCvv(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("CVV is required");
        }

        String normalized = value.trim();
        if (!normalized.matches("\\d{3,4}")) {
            throw new IllegalArgumentException("CVV must contain 3 or 4 digits");
        }
    }

    private String maskCardNumber(String numericCardNumber) {
        String lastFourDigits = numericCardNumber.substring(numericCardNumber.length() - 4);
        return "**** **** **** " + lastFourDigits;
    }

    private record PaymentSimulationResult(
            String maskedCardNumber,
            LocalDateTime paidAt,
            String transactionReference
    ) {
    }
}
