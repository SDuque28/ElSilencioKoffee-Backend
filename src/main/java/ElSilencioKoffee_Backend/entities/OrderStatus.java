package ElSilencioKoffee_Backend.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderStatus {

    PAID("PAID"),
    NON_PAID("NON PAID");

    private final String databaseValue;

    @JsonValue
    public String toJson() {
        return databaseValue;
    }

    @JsonCreator
    public static OrderStatus fromValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.trim()
                .toUpperCase()
                .replace('-', '_')
                .replace(' ', '_');

        for (OrderStatus status : values()) {
            if (status.name().equals(normalized)) {
                return status;
            }
        }

        throw new IllegalArgumentException("Invalid order status: " + value);
    }
}
