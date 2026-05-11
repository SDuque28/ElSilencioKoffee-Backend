package ElSilencioKoffee_Backend;

import ElSilencioKoffee_Backend.auth.dto.AuthResponse;
import ElSilencioKoffee_Backend.auth.services.IAuthService;
import ElSilencioKoffee_Backend.shared.dto.MessageResponse;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class AuthControllerSecurityTests {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    private IAuthService authService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    void publicAuthEndpointsAreAccessibleWithoutToken() throws Exception {
        AuthResponse authResponse = new AuthResponse();
        authResponse.setToken("jwt-token");
        authResponse.setUsername("public-user");
        authResponse.setEmail("public@example.com");
        authResponse.setRoles(List.of("ROLE_USER"));

        when(authService.register(any())).thenReturn(authResponse);
        when(authService.login(any())).thenReturn(authResponse);
        when(authService.passwordRecovery(any())).thenReturn(new MessageResponse("Password updated successfully"));

        mockMvc.perform(
                        post("/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"username":"public-user","email":"public@example.com","password":"secret"}
                                        """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("public-user"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        mockMvc.perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"username":"public-user","password":"secret"}
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        mockMvc.perform(
                        post("/auth/password-recovery")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "username":"public-user",
                                          "email":"public@example.com",
                                          "newPassword":"new-secret",
                                          "confirmPassword":"new-secret"
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password updated successfully"));
    }

    @Test
    void changePasswordRejectsUnauthenticatedRequests() throws Exception {
        mockMvc.perform(
                        post("/auth/change-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "currentPassword":"secret",
                                          "newPassword":"new-secret",
                                          "confirmPassword":"new-secret"
                                        }
                                        """)
                )
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(authService);
    }

    @Test
    @WithMockUser(username = "public-user", roles = "USER")
    void changePasswordAllowsAuthenticatedUsers() throws Exception {
        when(authService.changePassword(any(), any())).thenReturn(new MessageResponse("Password changed successfully"));

        mockMvc.perform(
                        post("/auth/change-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "currentPassword":"secret",
                                          "newPassword":"new-secret",
                                          "confirmPassword":"new-secret"
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password changed successfully"));
    }
}
