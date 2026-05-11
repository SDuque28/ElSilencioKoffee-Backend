package ElSilencioKoffee_Backend;

import ElSilencioKoffee_Backend.products.dto.ProductResponse;
import ElSilencioKoffee_Backend.products.services.IProductService;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class ProductControllerSecurityTests {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    private IProductService productService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    void listProductsIsPublic() throws Exception {
        when(productService.findAll()).thenReturn(List.of(createResponse(1L, "Geisha", "34.00")));

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Geisha"))
                .andExpect(jsonPath("$[0].price").value(34.00))
                .andExpect(jsonPath("$[0].stockQuantity").value(0));

        verify(productService).findAll();
    }

    @Test
    void createProductRejectsUnauthenticatedRequests() throws Exception {
        mockMvc.perform(
                        post("/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(validProductJson())
                )
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(productService);
    }

    @Test
    @WithMockUser(roles = "USER")
    void createProductRejectsRegularUsers() throws Exception {
        mockMvc.perform(
                        post("/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(validProductJson())
                )
                .andExpect(status().isForbidden());

        verifyNoInteractions(productService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createProductAllowsAdmins() throws Exception {
        when(productService.create(any())).thenReturn(createResponse(7L, "Bourbon", "22.50"));

        mockMvc.perform(
                        post("/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(validProductJson())
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.name").value("Bourbon"))
                .andExpect(jsonPath("$.presentationId").value(1))
                .andExpect(jsonPath("$.productionId").value(1));

        verify(productService).create(any());
    }

    private ProductResponse createResponse(Long id, String name, String price) {
        ProductResponse response = new ProductResponse();
        response.setId(id);
        response.setName(name);
        response.setImageUrl("https://cdn.example.com/" + name.toLowerCase() + ".png");
        response.setPrice(new BigDecimal(price));
        response.setPresentationId(1L);
        response.setProductionId(1L);
        response.setStockQuantity(0);
        return response;
    }

    private String validProductJson() {
        return """
                {
                  "name": "Bourbon",
                  "imageUrl": "https://cdn.example.com/bourbon.png",
                  "price": 22.50,
                  "presentationId": 1,
                  "productionId": 1
                }
                """;
    }
}
