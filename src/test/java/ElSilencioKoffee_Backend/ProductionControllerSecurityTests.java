package ElSilencioKoffee_Backend;

import ElSilencioKoffee_Backend.production.dto.ProductionResponse;
import ElSilencioKoffee_Backend.production.services.IProductionService;
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
import java.time.LocalDate;
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
class ProductionControllerSecurityTests {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    private IProductionService productionService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    void rejectUnauthenticatedAccess() throws Exception {
        mockMvc.perform(get("/production"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(
                        post("/production")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(validProductionJson())
                )
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(productionService);
    }

    @Test
    @WithMockUser(roles = "USER")
    void authenticatedUsersCanReadButCannotWrite() throws Exception {
        when(productionService.findAll()).thenReturn(List.of(createResponse(1L, 1L, 1L, "42.50")));

        mockMvc.perform(get("/production"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].sectionId").value(1))
                .andExpect(jsonPath("$[0].varietyId").value(1));

        mockMvc.perform(
                        post("/production")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(validProductionJson())
                )
                .andExpect(status().isForbidden());

        verify(productionService).findAll();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminsCanCreateProductionRecords() throws Exception {
        when(productionService.create(any())).thenReturn(createResponse(7L, 2L, 3L, "55.00"));

        mockMvc.perform(
                        post("/production")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(validProductionJson())
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.sectionId").value(2))
                .andExpect(jsonPath("$.varietyId").value(3))
                .andExpect(jsonPath("$.quantityKg").value(55.00));

        verify(productionService).create(any());
    }

    private ProductionResponse createResponse(Long id, Long sectionId, Long varietyId, String quantityKg) {
        ProductionResponse response = new ProductionResponse();
        response.setId(id);
        response.setSectionId(sectionId);
        response.setSectionName("Section " + sectionId);
        response.setSectionLocation("Location " + sectionId);
        response.setVarietyId(varietyId);
        response.setVarietyName("Variety " + varietyId);
        response.setQuantityKg(new BigDecimal(quantityKg));
        response.setCollectionDate(LocalDate.of(2026, 4, 20));
        return response;
    }

    private String validProductionJson() {
        return """
                {
                  "sectionId": 2,
                  "varietyId": 3,
                  "quantityKg": 55.00,
                  "collectionDate": "2026-04-20"
                }
                """;
    }
}
