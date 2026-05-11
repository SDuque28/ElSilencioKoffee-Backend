package ElSilencioKoffee_Backend.orders.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentMethod {

    CREDIT_CARD("CREDIT_CARD"),
    DEBIT_CARD("DEBIT_CARD");

    private final String databaseValue;

    @JsonValue
    public String toJson() {
        return databaseValue;
    }

    @JsonCreator
    public static PaymentMethod fromValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.trim()
                .toUpperCase()
                .replace('-', '_')
                .replace(' ', '_');

        for (PaymentMethod method : values()) {
            if (method.name().equals(normalized)) {
                return method;
            }
        }

        throw new IllegalArgumentException("Invalid payment method: " + value);
    }
}
