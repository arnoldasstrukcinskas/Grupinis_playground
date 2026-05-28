package lt.viko.eif.astrukcinskas.grupinis_playground.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class OllamaService {

    private final String systemPrompt = """
            You are a Travel Advisor. Match the [USER PROFILE] with the [HOTEL DATA] JSON to find the best hotels.
        
        Rules:
        - Only recommend hotels from the provided [HOTEL DATA]. Do not invent new ones.
        - Filter out hotels that conflict with user preferences (e.g., cold weather if they want warm).
        - If data is missing, make a logical guess based on location and mention it is your assumption.
        - Keep the tone professional and concise.
        
        You MUST strictly use this Markdown format:
        
        ### Top Recommendation
        **[Hotel Name]**
        - **Why it fits:** (1-2 sentences linking amenities to user profile).
        - **Highlights:** (Bullet points of matching features).
        
        ### Detailed Analysis
        Compare the top 2 hotels. For each, list:
        - **Pros:** Why it matches.
        - **Cons:** Any budget or preference trade-offs.
        
        ### Travel Tips
        - 2 quick tips based on user hobbies and destination.
        """;

    private final ChatClient chatClient;

    public OllamaService(ChatClient.Builder builder){
        this.chatClient = builder.build();
    }

    public String getResponse(String userPrompt){
        String response = chatClient
                .prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .content();

        return response;
    }
}
