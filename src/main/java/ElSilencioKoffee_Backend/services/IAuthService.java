package ElSilencioKoffee_Backend.services;

import ElSilencioKoffee_Backend.dto.AuthResponse;
import ElSilencioKoffee_Backend.dto.LoginRequest;
import ElSilencioKoffee_Backend.dto.RegisterRequest;

public interface IAuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}
