package ElSilencioKoffee_Backend.orders.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DeliveryStatus {

    PENDING("PENDING"),
    OUT_FOR_SHIPMENT("OUT_FOR_SHIPMENT"),
    DELIVERED("DELIVERED"),
    CANCELLED("CANCELLED");

    private final String databaseValue;

    @JsonValue
    public String toJson() {
        return databaseValue;
    }

    @JsonCreator
    public static DeliveryStatus fromValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.trim()
                .toUpperCase()
                .replace('-', '_')
                .replace(' ', '_');

        for (DeliveryStatus status : values()) {
            if (status.name().equals(normalized)) {
                return status;
            }
        }

        throw new IllegalArgumentException("Invalid delivery status: " + value);
    }
}
