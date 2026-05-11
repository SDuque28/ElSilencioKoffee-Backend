package ElSilencioKoffee_Backend.checkout.services;

import ElSilencioKoffee_Backend.checkout.dto.CheckoutRequest;
import ElSilencioKoffee_Backend.orders.entities.Order;

public interface ICheckoutService {

    Order checkout(String username, CheckoutRequest request);
}
