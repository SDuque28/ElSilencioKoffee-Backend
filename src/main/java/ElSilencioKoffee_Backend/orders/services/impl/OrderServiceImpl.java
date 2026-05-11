package ElSilencioKoffee_Backend.orders.services.impl;

import ElSilencioKoffee_Backend.orders.dto.OrderCreateItemRequest;
import ElSilencioKoffee_Backend.orders.entities.DeliveryOrder;
import ElSilencioKoffee_Backend.orders.entities.DeliveryStatus;
import ElSilencioKoffee_Backend.orders.entities.Order;
import ElSilencioKoffee_Backend.orders.entities.OrderDetail;
import ElSilencioKoffee_Backend.orders.entities.OrderStatus;
import ElSilencioKoffee_Backend.orders.repositories.OrderRepository;
import ElSilencioKoffee_Backend.orders.services.IOrderService;
import ElSilencioKoffee_Backend.inventory.services.IInventoryService;
import ElSilencioKoffee_Backend.products.entities.Product;
import ElSilencioKoffee_Backend.products.repositories.ProductRepository;
import ElSilencioKoffee_Backend.users.entities.Usuario;
import ElSilencioKoffee_Backend.users.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements IOrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UsuarioRepository usuarioRepository;
    private final IInventoryService inventoryService;

    @Override
    @Transactional
    public Order createOrder(String username, List<OrderCreateItemRequest> items) {
        Map<Long, Integer> normalizedItems = normalizeItems(items);
        Usuario usuario = findUserByUsername(username);
        Map<Long, Product> productsById = findProductsById(normalizedItems.keySet());
        inventoryService.ensureSufficientStock(normalizedItems);

        Order order = new Order();
        order.setUsuario(usuario);
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(calculateAndAttachOrderDetails(order, normalizedItems, productsById));

        return orderRepository.save(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findAllOrders() {
        return orderRepository.findAll(Sort.by(Sort.Direction.DESC, "orderDate", "id"));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Order> findAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findOrdersByUsername(String username) {
        Usuario usuario = findUserByUsername(username);
        return orderRepository.findAllByUsuarioId(
                usuario.getId(),
                Sort.by(Sort.Direction.DESC, "orderDate", "id")
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Order> findOrdersByUsername(String username, Pageable pageable) {
        Usuario usuario = findUserByUsername(username);
        return orderRepository.findByUsuarioId(usuario.getId(), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Order findOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Order not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Order findOrderByIdForUsername(Long id, String username) {
        Usuario usuario = findUserByUsername(username);
        return orderRepository.findByIdAndUsuarioId(id, usuario.getId())
                .orElseThrow(() -> new NoSuchElementException("Order not found: " + id));
    }

    @Override
    @Transactional
    public Order updateStatus(Long id, OrderStatus status, String actorUsername) {
        if (status == null) {
            throw new IllegalArgumentException("Order status is required");
        }

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Order not found: " + id));

        return performStatusTransition(order, status, actorUsername);
    }

    @Override
    @Transactional
    public Order payOrder(Long id, String username, boolean isAdmin) {
        Order order = isAdmin
                ? findOrderById(id)
                : findOrderByIdForUsername(id, username);
        return performStatusTransition(order, OrderStatus.PAID, username);
    }

    @Override
    @Transactional
    public Order updateDeliveryStatus(Long id, DeliveryStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Delivery status is required");
        }

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Order not found: " + id));
        DeliveryOrder deliveryOrder = order.getDeliveryOrder();

        if (deliveryOrder == null) {
            throw new IllegalArgumentException("Delivery status is not available for order: " + id);
        }

        validateDeliveryStatusTransition(order.getStatus(), deliveryOrder.getStatus(), status);
        deliveryOrder.setStatus(status);
        return orderRepository.save(order);
    }

    private Order performStatusTransition(Order order, OrderStatus nextStatus, String actorUsername) {
        validateStatusTransition(order.getStatus(), nextStatus);

        if (nextStatus == OrderStatus.PAID) {
            inventoryService.consumeStockForOrder(order, actorUsername);
        }

        order.setStatus(nextStatus);
        return orderRepository.save(order);
    }

    private Usuario findUserByUsername(String username) {
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + username));
    }

    private Map<Long, Integer> normalizeItems(List<OrderCreateItemRequest> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }

        Map<Long, Integer> normalizedItems = new LinkedHashMap<>();

        for (OrderCreateItemRequest item : items) {
            if (item == null) {
                throw new IllegalArgumentException("Order items must not be null");
            }
            if (item.getProductId() == null) {
                throw new IllegalArgumentException("Product ID is required");
            }
            if (item.getProductId() <= 0) {
                throw new IllegalArgumentException("Product ID must be greater than 0");
            }
            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new IllegalArgumentException("Item quantity must be greater than 0");
            }

            normalizedItems.merge(item.getProductId(), item.getQuantity(), Integer::sum);
        }

        return normalizedItems;
    }

    private Map<Long, Product> findProductsById(Iterable<Long> productIds) {
        List<Product> products = productRepository.findAllById(productIds);
        Map<Long, Product> productsById = new LinkedHashMap<>();

        for (Product product : products) {
            productsById.put(product.getId(), product);
        }

        for (Long productId : productIds) {
            if (!productsById.containsKey(productId)) {
                throw new NoSuchElementException("Product not found: " + productId);
            }
        }

        return productsById;
    }

    private BigDecimal calculateAndAttachOrderDetails(
            Order order,
            Map<Long, Integer> normalizedItems,
            Map<Long, Product> productsById
    ) {
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (Map.Entry<Long, Integer> entry : normalizedItems.entrySet()) {
            Product product = productsById.get(entry.getKey());
            BigDecimal quantity = BigDecimal.valueOf(entry.getValue());
            BigDecimal unitPrice = product.getPrice();

            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setProduct(product);
            orderDetail.setQuantity(quantity);
            orderDetail.setUnitPrice(unitPrice);
            order.addOrderDetail(orderDetail);

            totalAmount = totalAmount.add(unitPrice.multiply(quantity));
        }

        return totalAmount;
    }

    private void validateStatusTransition(OrderStatus currentStatus, OrderStatus nextStatus) {
        if (currentStatus == nextStatus) {
            throw new IllegalArgumentException("Order is already in status: " + nextStatus.toJson());
        }

        if (currentStatus == OrderStatus.PAID) {
            throw new IllegalArgumentException(
                    "Invalid order status transition: " + currentStatus.toJson() + " -> " + nextStatus.toJson()
            );
        }

        if (currentStatus == OrderStatus.PENDING && nextStatus == OrderStatus.PAID) {
            return;
        }

        throw new IllegalArgumentException(
                "Invalid order status transition: " + currentStatus.toJson() + " -> " + nextStatus.toJson()
        );
    }

    private void validateDeliveryStatusTransition(
            OrderStatus orderStatus,
            DeliveryStatus currentStatus,
            DeliveryStatus nextStatus
    ) {
        if (orderStatus != OrderStatus.PAID) {
            throw new IllegalArgumentException("Delivery status can only be updated for paid orders");
        }

        if (currentStatus == nextStatus) {
            throw new IllegalArgumentException("Delivery order is already in status: " + nextStatus.toJson());
        }

        if (currentStatus == DeliveryStatus.DELIVERED || currentStatus == DeliveryStatus.CANCELLED) {
            throw new IllegalArgumentException(
                    "Invalid delivery status transition: " + currentStatus.toJson() + " -> " + nextStatus.toJson()
            );
        }

        if (currentStatus == DeliveryStatus.PENDING
                && (nextStatus == DeliveryStatus.OUT_FOR_SHIPMENT || nextStatus == DeliveryStatus.CANCELLED)) {
            return;
        }

        if (currentStatus == DeliveryStatus.OUT_FOR_SHIPMENT
                && (nextStatus == DeliveryStatus.DELIVERED || nextStatus == DeliveryStatus.CANCELLED)) {
            return;
        }

        throw new IllegalArgumentException(
                "Invalid delivery status transition: " + currentStatus.toJson() + " -> " + nextStatus.toJson()
        );
    }
}

