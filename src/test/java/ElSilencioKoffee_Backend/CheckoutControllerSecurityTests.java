package ElSilencioKoffee_Backend;

import ElSilencioKoffee_Backend.checkout.services.ICheckoutService;
import ElSilencioKoffee_Backend.orders.entities.DeliveryOrder;
import ElSilencioKoffee_Backend.orders.entities.DeliveryStatus;
import ElSilencioKoffee_Backend.orders.entities.Order;
import ElSilencioKoffee_Backend.orders.entities.OrderPayment;
import ElSilencioKoffee_Backend.orders.entities.OrderShippingInformation;
import ElSilencioKoffee_Backend.orders.entities.OrderStatus;
import ElSilencioKoffee_Backend.orders.entities.PaymentMethod;
import ElSilencioKoffee_Backend.orders.entities.PaymentStatus;
import ElSilencioKoffee_Backend.users.entities.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class CheckoutControllerSecurityTests {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    private ICheckoutService checkoutService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    void rejectUnauthenticatedCheckout() throws Exception {
        mockMvc.perform(
                        post("/api/v1/checkout")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(validCheckoutJson())
                )
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(checkoutService);
    }

    @Test
    @WithMockUser(username = "buyer-1", roles = "USER")
    void authenticatedCheckoutReturnsSafePaymentSummary() throws Exception {
        when(checkoutService.checkout(eq("buyer-1"), any())).thenReturn(checkoutOrder());

        mockMvc.perform(
                        post("/api/v1/checkout")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(validCheckoutJson())
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value(41))
                .andExpect(jsonPath("$.orderStatus").value("PAID"))
                .andExpect(jsonPath("$.payment.maskedCardNumber").value("**** **** **** 4242"))
                .andExpect(jsonPath("$.payment.transactionReference").value("SIM-ABC123456789"))
                .andExpect(jsonPath("$.deliveryOrder.status").value("OUT_FOR_SHIPMENT"))
                .andExpect(jsonPath("$.payment.cvv").doesNotExist())
                .andExpect(jsonPath("$.payment.cardNumber").doesNotExist())
                .andExpect(jsonPath("$.payment.cardholderName").doesNotExist());

        verify(checkoutService).checkout(eq("buyer-1"), any());
    }

    private String validCheckoutJson() {
        return """
                {
                  "shippingInformation": {
                    "address": "Street 123 # 45-67",
                    "country": "Colombia",
                    "city": "Bogota",
                    "neighborhood": "Chapinero",
                    "referenceDetails": "Blue door"
                  },
                  "payment": {
                    "paymentMethod": "CREDIT_CARD",
                    "cardholderName": "Test Buyer",
                    "cardNumber": "4242424242424242",
                    "expirationDate": "12/2099",
                    "cvv": "123"
                  },
                  "notes": "Leave the package with the receptionist."
                }
                """;
    }

    private Order checkoutOrder() {
        Usuario usuario = new Usuario();
        usuario.setId(8L);
        usuario.setUsername("buyer-1");
        usuario.setEmail("buyer-1@example.com");

        Order order = new Order();
        order.setId(41L);
        order.setUsuario(usuario);
        order.setStatus(OrderStatus.PAID);
        order.setTotalAmount(new BigDecimal("26.00"));
        order.setNotes("Leave the package with the receptionist.");

        OrderShippingInformation shipping = new OrderShippingInformation();
        shipping.setAddress("Street 123 # 45-67");
        shipping.setCountry("Colombia");
        shipping.setCity("Bogota");
        shipping.setNeighborhood("Chapinero");
        shipping.setReferenceDetails("Blue door");
        order.setShippingInformation(shipping);

        OrderPayment payment = new OrderPayment();
        payment.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        payment.setMaskedCardNumber("**** **** **** 4242");
        payment.setStatus(PaymentStatus.APPROVED);
        payment.setTransactionReference("SIM-ABC123456789");
        payment.setPaidAt(LocalDateTime.of(2026, 5, 4, 10, 15));
        order.setPayment(payment);

        DeliveryOrder deliveryOrder = new DeliveryOrder();
        deliveryOrder.setStatus(DeliveryStatus.OUT_FOR_SHIPMENT);
        deliveryOrder.setCreatedAt(LocalDateTime.of(2026, 5, 4, 10, 15));
        deliveryOrder.setUpdatedAt(LocalDateTime.of(2026, 5, 4, 10, 15));
        order.setDeliveryOrder(deliveryOrder);

        return order;
    }
}
