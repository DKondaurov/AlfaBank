package com.example.alfabank.controller;

import com.example.alfabank.service.ExchangeService;
import com.example.alfabank.service.GifService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ExchangeController {

    @Value("${giphy.rich}")
    private String richTag;
    @Value("${giphy.broke}")
    private String brokeTag;

    private final GifService gifService;

    private final ExchangeService exchangeService;

    public ExchangeController(GifService gifService, ExchangeService exchangeService) {
        this.gifService = gifService;
        this.exchangeService = exchangeService;
    }

    @GetMapping("/gif/{currency}")
    public ResponseEntity<Map> getGif(@PathVariable String currency) {
        ResponseEntity<Map> result = null;
        int gifKey = 0;
        String gifTag = null;
        if (currency != null) {
            gifKey = exchangeService.getKeyForTag(currency);
        }
        switch (gifKey) {
            case 1:
                gifTag = this.richTag;
                break;
            case -1:
                gifTag = this.brokeTag;
                break;
        }
        result = gifService.getGif(gifTag);
        return result;
    }
}
