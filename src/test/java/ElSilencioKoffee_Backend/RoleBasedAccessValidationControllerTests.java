package ElSilencioKoffee_Backend;

import ElSilencioKoffee_Backend.entities.Order;
import ElSilencioKoffee_Backend.entities.OrderStatus;
import ElSilencioKoffee_Backend.entities.Rol;
import ElSilencioKoffee_Backend.entities.Usuario;
import ElSilencioKoffee_Backend.services.IOrderService;
import ElSilencioKoffee_Backend.services.IRolService;
import ElSilencioKoffee_Backend.services.IUsuarioService;
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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class RoleBasedAccessValidationControllerTests {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockitoBean
    private IOrderService orderService;

    @MockitoBean
    private IUsuarioService usuarioService;

    @MockitoBean
    private IRolService rolService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    void protectedEndpointsRejectUnauthenticatedRequests() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/users/me/orders"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/orders/1"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(usuarioService, orderService);
    }

    @Test
    @WithMockUser(roles = "USER")
    void adminOnlyEndpointsRejectRegularUsers() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/roles"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(usuarioService, rolService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminOnlyEndpointsAllowAdmins() throws Exception {
        Rol rol = new Rol();
        rol.setId(1L);
        rol.setNombre("ROLE_ADMIN");
        when(rolService.findAll()).thenReturn(List.of(rol));

        mockMvc.perform(get("/roles").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("ROLE_ADMIN"));

        verify(rolService).findAll();
    }

    @Test
    @WithMockUser(username = "customer-1", roles = "USER")
    void authenticatedUserCanAccessOwnOrdersRoute() throws Exception {
        when(orderService.findOrdersByUsername("customer-1")).thenReturn(List.of(createOrder(10L, 7L, OrderStatus.NON_PAID)));

        mockMvc.perform(get("/users/me/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].userId").value(7));

        verify(orderService).findOrdersByUsername("customer-1");
    }

    @Test
    @WithMockUser(username = "admin-owner", roles = "ADMIN")
    void authenticatedAdminsCanAccessOwnOrdersRoute() throws Exception {
        when(orderService.findOrdersByUsername("admin-owner")).thenReturn(List.of(createOrder(11L, 9L, OrderStatus.PAID)));

        mockMvc.perform(get("/users/me/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(11))
                .andExpect(jsonPath("$[0].status").value("PAID"));

        verify(orderService).findOrdersByUsername("admin-owner");
    }

    @Test
    @WithMockUser(username = "customer-1", roles = "USER")
    void orderDetailUsesOwnershipScopedLookupForRegularUsers() throws Exception {
        when(orderService.findOrderByIdForUsername(eq(12L), eq("customer-1")))
                .thenReturn(createOrder(12L, 5L, OrderStatus.NON_PAID));

        mockMvc.perform(get("/orders/12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(12))
                .andExpect(jsonPath("$.userId").value(5));

        verify(orderService).findOrderByIdForUsername(12L, "customer-1");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void orderDetailAllowsAdminLookupsAcrossUsers() throws Exception {
        when(orderService.findOrderById(13L)).thenReturn(createOrder(13L, 21L, OrderStatus.NON_PAID));

        mockMvc.perform(get("/orders/13"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(13))
                .andExpect(jsonPath("$.userId").value(21));

        verify(orderService).findOrderById(13L);
    }

    private Order createOrder(Long orderId, Long userId, OrderStatus status) {
        Usuario usuario = new Usuario();
        usuario.setId(userId);

        Order order = new Order();
        order.setId(orderId);
        order.setUsuario(usuario);
        order.setStatus(status);
        order.setTotalAmount(new BigDecimal("18.00"));
        return order;
    }
}
