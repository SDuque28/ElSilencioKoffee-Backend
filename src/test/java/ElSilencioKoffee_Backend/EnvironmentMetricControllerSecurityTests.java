package ElSilencioKoffee_Backend;

import ElSilencioKoffee_Backend.environment.dto.EnvironmentMetricResponse;
import ElSilencioKoffee_Backend.environment.services.IEnvironmentMetricService;
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
import java.time.LocalDateTime;
import java.util.List;

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
class EnvironmentMetricControllerSecurityTests {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockitoBean
    private IEnvironmentMetricService environmentMetricService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    void rejectUnauthenticatedAccess() throws Exception {
        mockMvc.perform(get("/environment-metrics"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(
                        post("/environment-metrics")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(validMetricJson())
                )
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(environmentMetricService);
    }

    @Test
    @WithMockUser(roles = "USER")
    void authenticatedUsersCanReadButCannotWrite() throws Exception {
        when(environmentMetricService.findAll(null, null, null, null))
                .thenReturn(List.of(createResponse(1L, "temperature", "21.50", "C")));

        mockMvc.perform(get("/environment-metrics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].metricType").value("temperature"))
                .andExpect(jsonPath("$[0].unit").value("C"));

        mockMvc.perform(
                        post("/environment-metrics")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(validMetricJson())
                )
                .andExpect(status().isForbidden());

        verify(environmentMetricService).findAll(null, null, null, null);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminsCanCreateMetrics() throws Exception {
        when(environmentMetricService.create(any()))
                .thenReturn(createResponse(7L, "humidity", "62.00", "%"));

        mockMvc.perform(
                        post("/environment-metrics")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(validMetricJson())
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.metricType").value("humidity"))
                .andExpect(jsonPath("$.value").value(62.00))
                .andExpect(jsonPath("$.unit").value("%"));

        verify(environmentMetricService).create(any());
    }

    @Test
    @WithMockUser(roles = "USER")
    void readEndpointsAcceptFiltersForAuthenticatedUsers() throws Exception {
        when(environmentMetricService.findAll(
                eq("temperature"),
                eq(2L),
                eq(LocalDateTime.parse("2026-04-20T08:00:00")),
                eq(LocalDateTime.parse("2026-04-20T10:00:00"))
        )).thenReturn(List.of(createResponse(2L, "temperature", "20.10", "C")));

        mockMvc.perform(get("/environment-metrics")
                        .param("type", "temperature")
                        .param("sectionId", "2")
                        .param("fromDate", "2026-04-20T08:00:00")
                        .param("toDate", "2026-04-20T10:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].metricType").value("temperature"));

        verify(environmentMetricService).findAll(
                "temperature",
                2L,
                LocalDateTime.parse("2026-04-20T08:00:00"),
                LocalDateTime.parse("2026-04-20T10:00:00")
        );
    }

    private EnvironmentMetricResponse createResponse(Long id, String metricType, String value, String unit) {
        EnvironmentMetricResponse response = new EnvironmentMetricResponse();
        response.setId(id);
        response.setMetricType(metricType);
        response.setValue(new BigDecimal(value));
        response.setUnit(unit);
        response.setMeasuredAt(LocalDateTime.of(2026, 4, 20, 8, 0));
        return response;
    }

    private String validMetricJson() {
        return """
                {
                  "metricType": "humidity",
                  "value": 62.00,
                  "unit": "%",
                  "measuredAt": "2026-04-20T08:00:00"
                }
                """;
    }
}
