package ElSilencioKoffee_Backend.services.impl;

import ElSilencioKoffee_Backend.dto.AuthResponse;
import ElSilencioKoffee_Backend.dto.LoginRequest;
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
        if (usuarioRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already taken: " + request.getUsername());
        }
        if (usuarioRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already taken: " + request.getEmail());
        }

        Usuario usuario = new Usuario();
        usuario.setUsername(request.getUsername());
        usuario.setEmail(request.getEmail());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
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
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        Usuario usuario = usuarioRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + request.getUsername()));
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        return buildResponse(userDetails, usuario.getEmail());
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
}
