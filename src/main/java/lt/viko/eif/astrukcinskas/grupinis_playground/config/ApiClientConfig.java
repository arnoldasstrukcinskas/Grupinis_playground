package lt.viko.eif.astrukcinskas.grupinis_playground.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class ApiClientConfig {

//    For api V2 link: https://booking-com.p.rapidapi.com/v2

    @Bean
    public RestClient restClient(){
        return RestClient.builder()
                .baseUrl("https://booking-com.p.rapidapi.com")
                .requestInterceptor(((request, body, execution) -> {
                    System.out.println("External API request URL: " + request.getURI());
                    System.out.println("External API method: " + request.getMethod());
                    return execution.execute(request, body);
                }))
                .build();
    }

}

