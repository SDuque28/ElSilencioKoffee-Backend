package ElSilencioKoffee_Backend;

import ElSilencioKoffee_Backend.orders.entities.Order;
import ElSilencioKoffee_Backend.orders.entities.OrderDetail;
import ElSilencioKoffee_Backend.orders.entities.OrderStatus;
import ElSilencioKoffee_Backend.orders.services.IOrderService;
import ElSilencioKoffee_Backend.products.entities.Product;
import ElSilencioKoffee_Backend.users.entities.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class OrderControllerDetailsTests {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockitoBean
    private IOrderService orderService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    @WithMockUser(username = "customer-1", roles = "USER")
    void createOrderReturnsDetailedLineItemsWithoutEntityInternals() throws Exception {
        when(orderService.createOrder(eq("customer-1"), anyList())).thenReturn(createOrder(20L, 8L));

        mockMvc.perform(
                        post("/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(validCreateOrderJson())
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(20))
                .andExpect(jsonPath("$.items[0].detailId").value(101))
                .andExpect(jsonPath("$.items[0].productId").value(1))
                .andExpect(jsonPath("$.items[0].productName").value("Geisha"))
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.items[0].subtotal").value(52.00))
                .andExpect(jsonPath("$.usuario").doesNotExist())
                .andExpect(jsonPath("$.orderDetails").doesNotExist());

        verify(orderService).createOrder(eq("customer-1"), anyList());
    }

    @Test
    @WithMockUser(username = "customer-1", roles = "USER")
    void getOrderByIdReturnsLineItemsWithoutLeakingEntities() throws Exception {
        when(orderService.findOrderByIdForUsername(20L, "customer-1")).thenReturn(createOrder(20L, 8L));

        mockMvc.perform(get("/orders/20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(20))
                .andExpect(jsonPath("$.totalAmount").value(70.00))
                .andExpect(jsonPath("$.items[1].detailId").value(102))
                .andExpect(jsonPath("$.items[1].productId").value(2))
                .andExpect(jsonPath("$.items[1].productName").value("Capsules"))
                .andExpect(jsonPath("$.items[1].subtotal").value(18.00))
                .andExpect(jsonPath("$.usuario").doesNotExist())
                .andExpect(jsonPath("$.orderDetails").doesNotExist());

        verify(orderService).findOrderByIdForUsername(20L, "customer-1");
    }

    private String validCreateOrderJson() {
        return """
                {
                  "items": [
                    { "productId": 1, "quantity": 2 },
                    { "productId": 2, "quantity": 1 }
                  ]
                }
                """;
    }

    private Order createOrder(Long orderId, Long userId) {
        Usuario usuario = new Usuario();
        usuario.setId(userId);

        Product productOne = new Product();
        productOne.setId(1L);
        productOne.setName("Geisha");
        productOne.setPrice(new BigDecimal("26.00"));

        Product productTwo = new Product();
        productTwo.setId(2L);
        productTwo.setName("Capsules");
        productTwo.setPrice(new BigDecimal("18.00"));

        OrderDetail firstDetail = new OrderDetail();
        firstDetail.setId(101L);
        firstDetail.setProduct(productOne);
        firstDetail.setQuantity(new BigDecimal("2"));
        firstDetail.setUnitPrice(new BigDecimal("26.00"));

        OrderDetail secondDetail = new OrderDetail();
        secondDetail.setId(102L);
        secondDetail.setProduct(productTwo);
        secondDetail.setQuantity(new BigDecimal("1"));
        secondDetail.setUnitPrice(new BigDecimal("18.00"));

        Order order = new Order();
        order.setId(orderId);
        order.setUsuario(usuario);
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(new BigDecimal("70.00"));
        order.addOrderDetail(firstDetail);
        order.addOrderDetail(secondDetail);
        return order;
    }
}
