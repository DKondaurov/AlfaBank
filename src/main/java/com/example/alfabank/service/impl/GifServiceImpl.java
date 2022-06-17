package com.example.alfabank.service.impl;

import com.example.alfabank.client.GifClient;
import com.example.alfabank.service.GifService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class GifServiceImpl implements GifService {

    private GifClient gifClient;

    @Value("${giphy.api.key}")
    private String apiKey;

    public GifServiceImpl(GifClient gifClient) {
        this.gifClient = gifClient;
    }

    @Override
    public ResponseEntity<Map> getGif(String tag) {
        ResponseEntity<Map> result = gifClient.getRandomGif(this.apiKey, tag);
        return result;
    }
}
