package ElSilencioKoffee_Backend.orders.dto;

import ElSilencioKoffee_Backend.checkout.dto.CheckoutResponse;
import ElSilencioKoffee_Backend.orders.entities.Order;
import ElSilencioKoffee_Backend.orders.entities.OrderDetail;
import ElSilencioKoffee_Backend.orders.entities.OrderPayment;
import ElSilencioKoffee_Backend.orders.entities.OrderShippingInformation;
import ElSilencioKoffee_Backend.orders.entities.DeliveryOrder;

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
        response.setNotes(order.getNotes());
        response.setItems(order.getOrderDetails().stream()
                .map(OrderResponseMapper::toDetailResponse)
                .toList());
        response.setShippingInformation(toShippingResponse(order.getShippingInformation()));
        response.setPayment(toPaymentResponse(order.getPayment()));
        response.setDeliveryOrder(toDeliveryResponse(order.getDeliveryOrder()));
        response.setCustomer(toCustomerResponse(order));
        return response;
    }

    public static OrderCreateResponse toCreateResponse(Order order) {
        OrderCreateResponse response = new OrderCreateResponse();
        response.setId(order.getId());
        response.setUserId(order.getUsuario().getId());
        response.setOrderDate(order.getOrderDate());
        response.setTotalAmount(order.getTotalAmount());
        response.setStatus(order.getStatus());
        response.setNotes(order.getNotes());
        response.setItems(order.getOrderDetails().stream()
                .map(OrderResponseMapper::toDetailResponse)
                .toList());
        response.setShippingInformation(toShippingResponse(order.getShippingInformation()));
        response.setPayment(toPaymentResponse(order.getPayment()));
        response.setDeliveryOrder(toDeliveryResponse(order.getDeliveryOrder()));
        return response;
    }

    public static AdminOrderDetailResponse toAdminDetailResponse(Order order) {
        AdminOrderDetailResponse response = new AdminOrderDetailResponse();
        response.setId(order.getId());
        response.setUserId(order.getUsuario().getId());
        response.setOrderDate(order.getOrderDate());
        response.setTotalAmount(order.getTotalAmount());
        response.setStatus(order.getStatus());
        response.setNotes(order.getNotes());
        response.setItems(order.getOrderDetails().stream()
                .map(OrderResponseMapper::toDetailResponse)
                .toList());
        response.setShippingInformation(toShippingResponse(order.getShippingInformation()));
        response.setPayment(toPaymentResponse(order.getPayment()));
        response.setDeliveryOrder(toDeliveryResponse(order.getDeliveryOrder()));
        response.setCustomer(toCustomerResponse(order));
        return response;
    }

    public static CheckoutResponse toCheckoutResponse(Order order) {
        CheckoutResponse response = new CheckoutResponse();
        response.setOrderId(order.getId());
        response.setOrderDate(order.getOrderDate());
        response.setOrderStatus(order.getStatus());
        response.setTotalAmount(order.getTotalAmount());
        response.setNotes(order.getNotes());
        response.setItems(order.getOrderDetails().stream()
                .map(OrderResponseMapper::toDetailResponse)
                .toList());
        response.setShippingInformation(toShippingResponse(order.getShippingInformation()));
        response.setPayment(toPaymentResponse(order.getPayment()));
        response.setDeliveryOrder(toDeliveryResponse(order.getDeliveryOrder()));
        return response;
    }

    public static OrderDetailResponse toDetailResponse(OrderDetail orderDetail) {
        OrderDetailResponse response = new OrderDetailResponse();
        response.setDetailId(orderDetail.getId());
        if (orderDetail.getProduct() != null) {
            response.setProductId(orderDetail.getProduct().getId());
            response.setProductName(orderDetail.getProduct().getName());
        }
        response.setQuantity(orderDetail.getQuantity().intValueExact());
        response.setUnitPrice(orderDetail.getUnitPrice());
        response.setSubtotal(orderDetail.getUnitPrice().multiply(orderDetail.getQuantity()));
        return response;
    }

    private static ShippingInformationResponse toShippingResponse(OrderShippingInformation shippingInformation) {
        if (shippingInformation == null) {
            return null;
        }

        ShippingInformationResponse response = new ShippingInformationResponse();
        response.setAddress(shippingInformation.getAddress());
        response.setCountry(shippingInformation.getCountry());
        response.setCity(shippingInformation.getCity());
        response.setNeighborhood(shippingInformation.getNeighborhood());
        response.setReferenceDetails(shippingInformation.getReferenceDetails());
        return response;
    }

    private static PaymentSummaryResponse toPaymentResponse(OrderPayment payment) {
        if (payment == null) {
            return null;
        }

        PaymentSummaryResponse response = new PaymentSummaryResponse();
        response.setPaymentMethod(payment.getPaymentMethod());
        response.setMaskedCardNumber(payment.getMaskedCardNumber());
        response.setStatus(payment.getStatus());
        response.setTransactionReference(payment.getTransactionReference());
        response.setPaidAt(payment.getPaidAt());
        return response;
    }

    private static DeliveryOrderResponse toDeliveryResponse(DeliveryOrder deliveryOrder) {
        if (deliveryOrder == null) {
            return null;
        }

        DeliveryOrderResponse response = new DeliveryOrderResponse();
        response.setId(deliveryOrder.getId());
        response.setStatus(deliveryOrder.getStatus());
        response.setCreatedAt(deliveryOrder.getCreatedAt());
        response.setUpdatedAt(deliveryOrder.getUpdatedAt());
        return response;
    }

    private static OrderCustomerResponse toCustomerResponse(Order order) {
        OrderCustomerResponse response = new OrderCustomerResponse();
        response.setId(order.getUsuario().getId());
        response.setUsername(order.getUsuario().getUsername());
        response.setEmail(order.getUsuario().getEmail());
        return response;
    }
}
