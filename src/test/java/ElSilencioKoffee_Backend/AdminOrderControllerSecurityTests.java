package ElSilencioKoffee_Backend;

import ElSilencioKoffee_Backend.orders.entities.DeliveryOrder;
import ElSilencioKoffee_Backend.orders.entities.DeliveryStatus;
import ElSilencioKoffee_Backend.orders.entities.Order;
import ElSilencioKoffee_Backend.orders.entities.OrderPayment;
import ElSilencioKoffee_Backend.orders.entities.OrderShippingInformation;
import ElSilencioKoffee_Backend.orders.entities.OrderStatus;
import ElSilencioKoffee_Backend.orders.entities.PaymentMethod;
import ElSilencioKoffee_Backend.orders.entities.PaymentStatus;
import ElSilencioKoffee_Backend.orders.services.IOrderService;
import ElSilencioKoffee_Backend.users.entities.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class AdminOrderControllerSecurityTests {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    private IOrderService orderService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    @WithMockUser(roles = "USER")
    void regularUsersCannotAccessAdminOrderEndpoints() throws Exception {
        mockMvc.perform(get("/api/v1/admin/orders"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/admin/orders/12"))
                .andExpect(status().isForbidden());

        mockMvc.perform(
                        patch("/api/v1/admin/orders/12/delivery-status")
                                .contentType(APPLICATION_JSON)
                                .content("""
                                        {"status":"OUT_FOR_SHIPMENT"}
                                        """)
                )
                .andExpect(status().isForbidden());

        verifyNoInteractions(orderService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminsCanViewFullPurchaseDetails() throws Exception {
        when(orderService.findAllOrders()).thenReturn(List.of(adminOrder()));
        when(orderService.findOrderById(12L)).thenReturn(adminOrder());

        mockMvc.perform(get("/api/v1/admin/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(12))
                .andExpect(jsonPath("$[0].customer.username").value("customer-1"))
                .andExpect(jsonPath("$[0].shippingInformation.city").value("Bogota"))
                .andExpect(jsonPath("$[0].payment.maskedCardNumber").value("**** **** **** 4242"))
                .andExpect(jsonPath("$[0].deliveryOrder.status").value("OUT_FOR_SHIPMENT"));

        mockMvc.perform(get("/api/v1/admin/orders/12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(12))
                .andExpect(jsonPath("$.customer.email").value("customer-1@example.com"))
                .andExpect(jsonPath("$.payment.transactionReference").value("SIM-ORDER000012"))
                .andExpect(jsonPath("$.deliveryOrder.status").value("OUT_FOR_SHIPMENT"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminsCanUpdateDeliveryStatus() throws Exception {
        Order order = adminOrder();
        order.getDeliveryOrder().setStatus(DeliveryStatus.DELIVERED);
        when(orderService.updateDeliveryStatus(eq(12L), eq(DeliveryStatus.DELIVERED))).thenReturn(order);

        mockMvc.perform(
                        patch("/api/v1/admin/orders/12/delivery-status")
                                .contentType(APPLICATION_JSON)
                                .content("""
                                        {"status":"DELIVERED"}
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(12))
                .andExpect(jsonPath("$.deliveryOrder.status").value("DELIVERED"));

        verify(orderService).updateDeliveryStatus(12L, DeliveryStatus.DELIVERED);
    }

    private Order adminOrder() {
        Usuario usuario = new Usuario();
        usuario.setId(6L);
        usuario.setUsername("customer-1");
        usuario.setEmail("customer-1@example.com");

        Order order = new Order();
        order.setId(12L);
        order.setUsuario(usuario);
        order.setStatus(OrderStatus.PAID);
        order.setTotalAmount(new BigDecimal("52.00"));
        order.setNotes("Handle carefully.");

        OrderShippingInformation shipping = new OrderShippingInformation();
        shipping.setAddress("Street 123 # 45-67");
        shipping.setCountry("Colombia");
        shipping.setCity("Bogota");
        shipping.setNeighborhood("Usaquen");
        shipping.setReferenceDetails("Blue door");
        order.setShippingInformation(shipping);

        OrderPayment payment = new OrderPayment();
        payment.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        payment.setMaskedCardNumber("**** **** **** 4242");
        payment.setStatus(PaymentStatus.APPROVED);
        payment.setTransactionReference("SIM-ORDER000012");
        payment.setPaidAt(LocalDateTime.of(2026, 5, 4, 11, 0));
        order.setPayment(payment);

        DeliveryOrder deliveryOrder = new DeliveryOrder();
        deliveryOrder.setId(88L);
        deliveryOrder.setStatus(DeliveryStatus.OUT_FOR_SHIPMENT);
        deliveryOrder.setCreatedAt(LocalDateTime.of(2026, 5, 4, 11, 5));
        deliveryOrder.setUpdatedAt(LocalDateTime.of(2026, 5, 4, 11, 5));
        order.setDeliveryOrder(deliveryOrder);

        return order;
    }
}
