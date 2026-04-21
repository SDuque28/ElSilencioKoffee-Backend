package ElSilencioKoffee_Backend;

import ElSilencioKoffee_Backend.entities.Order;
import ElSilencioKoffee_Backend.entities.OrderStatus;
import ElSilencioKoffee_Backend.entities.Usuario;
import ElSilencioKoffee_Backend.services.IOrderService;
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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class OrderStatusControllerSecurityTests {

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
    @WithMockUser(roles = "USER")
    void updateStatusRejectsRegularUsers() throws Exception {
        mockMvc.perform(
                        patch("/orders/{id}/status", 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"status":"PAID"}
                                        """)
                )
                .andExpect(status().isForbidden());

        verifyNoInteractions(orderService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateStatusAllowsAdminsAndDelegatesToService() throws Exception {
        Order order = new Order();
        order.setId(1L);
        order.setStatus(OrderStatus.PAID);
        order.setTotalAmount(new BigDecimal("18.00"));

        Usuario usuario = new Usuario();
        usuario.setId(7L);
        order.setUsuario(usuario);

        when(orderService.updateStatus(eq(1L), eq(OrderStatus.PAID))).thenReturn(order);

        mockMvc.perform(
                        patch("/orders/{id}/status", 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"status":"PAID"}
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("PAID"))
                .andExpect(jsonPath("$.userId").value(7));
    }
}
