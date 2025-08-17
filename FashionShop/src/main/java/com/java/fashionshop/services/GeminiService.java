package com.java.fashionshop.services;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class GeminiService {

    private static final String API_KEY = "AIzaSyCuzlE32NfPr9SVz9g0m1eTdWdwRdQNo6A";
    private static final String API_URL =
    	    "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + API_KEY;

    public String askGemini(String message) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String body = """
        {
          "contents": [
            {"parts":[{"text":"%s"}]}
          ]
        }
        """.formatted(message);

        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        return restTemplate.postForObject(API_URL, entity, String.class);
    }
}
