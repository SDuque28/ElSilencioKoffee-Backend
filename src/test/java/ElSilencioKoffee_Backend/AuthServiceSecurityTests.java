package ElSilencioKoffee_Backend;

import ElSilencioKoffee_Backend.dto.AuthResponse;
import ElSilencioKoffee_Backend.dto.LoginRequest;
import ElSilencioKoffee_Backend.dto.RegisterRequest;
import ElSilencioKoffee_Backend.entities.Rol;
import ElSilencioKoffee_Backend.entities.Usuario;
import ElSilencioKoffee_Backend.entities.UsuarioRol;
import ElSilencioKoffee_Backend.entities.UsuarioRolId;
import ElSilencioKoffee_Backend.repository.RolRepository;
import ElSilencioKoffee_Backend.repository.UsuarioRepository;
import ElSilencioKoffee_Backend.repository.UsuarioRolRepository;
import ElSilencioKoffee_Backend.services.IAuthService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class AuthServiceSecurityTests {

    @Autowired
    private IAuthService authService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private UsuarioRolRepository usuarioRolRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Test
    void registrationStoresEncodedPasswordAndTokenContainsExpiration() {
        ensureRole("ROLE_USER");

        RegisterRequest request = new RegisterRequest();
        request.setUsername("registered-user");
        request.setEmail("registered@example.com");
        request.setPassword("plain-secret");

        AuthResponse response = authService.register(request);
        Usuario usuario = usuarioRepository.findByUsername("registered-user").orElseThrow();
        Claims claims = parseClaims(response.getToken());

        assertNotEquals("plain-secret", usuario.getPassword());
        assertTrue(passwordEncoder.matches("plain-secret", usuario.getPassword()));
        assertEquals("registered-user", response.getUsername());
        assertEquals("registered@example.com", response.getEmail());
        assertEquals("registered-user", claims.getSubject());
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
        assertTrue(claims.getExpiration().after(new Date()));
        assertEquals(List.of("ROLE_USER"), claims.get("roles", List.class));
        assertNull(claims.get("password"));
    }

    @Test
    void registrationRejectsBlankPassword() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("blank-password-user");
        request.setEmail("blank@example.com");
        request.setPassword("   ");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.register(request)
        );

        assertEquals("Password is required", exception.getMessage());
    }

    @Test
    void loginAuthenticatesAgainstEncodedPassword() {
        Usuario usuario = new Usuario();
        usuario.setUsername("login-user");
        usuario.setEmail("login@example.com");
        usuario.setPassword(passwordEncoder.encode("plain-secret"));
        usuario.setActivo(true);
        usuario = usuarioRepository.save(usuario);
        assignRole(usuario, "ROLE_USER");

        LoginRequest request = new LoginRequest();
        request.setUsername("login-user");
        request.setPassword("plain-secret");

        AuthResponse response = authService.login(request);
        Claims claims = parseClaims(response.getToken());

        assertEquals("login-user", response.getUsername());
        assertTrue(response.getRoles().contains("ROLE_USER"));
        assertEquals("login-user", claims.getSubject());
        assertEquals(List.of("ROLE_USER"), claims.get("roles", List.class));
    }

    private void assignRole(Usuario usuario, String roleName) {
        Rol rol = ensureRole(roleName);
        UsuarioRol usuarioRol = new UsuarioRol(new UsuarioRolId(usuario.getId(), rol.getId()), usuario, rol);
        usuarioRolRepository.save(usuarioRol);
        usuario.getUsuariosRoles().add(usuarioRol);
    }

    private Rol ensureRole(String roleName) {
        return rolRepository.findByNombre(roleName)
                .orElseGet(() -> {
                    Rol rol = new Rol();
                    rol.setNombre(roleName);
                    return rolRepository.save(rol);
                });
    }

    private Claims parseClaims(String token) {
        SecretKey signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
