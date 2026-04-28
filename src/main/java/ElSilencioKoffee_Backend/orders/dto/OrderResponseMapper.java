package ElSilencioKoffee_Backend.orders.dto;

import ElSilencioKoffee_Backend.orders.entities.Order;
import ElSilencioKoffee_Backend.orders.entities.OrderDetail;

public final class OrderResponseMapper {

    private OrderResponseMapper() {
    }

    public static OrderResponse toResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setUserId(order.getUsuario().getId());
        response.setOrderDate(order.getOrderDate());
        response.setTotalAmount(order.getTotalAmount());
        response.setStatus(order.getStatus());
        response.setItems(order.getOrderDetails().stream()
                .map(OrderResponseMapper::toDetailResponse)
                .toList());
        return response;
    }

    public static OrderCreateResponse toCreateResponse(Order order) {
        OrderCreateResponse response = new OrderCreateResponse();
        response.setId(order.getId());
        response.setUserId(order.getUsuario().getId());
        response.setOrderDate(order.getOrderDate());
        response.setTotalAmount(order.getTotalAmount());
        response.setStatus(order.getStatus());
        response.setItems(order.getOrderDetails().stream()
                .map(OrderResponseMapper::toDetailResponse)
                .toList());
        return response;
    }

    public static OrderDetailResponse toDetailResponse(OrderDetail orderDetail) {
        OrderDetailResponse response = new OrderDetailResponse();
        response.setDetailId(orderDetail.getId());
        response.setProductId(orderDetail.getProduct().getId());
        response.setProductName(orderDetail.getProduct().getName());
        response.setQuantity(orderDetail.getQuantity().intValueExact());
        response.setUnitPrice(orderDetail.getUnitPrice());
        response.setSubtotal(orderDetail.getUnitPrice().multiply(orderDetail.getQuantity()));
        return response;
    }
}
