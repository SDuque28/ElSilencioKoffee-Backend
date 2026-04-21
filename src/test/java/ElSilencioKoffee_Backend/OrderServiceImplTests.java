package ElSilencioKoffee_Backend;

import ElSilencioKoffee_Backend.dto.OrderCreateItemRequest;
import ElSilencioKoffee_Backend.entities.Order;
import ElSilencioKoffee_Backend.entities.OrderStatus;
import ElSilencioKoffee_Backend.entities.Product;
import ElSilencioKoffee_Backend.entities.Usuario;
import ElSilencioKoffee_Backend.repository.ProductRepository;
import ElSilencioKoffee_Backend.repository.UsuarioRepository;
import ElSilencioKoffee_Backend.services.IOrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class OrderServiceImplTests {

    @Autowired
    private IOrderService orderService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void createOrderBuildsTotalFromProductsAndPersistsDetails() {
        Usuario usuario = new Usuario();
        usuario.setUsername("checkout-user");
        usuario.setPassword("secret");
        usuario.setEmail("checkout@example.com");
        usuario.setActivo(true);
        usuario = usuarioRepository.save(usuario);

        Product productOne = new Product();
        productOne.setId(1L);
        productOne.setName("Ethiopian Yirgacheffe");
        productOne.setPrice(new BigDecimal("26.00"));
        productRepository.save(productOne);

        Product productTwo = new Product();
        productTwo.setId(2L);
        productTwo.setName("Espresso Capsules");
        productTwo.setPrice(new BigDecimal("18.00"));
        productRepository.save(productTwo);

        OrderCreateItemRequest firstItem = new OrderCreateItemRequest();
        firstItem.setProductId(1L);
        firstItem.setQuantity(2);

        OrderCreateItemRequest secondItem = new OrderCreateItemRequest();
        secondItem.setProductId(2L);
        secondItem.setQuantity(1);

        Order order = orderService.createOrder(usuario.getUsername(), List.of(firstItem, secondItem));

        assertEquals(usuario.getId(), order.getUsuario().getId());
        assertEquals(OrderStatus.NON_PAID, order.getStatus());
        assertEquals(new BigDecimal("70.00"), order.getTotalAmount());
        assertEquals(2, order.getOrderDetails().size());
        assertEquals(1L, order.getOrderDetails().getFirst().getProduct().getId());
        assertEquals(new BigDecimal("2"), order.getOrderDetails().getFirst().getQuantity());
        assertEquals(new BigDecimal("26.00"), order.getOrderDetails().getFirst().getUnitPrice());
    }

    @Test
    void createOrderRejectsEmptyItemList() {
        assertThrows(
                IllegalArgumentException.class,
                () -> orderService.createOrder("checkout-user", List.of())
        );
    }
}
