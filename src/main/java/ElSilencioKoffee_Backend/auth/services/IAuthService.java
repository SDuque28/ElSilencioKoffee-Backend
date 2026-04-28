package ElSilencioKoffee_Backend.auth.services;

import ElSilencioKoffee_Backend.auth.dto.AuthResponse;
import ElSilencioKoffee_Backend.auth.dto.ChangePasswordRequest;
import ElSilencioKoffee_Backend.auth.dto.LoginRequest;
import ElSilencioKoffee_Backend.auth.dto.PasswordRecoveryRequest;
import ElSilencioKoffee_Backend.auth.dto.RegisterRequest;
import ElSilencioKoffee_Backend.shared.dto.MessageResponse;

public interface IAuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    MessageResponse passwordRecovery(PasswordRecoveryRequest request);

    MessageResponse changePassword(String username, ChangePasswordRequest request);
}

