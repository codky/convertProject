package com.example.convertbase64.service;

import kr.dogfoot.hwplib.object.HWPFile;
import kr.dogfoot.hwplib.object.bindata.BinData;
import kr.dogfoot.hwplib.object.bindata.EmbeddedBinaryData;
import kr.dogfoot.hwplib.object.bodytext.Section;
import kr.dogfoot.hwplib.object.bodytext.control.Control;
import kr.dogfoot.hwplib.object.bodytext.control.ControlTable;
import kr.dogfoot.hwplib.object.bodytext.control.ControlType;
import kr.dogfoot.hwplib.object.bodytext.control.gso.ControlPicture;
import kr.dogfoot.hwplib.object.bodytext.control.gso.GsoControl;
import kr.dogfoot.hwplib.object.bodytext.control.gso.GsoControlType;
import kr.dogfoot.hwplib.object.bodytext.control.table.Cell;
import kr.dogfoot.hwplib.object.bodytext.control.table.Row;
import kr.dogfoot.hwplib.object.bodytext.paragraph.Paragraph;
import kr.dogfoot.hwplib.object.docinfo.borderfill.fillinfo.PictureInfo;
import kr.dogfoot.hwplib.reader.HWPReader;
import kr.dogfoot.hwplib.tool.objectfinder.ControlFinder;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Base64;

@Service
public class HwpReadService {

    public String processHwpFileWithImg2(String filePath) {
        StringBuilder result = new StringBuilder();
        try {
            // HWP 파일 읽기
            HWPFile hwpFile = HWPReader.fromFile(filePath);
            if (hwpFile == null) {
                return "HWP 파일을 읽을 수 없습니다.";
            }

            // 출력 폴더 생성
            String outputFolderPath = "C:\\Users\\MEDIAZEN\\Desktop\\변환폴더(수요일까지)\\images";
            File outputFolder = new File(outputFolderPath);
            if (!outputFolder.exists()) {
                outputFolder.mkdirs();
            }

            // HWP 파일 순회
            int sectionIndex = 1;
            int imageCounter = 1;
            for (Section section : hwpFile.getBodyText().getSectionList()) {
                result.append("\n========== 섹션 ").append(sectionIndex).append(" ==========\n");

                for (Paragraph paragraph : section.getParagraphs()) {
                    // 텍스트 처리
                    String text = paragraph.getNormalString();
                    if (text != null && !text.isEmpty()) {
                        result.append(text).append("\n");
                    }

                    // 컨트롤 처리
                    if (paragraph.getControlList() != null) {
                        for (Control control : paragraph.getControlList()) {
                            if (control.getType() == ControlType.Gso) {
                                if (control instanceof GsoControl) {
                                    GsoControl gsoControl = (GsoControl) control;
                                    if (gsoControl.getGsoType() == GsoControlType.Picture) {
                                        processImage((ControlPicture) gsoControl, result, hwpFile, outputFolder, imageCounter);
                                        imageCounter++;
                                    }
                                }
                            } else if (control.getType() == ControlType.Table) {
                                processTable((ControlTable) control, result, hwpFile, outputFolder, imageCounter);
                            } else {
                                result.append("기타 컨트롤: ").append(control.getType()).append("\n");
                            }
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

    private void processTable(ControlTable table, StringBuilder result, HWPFile hwpFile, File outputFolder, int imageCounter) throws UnsupportedEncodingException {
        for (Row row : table.getRowList()) {
            for (Cell cell : row.getCellList()) {
                // 셀 텍스트 처리
                String cellText = cell.getParagraphList().getNormalString();
                if (cellText != null && !cellText.isEmpty()) {
                    result.append(cellText);
                }

                // 셀 내 이미지 탐지
                for (Paragraph paragraph : cell.getParagraphList()) {
                    if (paragraph.getControlList() != null) {
                        for (Control control : paragraph.getControlList()) {
                            if (control instanceof GsoControl) {
                                GsoControl gsoControl = (GsoControl) control;
                                if (gsoControl.getGsoType() == GsoControlType.Picture) {
                                    processImage((ControlPicture) gsoControl, result, hwpFile, outputFolder, imageCounter);
                                    imageCounter++;
                                }
                            }
                        }
                    }
                }
            }
            result.append("\n");
        }
    }

    private void processImage(ControlPicture picture, StringBuilder result, HWPFile hwpFile, File outputFolder, int imageCounter) {
        try {
            PictureInfo pictureInfo = picture.getShapeComponentPicture().getPictureInfo();
            int binItemID = pictureInfo.getBinItemID();

            // BinData 추출
            EmbeddedBinaryData embeddedBinaryData = extractBinDataFromHWP(binItemID, result, hwpFile, imageCounter);
            if (embeddedBinaryData != null) {
                // 이미지 파일 저장 경로 생성
                String outputPath = outputFolder.getPath() + "\\" + embeddedBinaryData.getName();


                // 파일 확장자 추가 (필요한 경우)
                if (!outputPath.toLowerCase().endsWith(".bmp")) {
                    outputPath += ".bmp";
                }
                // 파일 저장
                try (FileOutputStream fos = new FileOutputStream(outputPath)) {
                    fos.write(embeddedBinaryData.getData());
                    System.out.println("이미지가 저장되었습니다: " + outputPath);
                    result.append("\n[이미지 위치]").append("\n");
                    //result.append("\n[이미지 저장 완료]: ").append(outputPath).append("\n");
                    //result.append("\n 이미지명: ").append("BIN").append(String.format("%04d", imageCounter)).append(".bmp").append("\n");
                }
            } else {
                result.append("\n[이미지 없음]\n");
            }
        } catch (Exception e) {
            result.append("\n[이미지 처리 오류]: ").append(e.getMessage()).append("\n");
        }
    }

    private EmbeddedBinaryData extractBinDataFromHWP(int binItemID, StringBuilder result, HWPFile hwpFile, int imageCounter) {
        try {
            ArrayList<kr.dogfoot.hwplib.object.docinfo.BinData> binDataList = hwpFile.getDocInfo().getBinDataList();
            for (kr.dogfoot.hwplib.object.docinfo.BinData binData : binDataList) {
                if (binData.getBinDataID() == binItemID) {
                    EmbeddedBinaryData embeddedBinaryData = hwpFile.getBinData().getEmbeddedBinaryDataList().get(binData.getBinDataID() - 1);
                    //result.append("\n 이미지명: ").append("BIN").append(String.format("%04d", imageCounter)).append(".bmp").append("\n");
                    return embeddedBinaryData;
                }
            }
        } catch (Exception e) {
            System.err.println("BinData 추출 오류: " + e.getMessage());
        }
        return null;
    }




    public String processHwpFileWithImg(String filePath) {
        StringBuilder result = new StringBuilder();
        try {
            // HWP 파일 읽기
            HWPFile hwpFile = HWPReader.fromFile(filePath);
            if (hwpFile == null) {
                return "HWP 파일을 읽을 수 없습니다.";
            }

            // HWP 파일 이름 가져오기
            File hwpFileObj = new File(filePath);
            String hwpFileName = hwpFileObj.getName().replaceFirst("\\.hwp$", "");

            // 출력 폴더 생성
            String outputFolderPath = "C:\\Users\\MEDIAZEN\\Desktop\\변환폴더(수요일까지)\\" + hwpFileName;
            File outputFolder = new File(outputFolderPath);
            if (!outputFolder.exists()) {
                outputFolder.mkdirs();
            }

            // txt 폴더와 이미지 폴더 생성
            File txtFolder = new File(outputFolderPath + "\\txt");
            File imageFolder = new File(outputFolderPath + "\\images");
            if (!txtFolder.exists()) {
                txtFolder.mkdirs();
            }
            if (!imageFolder.exists()) {
                imageFolder.mkdirs();
            }
            // BinData 처리
            BinData binData = hwpFile.getBinData();
            ArrayList<EmbeddedBinaryData> list = binData.getEmbeddedBinaryDataList();
            if (!list.isEmpty()) {
                System.out.println("list size = " + list.size());
                int imageIndex = 1; // 이미지 번호
                for (EmbeddedBinaryData embeddedBinaryData : list) {
                    byte[] data = embeddedBinaryData.getData();
                    if (data != null) {
                        try {
                            // 확장자 확인
                            String extension = embeddedBinaryData.getName().toLowerCase();
                            if (extension.endsWith(".bmp")) {
                                // BMP 데이터를 처리
                                ByteArrayInputStream bais = new ByteArrayInputStream(data);
                                BufferedImage bufferedImage = ImageIO.read(bais);
                                if (bufferedImage != null) {
                                    // JPEG로 변환하기 위한 OutputStream 생성
                                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                    ImageIO.write(bufferedImage, "jpeg", baos);
                                    data = baos.toByteArray();
                                    extension = "jpeg";
                                }
                            }

                            // Base64로 인코딩
                            String base64Data = Base64.getEncoder().encodeToString(data);

                            // 텍스트 파일로 저장
                            String textFileName = txtFolder.getPath() + "\\" + hwpFileName + "_이미지_" + imageIndex + ".txt";
                            try (FileOutputStream fos = new FileOutputStream(textFileName)) {
                                fos.write(base64Data.getBytes());
                                System.out.println("Base64 데이터가 텍스트 파일로 저장되었습니다: " + textFileName);
                            }

                            // 이미지 파일로 저장
                            String imageFileName = imageFolder.getPath() + "\\" + hwpFileName + "_이미지_" + imageIndex + "." + extension;
                            try (FileOutputStream fos = new FileOutputStream(imageFileName)) {
                                fos.write(data);
                                System.out.println("이미지가 저장되었습니다: " + imageFileName);
                            }

                            result.append("\n 이미지명: ").append(imageIndex).append(" ").append(embeddedBinaryData.getName()).append(" ")
                                    .append("\n저장 경로: ").append(imageFileName).append("\n");

                            imageIndex++;
                        } catch (IOException e) {
                            System.out.println("이미지 처리 중 오류 발생 for image " + imageIndex + ": " + e.getMessage());
                            result.append("\n========== 이미지 ").append(imageIndex).append(": 처리 중 오류 발생 ==========").append("\n").append(e.getMessage()).append("\n");
                            imageIndex++;
                        }
                    } else {
                        System.out.println("Embedded binary data is null for image " + imageIndex);
                        result.append("\n========== 이미지 ").append(imageIndex).append(": Embedded Binary Data is null ==========").append("\n");
                        imageIndex++;
                    }
                }
            } else {
                System.out.println("No embedded binary data found.");
                result.append("\n========== No Embedded Binary Data Found ==========").append("\n");
            }

            // 섹션별로 순회
            int sectionIndex = 1;

            for (Section section : hwpFile.getBodyText().getSectionList()) {
                result.append("\n========== 섹션 ").append(sectionIndex).append(" ==========" + "\n");

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
                                            int borderFillId = cell.getListHeader().getBorderFillId();
//                                            BorderFill borderFill = hwpFile.getDocInfo().getBorderFillList().get(borderFillId - 1);
//                                            borderFill.getFillInfo().getImageFill().getPictureInfo().getBinItemID();
                                            ArrayList<kr.dogfoot.hwplib.object.docinfo.BinData> binDataList = hwpFile.getDocInfo().getBinDataList();
                                            EmbeddedBinaryData embeddedBinaryData = hwpFile.getBinData().getEmbeddedBinaryDataList().get(binDataList.get(0).getBinDataID());
                                            byte[] data = embeddedBinaryData.getData();
                                            if (data != null) {
                                                try {
                                                    // 확장자 확인
                                                    String extension = embeddedBinaryData.getName().toLowerCase();
                                                    if (extension.endsWith(".bmp")) {
                                                        // BMP 데이터를 처리
                                                        ByteArrayInputStream bais = new ByteArrayInputStream(data);
                                                        BufferedImage bufferedImage = ImageIO.read(bais);
                                                        if (bufferedImage != null) {
                                                            // JPEG로 변환하기 위한 OutputStream 생성
                                                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                                            ImageIO.write(bufferedImage, "jpeg", baos);
                                                            data = baos.toByteArray();
                                                            extension = "jpeg";
                                                        }
                                                    }

                                                    // Base64로 인코딩
                                                    String base64Data = Base64.getEncoder().encodeToString(data);

                                                    // 텍스트 파일로 저장
                                                    String textFileName = txtFolder.getPath() + "\\" + hwpFileName + "_이미지_" + ".txt";
                                                    try (FileOutputStream fos = new FileOutputStream(textFileName)) {
                                                        fos.write(base64Data.getBytes());
                                                        System.out.println("Base64 데이터가 텍스트 파일로 저장되었습니다: " + textFileName);
                                                    }

                                                    // 이미지 파일로 저장
                                                    String imageFileName = imageFolder.getPath() + "\\" + hwpFileName + "_이미지_" + "." + extension;
                                                    try (FileOutputStream fos = new FileOutputStream(imageFileName)) {
                                                        fos.write(data);
                                                        System.out.println("이미지가 저장되었습니다: " + imageFileName);
                                                    }

                                                    result.append("\n 이미지명: ").append(" ").append(embeddedBinaryData.getName()).append(" ")
                                                            .append("\n저장 경로: ").append(imageFileName).append("\n");


                                                } catch (IOException e) {
                                                    System.out.println("이미지 처리 중 오류 발생 for image " + ": " + e.getMessage());
                                                    result.append("\n========== 이미지 ").append(": 처리 중 오류 발생 ==========").append("\n").append(e.getMessage()).append("\n");

                                                }
                                            } else {
                                                System.out.println("Embedded binary data is null for image ");
                                                result.append("\n========== 이미지 ").append(": Embedded Binary Data is null ==========").append("\n");
                                            }

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