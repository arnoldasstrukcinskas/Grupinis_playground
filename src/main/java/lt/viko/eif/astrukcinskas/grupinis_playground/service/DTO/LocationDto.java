package lt.viko.eif.astrukcinskas.grupinis_playground.service.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({
        "country",
        "destinationId",
        "destinationName",
        "destinationType"
})

public class LocationDto {

    @JsonProperty("dest_id")
    private int destinationId;

    @JsonProperty("name")
    private String destinationName;

    @JsonProperty("country")
    private String country;

    @JsonProperty("dest_type")
    private String destinationType;

    public String getDestinationType() {
        return destinationType;
    }

    public void setDestinationType(String destinationType) {
        this.destinationType = destinationType;
    }

    public int getDestinationId() {
        return destinationId;
    }

    public void setDestinationId(int destinationId) {
        this.destinationId = destinationId;
    }

    public String getDestinationName() {
        return destinationName;
    }

    public void setDestinationName(String destinationName) {
        this.destinationName = destinationName;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
