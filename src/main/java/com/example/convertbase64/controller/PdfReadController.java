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
        String filePath = "C:\\Users\\MEDIAZEN\\Desktop\\변환폴더(수요일까지)\\(베이시스푸드_대중소)_완료보고서_V2.pdf"; // PDF 파일 경로

        try (PDDocument document = PDDocument.load(new File(filePath))) {
            // PDF 페이지 수 출력
            System.out.println("Total Pages: " + document.getNumberOfPages());

            // PDF 텍스트 추출
            PDFTextStripper pdfStripper = new PDFTextStripper();
            pdfStripper.setStartPage(1); // 시작 페이지
            pdfStripper.setEndPage(5);   // 끝 페이지
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
