package ElSilencioKoffee_Backend.controllers;

import ElSilencioKoffee_Backend.dto.AuthResponse;
import ElSilencioKoffee_Backend.dto.ChangePasswordRequest;
import ElSilencioKoffee_Backend.dto.LoginRequest;
import ElSilencioKoffee_Backend.dto.MessageResponse;
import ElSilencioKoffee_Backend.dto.PasswordRecoveryRequest;
import ElSilencioKoffee_Backend.dto.RegisterRequest;
import ElSilencioKoffee_Backend.services.IAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IAuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/password-recovery")
    public ResponseEntity<MessageResponse> passwordRecovery(@RequestBody PasswordRecoveryRequest request) {
        return ResponseEntity.ok(authService.passwordRecovery(request));
    }

    @PostMapping("/change-password")
    public ResponseEntity<MessageResponse> changePassword(Authentication authentication,
                                                         @RequestBody ChangePasswordRequest request) {
        return ResponseEntity.ok(authService.changePassword(authentication.getName(), request));
    }
}
