package com.example.convertbase64.service;

import kr.dogfoot.hwplib.object.HWPFile;
import kr.dogfoot.hwplib.object.bodytext.Section;
import kr.dogfoot.hwplib.object.bodytext.control.Control;
import kr.dogfoot.hwplib.object.bodytext.control.ControlTable;
import kr.dogfoot.hwplib.object.bodytext.control.ControlType;
import kr.dogfoot.hwplib.object.bodytext.control.table.Cell;
import kr.dogfoot.hwplib.object.bodytext.control.table.Row;
import kr.dogfoot.hwplib.object.bodytext.paragraph.Paragraph;
import kr.dogfoot.hwplib.reader.HWPReader;
import kr.dogfoot.hwplib.tool.objectfinder.ControlFinder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class HwpReadService {

    public String readAndPrintTable(String filePath) {
        StringBuilder result = new StringBuilder();
        try {
            HWPFile hwpFile = HWPReader.fromFile(filePath);
            if (hwpFile != null) {
                ArrayList<Control> controls = ControlFinder.find(hwpFile, (control, paragraph, section) -> control.getType() == ControlType.Table);
                if (controls != null && !controls.isEmpty()) {
                    ControlTable table = (ControlTable) controls.get(0); // 첫 번째 테이블
                    result.append("========== 테이블 1 ==========\n");
                    for (Row row : table.getRowList()) {
                        for (Cell cell : row.getCellList()) {
                            result.append(cell.getParagraphList().getNormalString()).append(" | ");
                        }
                        result.append("\n");
                    }
                } else {
                    result.append("테이블을 찾을 수 없습니다.");
                }
            } else {
                result.append("HWP 파일을 읽을 수 없습니다.");
            }
        } catch (Exception e) {
            result.append("오류 발생: ").append(e.getMessage());
        }
        return result.toString();
    }

    public String readAndPrintTableAll(String filePath) {
        StringBuilder result = new StringBuilder();
        try {
            HWPFile hwpFile = HWPReader.fromFile(filePath);
            if (hwpFile != null) {
                ArrayList<Control> controls = ControlFinder.find(hwpFile, (control, paragraph, section) -> control.getType() == ControlType.Table);
                if (controls != null && !controls.isEmpty()) {
                    int tableIndex = 1;
                    for (Control control : controls) {
                        if (control instanceof ControlTable) {
                            ControlTable table = (ControlTable) control;
                            result.append("\n========== 테이블 ").append(tableIndex).append(" ==========\n");
                            for (Row row : table.getRowList()) {
                                for (Cell cell : row.getCellList()) {
                                    result.append(cell.getParagraphList().getNormalString()).append(" | ");
                                }
                                result.append("\n");
                            }
                            tableIndex++;
                        }
                    }
                } else {
                    result.append("테이블을 찾을 수 없습니다.");
                }
            } else {
                result.append("HWP 파일을 읽을 수 없습니다.");
            }
        } catch (Exception e) {
            result.append("오류 발생: ").append(e.getMessage());
        }
        return result.toString();
    }

    public String readAndPrintText(String filePath) {
        StringBuilder result = new StringBuilder();
        try {
            HWPFile hwpFile = HWPReader.fromFile(filePath);
            if (hwpFile != null) {
                result.append("========== 텍스트 ==========\n");
                for (Section section : hwpFile.getBodyText().getSectionList()) {
                    for (Paragraph paragraph : section.getParagraphs()) { // getParagraphs로 수정
                        String text = paragraph.getNormalString();
                        if (text != null && !text.isEmpty()) {
                            result.append(text).append("\n");
                        }
                    }
                }
            } else {
                result.append("HWP 파일을 읽을 수 없습니다.");
            }
        } catch (Exception e) {
            result.append("오류 발생: ").append(e.getMessage());
        }
        return result.toString();
    }
}