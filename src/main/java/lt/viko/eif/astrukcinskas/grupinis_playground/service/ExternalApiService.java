package lt.viko.eif.astrukcinskas.grupinis_playground.service;

import lt.viko.eif.astrukcinskas.grupinis_playground.service.DTO.HotelDto;
import lt.viko.eif.astrukcinskas.grupinis_playground.service.DTO.LocationDto;
import lt.viko.eif.astrukcinskas.grupinis_playground.service.DTO.RequestDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

@Service
public class ExternalApiService {

    private final RestClient restClient;
    private final String apiKey;
    private final HotelsService hotelsService;

    public ExternalApiService(RestClient restClient, @Value("${external.api.key}") String apiKey, HotelsService hotelsService) {
        this.restClient = restClient;
        this.apiKey = apiKey;
        this.hotelsService = hotelsService;
    }

    public List<HotelDto> getHotels(RequestDto requestDto){

        RequestDto request = recreateRequest(requestDto);

        System.out.println(request);
        JsonNode response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/hotels/search")
                        .queryParam("locale", request.getLocale())
                        .queryParam("dest_type", request.getDestinationType())
                        .queryParam("dest_id", request.getDestinationId())
                        .queryParam("checkin_date", request.getCheckInDate())
                        .queryParam("checkout_date", request.getCheckOutDate())
                        .queryParam("room_number", request.getRoomNumber())
                        .queryParam("adults_number", request.getAdultsNumber())
                        .queryParam("filter_by_currency", request.getFilterByCurrency())
                        .queryParam("order_by", request.getOrderBy())
                        .queryParam("units", request.getUnits())
                        .queryParam("include_adjacency", request.getIncludeAdjency())
                        .build())
                .headers(httpHeaders -> {
                    httpHeaders.add("Content-Type", "application/json");
                    httpHeaders.add("x-rapidapi-host", "booking-com.p.rapidapi.com");
                    httpHeaders.add("x-rapidapi-key", apiKey);
                })
                .retrieve()
                .body(JsonNode.class);

        List<HotelDto> hotels = jsonDataReader(response);
        hotelsService.setHotels(hotels);

        return hotels;
    }

    public List<LocationDto> getLocations(String locationName){
        List<LocationDto> locations = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/hotels/locations")
                        .queryParam("locale", "en-gb")
                        .queryParam("name", locationName)
                        .build()
                ).headers(httpHeaders -> {
                    httpHeaders.add("Content-Type", "application/json");
                    httpHeaders.add("x-rapidapi-host", "booking-com.p.rapidapi.com");
                    httpHeaders.add("x-rapidapi-key", apiKey);
                })
                .retrieve()
                .body(new ParameterizedTypeReference<List<LocationDto>>() {});


        return locations;
    }

    //Helpers
    private RequestDto recreateRequest(RequestDto requestDto)
    {
        RequestDto refactoredRequest = new RequestDto();
        refactoredRequest.setDestinationId(requestDto.getDestinationId());
        refactoredRequest.setCheckInDate(requestDto.getCheckInDate());
        refactoredRequest.setCheckOutDate(requestDto.getCheckOutDate());
        refactoredRequest.setAdultsNumber(requestDto.getAdultsNumber());
        refactoredRequest.setRoomNumber(requestDto.getRoomNumber());
        refactoredRequest.setHobbiesAndInterests(requestDto.getHobbiesAndInterests());
        refactoredRequest.setPromptToOllama(requestDto.getPromptToOllama());

        return refactoredRequest;
    }

    private List<HotelDto> jsonDataReader(JsonNode jsonNode){
        List<HotelDto> hotels = new ArrayList<>();

        JsonNode hotelsArray = jsonNode.path("result");

        for (JsonNode hotel : hotelsArray){
            HotelDto hotelDto = new HotelDto();

            hotelDto.setHotelName(hotel.path("hotel_name").asString());
            hotelDto.setAccomodationType(hotel.path("accommodation_type_name").asString());
            hotelDto.setHotelStars(hotel.path("class").asInt());
            hotelDto.setDistrict(hotel.path("district").asString());
            hotelDto.setDistanceToCenter(hotel.path("distance_to_cc_formatted").asString());
            hotelDto.setBeachFront(hotel.path("is_beach_front").asBoolean());
            hotelDto.setPrice(hotel.path("composite_price_breakdown")
                    .path("gross_amount")
                    .path("amount_unrounded")
                    .asString());
            hotelDto.setPriceAllInclusive(hotel.path("composite_price_breakdown")
                    .path("all_inclusive_amount_hotel_currency")
                    .path("amount_unrounded")
                    .asString());
            hotelDto.setAddress(hotel.path("address").asString());
            hotelDto.setMainPhotoUrl(hotel.path("main_photo_url").asString());
            hotelDto.setMaxPhotoUrl(hotel.path("max_photo_url").asString());
            hotelDto.setPhotoUrl1440(hotel.path("max_1440_photo_url"). asString());
            hotelDto.setReviewScoreNumber(hotel.path("review_score").asDouble());
            hotelDto.setReviewScoreWord(hotel.path("review_score_word").asString());
            hotelDto.setReviewNumber(hotel.path("review_nr").asInt());
            hotelDto.setAdditionals(hotel.path("ribbon_text").asString());

            hotels.add(hotelDto);
        }

        return hotels;
    }
}
