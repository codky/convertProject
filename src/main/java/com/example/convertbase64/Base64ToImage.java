package com.example.convertbase64;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;

public class Base64ToImage {
    public static void main(String[] args) {
        // Base64 데이터를 파일에서 읽기
        File base64File = new File("C:\\Users\\MEDIAZEN\\Desktop\\변환폴더(수요일까지)\\이미지1.txt"); // Base64 문자열이 저장된 파일
        try {
            String base64Data = Files.readString(base64File.toPath());

            // 디코딩하여 byte 배열로 변환
            byte[] imageBytes = Base64.getDecoder().decode(base64Data);

            // 디코딩된 데이터를 파일로 저장
            try (FileOutputStream fos = new FileOutputStream("decodedImage.jpg")) {
                fos.write(imageBytes);
                System.out.println("이미지가 'decodedImage.jpg' 파일로 저장되었습니다.");
            }
        } catch (IOException e) {
            System.err.println("오류 발생: " + e.getMessage());
        }
    }
}
