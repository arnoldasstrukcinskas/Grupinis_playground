package lt.viko.eif.astrukcinskas.grupinis_playground.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class ExternalApiService {

    private final RestClient restClient;
    private final String apiKey;

    public ExternalApiService(RestClient restClient, @Value("${external.api.key}") String apiKey) {
        this.restClient = restClient;
        this.apiKey = apiKey;
    }

    public String gerResponse(){
        String response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/hotels/search")
                        .queryParam("locale", "en-gb")
                        .queryParam("dest_type", "city")
                        .queryParam("dest_id", -553173)
                        .queryParam("checkin_date", "2027-01-11")
                        .queryParam("checkout_date", "2027-01-12")
                        .queryParam("room_number", 1)
                        .queryParam("adults_number", "2")
                        .queryParam("filter_by_currency", "USD")
                        .queryParam("order_by", "popularity")
                        .queryParam("units", "metric")
                        .build())
                .headers(httpHeaders -> {
                    httpHeaders.add("Content-Type", "application/json");
                    httpHeaders.add("x-rapidapi-host", "booking-com.p.rapidapi.com");
                    httpHeaders.add("x-rapidapi-key", apiKey);
                })
                .retrieve()
                .body(new ParameterizedTypeReference<String>() {});

        return response;
    }

}
