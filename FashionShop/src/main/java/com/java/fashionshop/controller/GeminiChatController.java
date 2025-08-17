package com.java.fashionshop.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import com.java.fashionshop.services.ChatProductService;

@RestController
@RequestMapping("/api/public/chat")
public class GeminiChatController {

    @Autowired
    private ChatProductService chatProductService;

    @PostMapping
    public ResponseEntity<String> chat(@RequestBody Map<String, String> request) {
        String userMessage = request.get("message");

        // Gọi ChatProductService để lấy câu trả lời (Gemini + DB context)
        String aiResponse = chatProductService.askAboutProduct(userMessage);

        return ResponseEntity.ok(aiResponse);
    }
}
