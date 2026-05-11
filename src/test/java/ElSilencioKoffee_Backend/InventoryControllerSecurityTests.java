package ElSilencioKoffee_Backend;

import ElSilencioKoffee_Backend.inventory.dto.InventoryResponse;
import ElSilencioKoffee_Backend.inventory.services.IInventoryService;
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
class InventoryControllerSecurityTests {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    private IInventoryService inventoryService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    void rejectUnauthenticatedAccess() throws Exception {
        mockMvc.perform(get("/inventory"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(
                        post("/inventory/1/increase")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(validAdjustmentJson())
                )
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(inventoryService);
    }

    @Test
    @WithMockUser(roles = "USER")
    void rejectRegularUsers() throws Exception {
        mockMvc.perform(get("/inventory"))
                .andExpect(status().isForbidden());

        mockMvc.perform(
                        post("/inventory/1/increase")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(validAdjustmentJson())
                )
                .andExpect(status().isForbidden());

        verifyNoInteractions(inventoryService);
    }

    @Test
    @WithMockUser(username = "admin-user", roles = "ADMIN")
    void allowAdminsToReadAndModifyInventory() throws Exception {
        when(inventoryService.findAll()).thenReturn(java.util.List.of(createResponse(1L, 4L, "Geisha", 12)));
        when(inventoryService.increaseStock(eq(1L), any(), eq("admin-user")))
                .thenReturn(createResponse(1L, 4L, "Geisha", 18));

        mockMvc.perform(get("/inventory"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].productId").value(4))
                .andExpect(jsonPath("$[0].stockQuantity").value(12));

        mockMvc.perform(
                        post("/inventory/1/increase")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(validAdjustmentJson())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.stockQuantity").value(18));

        verify(inventoryService).findAll();
        verify(inventoryService).increaseStock(eq(1L), any(), eq("admin-user"));
    }

    private InventoryResponse createResponse(Long id, Long productId, String productName, Integer stockQuantity) {
        InventoryResponse response = new InventoryResponse();
        response.setId(id);
        response.setProductId(productId);
        response.setProductName(productName);
        response.setProductImageUrl("https://cdn.example.com/" + productName.toLowerCase() + ".png");
        response.setStockQuantity(stockQuantity);
        return response;
    }

    private String validAdjustmentJson() {
        return """
                {
                  "quantity": 6,
                  "referenceType": "MANUAL"
                }
                """;
    }
}
