package com.example.convertbase64.controller;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Controller;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Controller
public class PdfReadController {
    public static void main(String[] args) {
        String filePath = "C:\\Users\\MEDIAZEN\\Downloads\\제안요청서_위기관리.pdf"; // PDF 파일 경로

        try (PDDocument document = PDDocument.load(new File(filePath))) {
            // PDF 페이지 수 출력
            System.out.println("Total Pages: " + document.getNumberOfPages());

            // PDF 텍스트 추출
            PDFTextStripper pdfStripper = new PDFTextStripper();
            pdfStripper.setSortByPosition(true); // 텍스트 정렬 설정
            String rawText = pdfStripper.getText(document);

            System.out.println("RawText :");
            System.out.println(rawText);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to load PDF file.");
        }
    }
}
