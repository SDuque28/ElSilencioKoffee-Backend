package ElSilencioKoffee_Backend.services;

import ElSilencioKoffee_Backend.dto.AuthResponse;
import ElSilencioKoffee_Backend.dto.ChangePasswordRequest;
import ElSilencioKoffee_Backend.dto.LoginRequest;
import ElSilencioKoffee_Backend.dto.MessageResponse;
import ElSilencioKoffee_Backend.dto.PasswordRecoveryRequest;
import ElSilencioKoffee_Backend.dto.RegisterRequest;

public interface IAuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    MessageResponse passwordRecovery(PasswordRecoveryRequest request);

    MessageResponse changePassword(String username, ChangePasswordRequest request);
}
