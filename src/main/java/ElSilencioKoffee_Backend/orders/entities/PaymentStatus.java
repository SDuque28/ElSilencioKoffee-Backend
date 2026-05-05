package ElSilencioKoffee_Backend.orders.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentStatus {

    APPROVED("APPROVED"),
    DECLINED("DECLINED");

    private final String databaseValue;

    @JsonValue
    public String toJson() {
        return databaseValue;
    }

    @JsonCreator
    public static PaymentStatus fromValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.trim()
                .toUpperCase()
                .replace('-', '_')
                .replace(' ', '_');

        for (PaymentStatus status : values()) {
            if (status.name().equals(normalized)) {
                return status;
            }
        }

        throw new IllegalArgumentException("Invalid payment status: " + value);
    }
}
