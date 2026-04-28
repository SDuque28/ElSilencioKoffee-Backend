package ElSilencioKoffee_Backend;

import ElSilencioKoffee_Backend.orders.services.IOrderService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class JwtAuthenticationFilterTests {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockitoBean
    private IOrderService orderService;

    @Value("${jwt.secret}")
    private String jwtSecret;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    void protectedEndpointRejectsMissingToken() throws Exception {
        mockMvc.perform(get("/users/me/orders"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(orderService);
    }

    @Test
    void protectedEndpointRejectsMalformedBearerToken() throws Exception {
        mockMvc.perform(
                        get("/users/me/orders")
                                .header("Authorization", "Bearer invalid token")
                )
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(orderService);
    }

    @Test
    void protectedEndpointRejectsExpiredToken() throws Exception {
        String expiredToken = Jwts.builder()
                .subject("expired-user")
                .claim("roles", java.util.List.of("ROLE_USER"))
                .issuedAt(Date.from(Instant.now().minusSeconds(120)))
                .expiration(Date.from(Instant.now().minusSeconds(60)))
                .signWith(signingKey(jwtSecret))
                .compact();

        mockMvc.perform(
                        get("/users/me/orders")
                                .header("Authorization", "Bearer " + expiredToken)
                )
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(orderService);
    }

    @Test
    void protectedEndpointRejectsTokenSignedWithWrongSecret() throws Exception {
        String invalidSignatureToken = Jwts.builder()
                .subject("wrong-secret-user")
                .claim("roles", java.util.List.of("ROLE_USER"))
                .issuedAt(new Date())
                .expiration(Date.from(Instant.now().plusSeconds(300)))
                .signWith(signingKey("abcdef0123456789abcdef0123456789"))
                .compact();

        mockMvc.perform(
                        get("/users/me/orders")
                                .header("Authorization", "Bearer " + invalidSignatureToken)
                )
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(orderService);
    }

    private SecretKey signingKey(String secret) {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
