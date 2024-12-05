package com.example.convertbase64.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AudioService {

    private static final String OUTPUT_DIRECTORY = "C:/output"; // 저장할 디렉터리 경로

    public List<String> convertAndSaveWavFiles(MultipartFile[] files) throws IOException {
        List<String> savedFilePaths = new ArrayList<>();

        for (MultipartFile file : files) {
            String base64Data = convertWavToBase64(file);
            String fileName = file.getOriginalFilename().replace(".wav", ".txt");

            // Base64 데이터를 텍스트 파일로 저장
            String savedFilePath = saveBase64ToFile(fileName, base64Data);
            savedFilePaths.add(savedFilePath);
        }

        return savedFilePaths;
    }

    private String convertWavToBase64(MultipartFile file) throws IOException {
        // 파일을 바이트 배열로 읽어서 Base64로 인코딩
        byte[] fileBytes = file.getBytes();
        return Base64.getEncoder().encodeToString(fileBytes);
    }

    private String saveBase64ToFile(String fileName, String base64Data) throws IOException {
        // 디렉터리가 없으면 생성
        File directory = new File(OUTPUT_DIRECTORY);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // .txt 파일 경로 생성
        File outputFile = new File(directory, fileName);

        // Base64 데이터를 파일로 저장
        try (FileWriter writer = new FileWriter(outputFile)) {
            writer.write(base64Data);
        }

        return outputFile.getAbsolutePath();
    }
}
