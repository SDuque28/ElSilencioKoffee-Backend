package ElSilencioKoffee_Backend.services.impl;

import ElSilencioKoffee_Backend.entities.Usuario;
import ElSilencioKoffee_Backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsernameWithRoles(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        return User.builder()
                .username(usuario.getUsername())
                .password(usuario.getPassword())
                .disabled(Boolean.FALSE.equals(usuario.getActivo()))
                .authorities(
                        usuario.getUsuariosRoles().stream()
                                .map(usuarioRol -> new SimpleGrantedAuthority(usuarioRol.getRol().getNombre()))
                                .toList()
                )
                .build();
    }
}
