package ElSilencioKoffee_Backend;

import ElSilencioKoffee_Backend.cart.dto.CartResponse;
import ElSilencioKoffee_Backend.cart.services.ICartService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class CartControllerSecurityTests {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockitoBean
    private ICartService cartService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    void rejectUnauthenticatedAccess() throws Exception {
        mockMvc.perform(get("/cart"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(
                        post("/cart/items")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"productId":1,"quantity":2}
                                        """)
                )
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(cartService);
    }

    @Test
    @WithMockUser(username = "cart-user", roles = "USER")
    void authenticatedUserCanGetOwnCart() throws Exception {
        when(cartService.getCartForUsername("cart-user")).thenReturn(response());

        mockMvc.perform(get("/cart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(7))
                .andExpect(jsonPath("$.totalItems").value(2))
                .andExpect(jsonPath("$.totalAmount").value(24.00));

        verify(cartService).getCartForUsername("cart-user");
    }

    @Test
    @WithMockUser(username = "cart-user", roles = "USER")
    void authenticatedUserCanAddToOwnCart() throws Exception {
        when(cartService.addItem(eq("cart-user"), any())).thenReturn(response());

        mockMvc.perform(
                        post("/cart/items")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"productId":1,"quantity":2}
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(7))
                .andExpect(jsonPath("$.totalItems").value(2));

        verify(cartService).addItem(eq("cart-user"), any());
    }

    private CartResponse response() {
        CartResponse response = new CartResponse();
        response.setId(3L);
        response.setUserId(7L);
        response.setTotalItems(2);
        response.setTotalAmount(new BigDecimal("24.00"));
        return response;
    }
}
