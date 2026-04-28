package ElSilencioKoffee_Backend.services.impl;

import ElSilencioKoffee_Backend.dto.AuthResponse;
import ElSilencioKoffee_Backend.dto.ChangePasswordRequest;
import ElSilencioKoffee_Backend.dto.LoginRequest;
import ElSilencioKoffee_Backend.dto.MessageResponse;
import ElSilencioKoffee_Backend.dto.PasswordRecoveryRequest;
import ElSilencioKoffee_Backend.dto.RegisterRequest;
import ElSilencioKoffee_Backend.entities.Usuario;
import ElSilencioKoffee_Backend.entities.UsuarioRol;
import ElSilencioKoffee_Backend.entities.UsuarioRolId;
import ElSilencioKoffee_Backend.repository.RolRepository;
import ElSilencioKoffee_Backend.repository.UsuarioRepository;
import ElSilencioKoffee_Backend.repository.UsuarioRolRepository;
import ElSilencioKoffee_Backend.security.JwtUtil;
import ElSilencioKoffee_Backend.services.IAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final UsuarioRolRepository usuarioRolRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String username = normalizeRequired(request.getUsername(), "Username is required");
        String email = normalizeRequired(request.getEmail(), "Email is required");
        String rawPassword = requirePassword(request.getPassword(), "Password is required");

        if (usuarioRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username already taken: " + username);
        }
        if (usuarioRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email already taken: " + email);
        }

        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setEmail(email);
        usuario.setPassword(passwordEncoder.encode(rawPassword));
        usuario.setActivo(true);
        usuarioRepository.save(usuario);

        // Assign default ROLE_USER if it exists in the rol table
        rolRepository.findByNombre("ROLE_USER").ifPresent(rol -> {
            UsuarioRolId id = new UsuarioRolId(usuario.getId(), rol.getId());
            UsuarioRol usuarioRol = new UsuarioRol(id, usuario, rol);
            usuarioRolRepository.save(usuarioRol);
            usuario.getUsuariosRoles().add(usuarioRol);
        });

        UserDetails userDetails = userDetailsService.loadUserByUsername(usuario.getUsername());
        return buildResponse(userDetails, usuario.getEmail());
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        String username = normalizeRequired(request.getUsername(), "Username is required");
        String password = requirePassword(request.getPassword(), "Password is required");

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        return buildResponse(userDetails, usuario.getEmail());
    }

    @Override
    @Transactional
    public MessageResponse passwordRecovery(PasswordRecoveryRequest request) {
        validatePasswordUpdate(request.getNewPassword(), request.getConfirmPassword());

        Usuario usuario = usuarioRepository.findByUsernameAndEmail(request.getUsername(), request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("No user found with the provided username and email"));

        validateActiveUser(usuario);
        updatePassword(usuario, request.getNewPassword());

        return new MessageResponse("Password updated successfully");
    }

    @Override
    @Transactional
    public MessageResponse changePassword(String username, ChangePasswordRequest request) {
        validatePasswordUpdate(request.getNewPassword(), request.getConfirmPassword());

        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        validateActiveUser(usuario);

        if (isBlank(request.getCurrentPassword())) {
            throw new IllegalArgumentException("Current password is required");
        }
        if (!passwordEncoder.matches(request.getCurrentPassword(), usuario.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        if (passwordEncoder.matches(request.getNewPassword(), usuario.getPassword())) {
            throw new IllegalArgumentException("The new password must be different from the current password");
        }

        updatePassword(usuario, request.getNewPassword());

        return new MessageResponse("Password changed successfully");
    }

    private AuthResponse buildResponse(UserDetails userDetails, String email) {
        String token = jwtUtil.generateToken(userDetails);
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setUsername(userDetails.getUsername());
        response.setEmail(email);
        response.setRoles(roles);
        return response;
    }

    private void validatePasswordUpdate(String newPassword, String confirmPassword) {
        if (isBlank(newPassword) || isBlank(confirmPassword)) {
            throw new IllegalArgumentException("New password and confirmation are required");
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("New password and confirmation do not match");
        }
    }

    private void validateActiveUser(Usuario usuario) {
        if (Boolean.FALSE.equals(usuario.getActivo())) {
            throw new IllegalArgumentException("The user is inactive");
        }
    }

    private void updatePassword(Usuario usuario, String rawPassword) {
        usuario.setPassword(passwordEncoder.encode(rawPassword));
        usuarioRepository.save(usuario);
    }

    private String normalizeRequired(String value, String message) {
        if (isBlank(value)) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private String requirePassword(String value, String message) {
        if (isBlank(value)) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
