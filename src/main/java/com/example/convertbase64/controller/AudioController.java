package com.example.convertbase64.controller;

import com.example.convertbase64.service.AudioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/audio")
public class AudioController {

    private final AudioService audioService;

    public AudioController(AudioService audioService) {
        this.audioService = audioService;
    }

    @PostMapping("/convert-and-save")
    public ResponseEntity<List<String>> convertAndSaveFiles(@RequestParam("files") MultipartFile[] files) {
        try {
            // 파일 변환 및 저장
            List<String> savedFiles = audioService.convertAndSaveWavFiles(files);
            return ResponseEntity.ok(savedFiles);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(List.of("Error: " + e.getMessage()));
        }
    }
}
