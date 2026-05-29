package lt.viko.eif.astrukcinskas.grupinis_playground.service;

import lt.viko.eif.astrukcinskas.grupinis_playground.service.DTO.HotelDto;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OllamaService {

    private final String systemPrompt = """
            You are a hotel ranking engine.

            CRITICAL RULES:
            1. You may ONLY use hotels that appear in the HOTEL_LIST section.
            2. If a hotel name is not present in HOTEL_LIST, it is FORBIDDEN to mention it.
            3. Before generating the answer, verify that every hotel mentioned exists in HOTEL_LIST.
            4. If fewer than 5 matching hotels exist, return fewer than 5 hotels.
            5. Never invent hotel names, locations, restaurants, amenities, ratings, prices, or reviews.
            6. Use only the data explicitly provided for each hotel.
            7. If information is missing, write "Information not provided".
            
            TASK:
            - Analyze ALL hotels in HOTEL_LIST.
            - Rank the best 5 hotels according to USER_REQUEST.
            - Explain why each hotel was ranked.
            - Compare the top 2 hotels.
            - Output only information derived from HOTEL_LIST.
            
            SELF-CHECK:
            Before finalizing the response:
            - Ensure every mentioned hotel exists in HOTEL_LIST.
            - Ensure no external knowledge was used.
            - Ensure every claim can be traced to provided hotel data.
            """;

    private final ChatClient chatClient;
    private final HotelsService hotelsService;

    public OllamaService(ChatClient.Builder builder, HotelsService hotelsService){
        this.chatClient = builder.build();
        this.hotelsService = hotelsService;
    }

    public String getResponse(String userPrompt, String hobiesAndInterests){
        List<HotelDto> hotels = hotelsService.getHotels();

        //For testing
        if (hotels == null) {
            hotels = new ArrayList<>();
        }

        ObjectMapper objectMapper = new ObjectMapper();
        String hotelsJson = objectMapper.writeValueAsString(hotels);

        String hotelText = hotels.stream()
                .map(h -> String.format(
                        "Hotel: %s | Stars: %d | District: %s | Center: %s | Price: %s | Score: %.1f | Features: %s",
                        h.getHotelName(),
                        h.getHotelStars(),
                        h.getDistrict(),
                        h.getDistanceToCenter(),
                        h.getPrice(),
                        h.getReviewScoreNumber(),
                        h.getAdditionals()
                ))
                .collect(Collectors.joining("\n"));

        String prompt = userPrompt + " Hobies and interests: " + hobiesAndInterests + "\n" + hotelText;
//        System.out.println(prompt);

        String response = chatClient
                .prompt()
                .system(systemPrompt)
                .user(prompt.formatted(hotelText))
                .call()
                .content();

        return response;
    }
}
