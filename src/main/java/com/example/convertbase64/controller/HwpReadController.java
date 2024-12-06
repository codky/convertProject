package com.example.convertbase64.controller;

import kr.dogfoot.hwplib.object.HWPFile;
import kr.dogfoot.hwplib.object.bodytext.Section;
import kr.dogfoot.hwplib.object.bodytext.control.Control;
import kr.dogfoot.hwplib.object.bodytext.control.ControlTable;
import kr.dogfoot.hwplib.object.bodytext.control.ControlType;
import kr.dogfoot.hwplib.object.bodytext.control.table.Cell;
import kr.dogfoot.hwplib.object.bodytext.control.table.Row;
import kr.dogfoot.hwplib.object.bodytext.paragraph.Paragraph;
import kr.dogfoot.hwplib.reader.HWPReader;
import kr.dogfoot.hwplib.tool.objectfinder.ControlFilter;
import kr.dogfoot.hwplib.tool.objectfinder.ControlFinder;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;

@Controller
public class HwpReadController implements ControlFilter {


    @Override
    public boolean isMatched(Control control, Paragraph paragraph, Section section) {
        return control.getType() == ControlType.Table;
    }

    public void readAndPrintTable(String filePath) {
        try {
            HWPFile hwpFile = HWPReader.fromFile(filePath);
            if (hwpFile != null) {
                ArrayList<Control> result = ControlFinder.find(hwpFile, this);
                if (result != null && !result.isEmpty()) {
                    Control control = result.get(0); // 첫 번째 테이블 가져오기
                    ControlTable table = (ControlTable) control;

                    System.out.println("========== 결과 ==========");
                    for (Row row : table.getRowList()) {
                        for (Cell cell : row.getCellList()) {
                            System.out.print(cell.getParagraphList().getNormalString() + " | ");
                        }
                        System.out.println();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void readAndPrintTableAll(String filePath) {
        try {
            HWPFile hwpFile = HWPReader.fromFile(filePath);
            if (hwpFile != null) {
                ArrayList<Control> tables = ControlFinder.find(hwpFile, this); // 모든 테이블 찾기
                if (tables != null && !tables.isEmpty()) {
                    System.out.println("========== 결과 ==========");
                    int tableIndex = 1;
                    for (Control control : tables) {
                        if (control instanceof ControlTable) { // 테이블인지 확인
                            ControlTable table = (ControlTable) control;
                            System.out.println("\n[테이블 " + tableIndex + "]");
                            for (Row row : table.getRowList()) {
                                for (Cell cell : row.getCellList()) {
                                    System.out.print(cell.getParagraphList().getNormalString() + " | ");
                                }
                                System.out.println();
                            }
                            tableIndex++;
                        }
                    }
                } else {
                    System.out.println("테이블이 없습니다.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void readAndPrintText(String filePath) {
        try {
            HWPFile hwpFile = HWPReader.fromFile(filePath);
            if (hwpFile != null) {
                System.out.println("========== 한글 파일의 텍스트 ==========");
                // 섹션 리스트 가져오기
                for (Section section : hwpFile.getBodyText().getSectionList()) {
                    // 각 섹션의 문단 리스트 순회
                    for (Paragraph paragraph : section.getParagraphs()) { // 최신 API에서는 getParagraphArray 사용
                        try {
                            String text = paragraph.getNormalString(); // 문단의 텍스트 가져오기
                            if (text != null && !text.isEmpty()) {
                                System.out.println(text); // 콘솔 출력
                            }
                        } catch (Exception e) {
                            System.err.println("문단 텍스트를 가져오는 중 오류 발생:");
                            e.printStackTrace();
                        }
                    }
                }
            } else {
                System.out.println("HWP 파일을 읽을 수 없습니다.");
            }
        } catch (Exception e) {
            System.err.println("파일 읽기 실패:");
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        HwpReadController hwpReader = new HwpReadController();
        String filePath = "C:\\Users\\MEDIAZEN\\Desktop\\변환폴더(수요일까지)\\(우성정공_구축및고도화) 최종완료보고서.hwp"; // 예시 파일 경로
        if (hwpReader.isHwpFile(filePath)) {
            //hwpReader.readAndPrintText(filePath);
            //hwpReader.readAndPrintTable(filePath);
            hwpReader.readAndPrintTableAll(filePath);
        } else {
            System.out.println("확장자가 hwp나 hwpx가 아닌 파일은 실행할 수 없습니다: " + filePath);
        }
    }

    public boolean isHwpFile(String filename) {
        String extension = "";
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0) {
            extension = filename.substring(lastDotIndex + 1);
        }
        return extension.equalsIgnoreCase("hwp") || extension.equalsIgnoreCase("hwpx");
    }
}
