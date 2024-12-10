package com.example.convertbase64.controller;

import com.example.convertbase64.service.HwpReadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/hwp")
public class HwpReadController {

    private final HwpReadService hwpReadService;

    @Autowired
    public HwpReadController(HwpReadService hwpReadService) {
        this.hwpReadService = hwpReadService;
    }

    @PostMapping("/read-content-img")
    public String readContentFromFileWithImg(@RequestParam("file") MultipartFile file) {
        try {
            // 임시 파일 생성
            File tempFile = File.createTempFile("uploaded", ".hwp");
            file.transferTo(tempFile);

            // HWP 파일 처리
            String result = hwpReadService.processHwpFileWithImg(tempFile.getAbsolutePath());

            // 임시 파일 삭제
            tempFile.delete();

            return result;
        } catch (IOException e) {
            return "파일 처리 중 오류 발생: " + e.getMessage();
        }
    }

    @PostMapping("/read-content")
    public String readContentFromFile(@RequestParam("file") MultipartFile file) {
        try {
            // 임시 파일 생성
            File tempFile = File.createTempFile("uploaded", ".hwp");
            file.transferTo(tempFile);

            // HWP 파일 처리
            String result = hwpReadService.processHwpFile(tempFile.getAbsolutePath());

            // 임시 파일 삭제
            tempFile.delete();

            return result;
        } catch (IOException e) {
            return "파일 처리 중 오류 발생: " + e.getMessage();
        }
    }

    @PostMapping("/read-table")
    public String readTableFromFile(@RequestParam("file") MultipartFile file) {
        try {
            // 임시 파일 생성
            File tempFile = File.createTempFile("uploaded", ".hwp");
            file.transferTo(tempFile);

            // HWP 파일 처리
            String result = hwpReadService.readAndPrintTable(tempFile.getAbsolutePath());

            // 임시 파일 삭제
            tempFile.delete();

            return result;
        } catch (IOException e) {
            return "파일 처리 중 오류 발생: " + e.getMessage();
        }
    }

    @PostMapping("/read-all-tables")
    public String readAllTablesFromFile(@RequestParam("file") MultipartFile file) {
        try {
            // 임시 파일 생성
            File tempFile = File.createTempFile("uploaded", ".hwp");
            file.transferTo(tempFile);

            // HWP 파일 처리
            String result = hwpReadService.readAndPrintTableAll(tempFile.getAbsolutePath());

            // 임시 파일 삭제
            tempFile.delete();

            return result;
        } catch (IOException e) {
            return "파일 처리 중 오류 발생: " + e.getMessage();
        }
    }

    @PostMapping("/read-text")
    public String readTextFromFile(@RequestParam("file") MultipartFile file) {
        try {
            // 임시 파일 생성
            File tempFile = File.createTempFile("uploaded", ".hwp");
            file.transferTo(tempFile);

            // HWP 파일 처리
            String result = hwpReadService.readAndPrintText(tempFile.getAbsolutePath());

            // 임시 파일 삭제
            tempFile.delete();

            return result;
        } catch (IOException e) {
            return "파일 처리 중 오류 발생: " + e.getMessage();
        }
    }

//    public static void main(String[] args) {
//        HwpReadController hwpReader = new HwpReadController();
//        String filePath = "C:\\Users\\MEDIAZEN\\Desktop\\변환폴더(수요일까지)\\(우성정공_구축및고도화) 최종완료보고서.hwp"; // 예시 파일 경로
//        if (hwpReader.isHwpFile(filePath)) {
//            //hwpReader.readAndPrintText(filePath);
//            //hwpReader.readAndPrintTable(filePath);
//            hwpReader.readAndPrintTableAll(filePath);
//        } else {
//            System.out.println("확장자가 hwp나 hwpx가 아닌 파일은 실행할 수 없습니다: " + filePath);
//        }
//    }


}
