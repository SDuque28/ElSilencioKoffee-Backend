package ElSilencioKoffee_Backend.services.impl;

import ElSilencioKoffee_Backend.entities.Order;
import ElSilencioKoffee_Backend.entities.Usuario;
import ElSilencioKoffee_Backend.repository.OrderRepository;
import ElSilencioKoffee_Backend.repository.UsuarioRepository;
import ElSilencioKoffee_Backend.services.IUsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements IUsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final OrderRepository orderRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> findAll() {
        return usuarioRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Usuario> findAll(Pageable pageable) {
        return usuarioRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario findById(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + id));
    }

    @Override
    @Transactional
    public Usuario update(Long id, String username, String email, Boolean activo) {
        Usuario usuario = findById(id);

        if (username != null) {
            validateUsername(username, id);
            usuario.setUsername(username.trim());
        }

        if (email != null) {
            validateEmail(email, id);
            usuario.setEmail(email.trim());
        }

        if (activo != null) {
            usuario.setActivo(activo);
        }

        return usuarioRepository.save(usuario);
    }

    @Override
    @Transactional
    public void softDelete(Long id) {
        Usuario usuario = findById(id);
        usuario.setActivo(false);
        usuarioRepository.save(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findOrdersByUserId(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new NoSuchElementException("User not found: " + id);
        }

        return orderRepository.findAllByUsuarioId(
                id,
                Sort.by(Sort.Direction.DESC, "orderDate", "id")
        );
    }

    private void validateUsername(String username, Long userId) {
        String normalized = username.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Username cannot be blank");
        }
        if (usuarioRepository.existsByUsernameAndIdNot(normalized, userId)) {
            throw new IllegalArgumentException("Username already taken: " + normalized);
        }
    }

    private void validateEmail(String email, Long userId) {
        String normalized = email.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Email cannot be blank");
        }
        if (usuarioRepository.existsByEmailAndIdNot(normalized, userId)) {
            throw new IllegalArgumentException("Email already taken: " + normalized);
        }
    }
}
