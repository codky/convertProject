package com.example.convertbase64.service;

import kr.dogfoot.hwplib.object.HWPFile;
import kr.dogfoot.hwplib.object.bodytext.Section;
import kr.dogfoot.hwplib.object.bodytext.control.Control;
import kr.dogfoot.hwplib.object.bodytext.control.ControlTable;
import kr.dogfoot.hwplib.object.bodytext.control.ControlType;
import kr.dogfoot.hwplib.object.bodytext.control.gso.ControlPicture;
import kr.dogfoot.hwplib.object.bodytext.control.table.Cell;
import kr.dogfoot.hwplib.object.bodytext.control.table.Row;
import kr.dogfoot.hwplib.object.bodytext.paragraph.Paragraph;
import kr.dogfoot.hwplib.object.docinfo.BinData;
import kr.dogfoot.hwplib.object.docinfo.borderfill.fillinfo.PictureInfo;
import kr.dogfoot.hwplib.reader.HWPReader;
import kr.dogfoot.hwplib.tool.objectfinder.ControlFinder;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
public class HwpReadService {

    public String processHwpFileWithImg(String filePath) {
        StringBuilder result = new StringBuilder();
        try {
            // HWP 파일 읽기
            HWPFile hwpFile = HWPReader.fromFile(filePath);
            if (hwpFile == null) {
                return "HWP 파일을 읽을 수 없습니다.";
            }

            // 섹션별로 순회
            int sectionIndex = 1;

            for (Section section : hwpFile.getBodyText().getSectionList()) {
                result.append("\n========== 섹션 ").append(sectionIndex).append(" ==========\n");

                // 섹션 내 텍스트 및 테이블 처리
                for (Paragraph paragraph : section.getParagraphs()) {
                    try {
                        // 텍스트 출력
                        String text = paragraph.getNormalString();
                        if (text != null && !text.isEmpty()) {
                            result.append(text).append("\n");
                        }

                        // 테이블 출력
                        if (paragraph.getControlList() != null) {
                            for (Control control : paragraph.getControlList()) {
                                if (control.getType() == ControlType.Table && control instanceof ControlTable) {
                                    ControlTable table = (ControlTable) control;
                                    for (Row row : table.getRowList()) {
                                        for (Cell cell : row.getCellList()) {
                                            result.append(cell.getParagraphList().getNormalString());
                                        }
                                        result.append("\n");
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        result.append("오류 발생: ").append(e.getMessage()).append("\n");
                    }
                }

                // 이미지 처리 (ControlFinder 사용)
                ArrayList<Control> controls = ControlFinder.find(hwpFile,
                        (control, paragraph, sectionObj) -> control instanceof ControlPicture);

                for (Control control : controls) {
                    if (control instanceof ControlPicture) {
                        ControlPicture picture = (ControlPicture) control;
                        String base64Image = extractImageFromPicture(picture, hwpFile);
                        if (base64Image != null) {
                            result.append("\n[이미지(Base64)]\n").append(base64Image).append("\n");
                        }
                    }
                }

                sectionIndex++;
            }
        } catch (Exception e) {
            result.append("오류 발생: ").append(e.getMessage());
        }
        return result.toString();
    }



    private String extractImageFromPicture(ControlPicture picture, HWPFile hwpFile) {
        try {
            // PictureInfo에서 BinItemID 가져오기
            PictureInfo pictureInfo = picture.getShapeComponentPicture().getPictureInfo();
            int binItemID = pictureInfo.getBinItemID();

            // BinData 추출
            List<BinData> binDataList = hwpFile.getDocInfo().getBinDataList();
            for (BinData binData : binDataList) {
                if (binData.getBinDataID() == binItemID) {
                    String linkPath = binData.getAbsolutePathForLink();
                    if (linkPath == null || linkPath.isEmpty()) {
                        linkPath = binData.getRelativePathForLink();
                    }
                    if (linkPath != null && !linkPath.isEmpty()) {
                        byte[] imageBytes = Files.readAllBytes(Paths.get(linkPath));
                        return Base64.getEncoder().encodeToString(imageBytes);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("이미지 추출 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }


    public String processHwpFile(String filePath) {
        StringBuilder result = new StringBuilder();
        try {
            HWPFile hwpFile = HWPReader.fromFile(filePath);
            if (hwpFile == null) {
                return "HWP 파일을 읽을 수 없습니다.";
            }

            // 섹션별로 순회
            int sectionIndex = 1;

            for (Section section : hwpFile.getBodyText().getSectionList()) {
                //result.append("\n========== 섹션 ").append(sectionIndex).append(" ==========\n");

                // 섹션 내 텍스트 읽기
                for (Paragraph paragraph : section.getParagraphs()) {
                    try {
                        String text = paragraph.getNormalString();
                        if (text != null && !text.isEmpty()) {
                            result.append(text).append("\n");
                        }

                        if (paragraph.getControlList() != null) {
                            for (Control control : paragraph.getControlList()) {
                                if (control.getType() == ControlType.Table && control instanceof ControlTable) {
                                    ControlTable table = (ControlTable) control;
                                    for (Row row : table.getRowList()) {
                                        for (Cell cell : row.getCellList()) {
                                            result.append(cell.getParagraphList().getNormalString());
                                            //.append(" | ");
                                        }
                                        result.append("\n");
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        result.append("오류 발생: ").append(e.getMessage()).append("\n");
                    }
                }

                sectionIndex++;
            }
        } catch (Exception e) {
            result.append("오류 발생: ").append(e.getMessage());
        }
        return result.toString();
    }

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