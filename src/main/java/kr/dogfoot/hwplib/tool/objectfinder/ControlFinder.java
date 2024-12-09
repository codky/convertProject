package kr.dogfoot.hwplib.tool.objectfinder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import kr.dogfoot.hwplib.object.HWPFile;
import kr.dogfoot.hwplib.object.bindata.EmbeddedBinaryData;
import kr.dogfoot.hwplib.object.bodytext.ParagraphListInterface;
import kr.dogfoot.hwplib.object.bodytext.Section;
import kr.dogfoot.hwplib.object.bodytext.control.Control;
import kr.dogfoot.hwplib.object.bodytext.control.ControlEndnote;
import kr.dogfoot.hwplib.object.bodytext.control.ControlFooter;
import kr.dogfoot.hwplib.object.bodytext.control.ControlFootnote;
import kr.dogfoot.hwplib.object.bodytext.control.ControlHeader;
import kr.dogfoot.hwplib.object.bodytext.control.ControlHiddenComment;
import kr.dogfoot.hwplib.object.bodytext.control.ControlTable;
import kr.dogfoot.hwplib.object.bodytext.control.ControlType;
import kr.dogfoot.hwplib.object.bodytext.control.gso.*;
import kr.dogfoot.hwplib.object.bodytext.control.gso.GsoControl;
import kr.dogfoot.hwplib.object.bodytext.control.table.Cell;
import kr.dogfoot.hwplib.object.bodytext.control.table.Row;
import kr.dogfoot.hwplib.object.bodytext.paragraph.Paragraph;
import kr.dogfoot.hwplib.object.docinfo.BinData;
import kr.dogfoot.hwplib.object.docinfo.borderfill.fillinfo.PictureInfo;

import static kr.dogfoot.hwplib.object.bodytext.control.gso.GsoControlType.*;

public class ControlFinder {
    private ControlFilter filter;

    private ArrayList<Control> resultList;

    private Section currentSection;

    private Paragraph currentParagraph;

    public static ArrayList<Control> find(HWPFile hwpFile, ControlFilter filter) {
        ControlFinder finder = new ControlFinder();
        return finder.go(hwpFile, filter);
    }

    private ArrayList<Control> go(HWPFile hwpFile, ControlFilter filter) {
        this.resultList = new ArrayList<>();
        this.filter = filter;
        for (Section s : hwpFile.getBodyText().getSectionList()) {
            this.currentSection = s;
            forParagraphList((ParagraphListInterface) s, hwpFile); // HWPFile 객체 전달
        }
        return this.resultList;
    }

    private void forParagraphList(ParagraphListInterface paraList, HWPFile hwpFile) {
        for (Paragraph p : paraList) {
            this.currentParagraph = p;
            forParagraph(p, hwpFile); // HWPFile 객체 전달
        }
    }

    private void forParagraph(Paragraph p, HWPFile hwpFile) {
        if (p.getControlList() == null)
            return;
        for (Control c : p.getControlList()) {
            if (this.filter.isMatched(c, this.currentParagraph, this.currentSection))
                this.resultList.add(c);
            forParagraphInControl(c, hwpFile); // HWPFile 객체 전달
        }
    }

    private void forParagraphInControl(Control c, HWPFile hwpFile) {
        if (c instanceof GsoControl) {
            GsoControl gsoControl = (GsoControl) c;
            switch (gsoControl.getGsoType()) { // GsoControlType 사용
                case Rectangle:
                    forRectangle((ControlRectangle) gsoControl, hwpFile);
                    break;
                case Ellipse:
                    forEllipse((ControlEllipse) gsoControl, hwpFile);
                    break;
                case Arc:
                    forArc((ControlArc) gsoControl, hwpFile);
                    break;
                case Curve:
                    forCurve((ControlCurve) gsoControl, hwpFile);
                    break;
                case Container:
                    forContainer((ControlContainer) gsoControl, hwpFile); // HWPFile 전달
                    break;
                default:
                    break;
            }
        } else {
            switch (c.getType()) { // ControlType 사용
                case Table:
                    forTable((ControlTable) c, hwpFile);
                    break;
                case Header:
                    forHeader((ControlHeader) c, hwpFile);
                    break;
                case Footer:
                    forFooter((ControlFooter) c, hwpFile);
                    break;
                case Footnote:
                    forFootnote((ControlFootnote) c, hwpFile);
                    break;
                case Endnote:
                    forEndnote((ControlEndnote) c, hwpFile);
                    break;
                case HiddenComment:
                    forHiddenComment((ControlHiddenComment) c, hwpFile);
                    break;
                default:
                    break;
            }
        }
    }



    private void forTable(ControlTable table, HWPFile hwpFile) {
        for (Row r : table.getRowList()) {
            for (Cell c : r.getCellList())
                forParagraphList((ParagraphListInterface)c.getParagraphList(), hwpFile);
        }
    }

    private void forHeader(ControlHeader header, HWPFile hwpFile) {
        forParagraphList((ParagraphListInterface)header.getParagraphList(), hwpFile);
    }

    private void forFooter(ControlFooter footer, HWPFile hwpFile) {
        forParagraphList((ParagraphListInterface)footer.getParagraphList(), hwpFile);
    }

    private void forFootnote(ControlFootnote footnote, HWPFile hwpFile) {
        forParagraphList((ParagraphListInterface)footnote.getParagraphList(), hwpFile);
    }

    private void forEndnote(ControlEndnote endnote, HWPFile hwpFile) {
        forParagraphList((ParagraphListInterface)endnote.getParagraphList(), hwpFile);
    }

    private void forHiddenComment(ControlHiddenComment hiddenComment, HWPFile hwpFile) {
        forParagraphList((ParagraphListInterface)hiddenComment.getParagraphList(), hwpFile);
    }

    private void forGso(GsoControl gc, HWPFile hwpFile) {
        switch (gc.getGsoType()) {
            case Picture:
                forPicture((ControlPicture) gc, hwpFile);
                break;
            case Rectangle:
                forRectangle((ControlRectangle)gc, hwpFile);
                break;
            case Ellipse:
                forEllipse((ControlEllipse)gc, hwpFile);
                break;
            case Arc:
                forArc((ControlArc)gc, hwpFile);
                break;
            case Polygon:
                forPolygon((ControlPolygon)gc, hwpFile);
                break;
            case Curve:
                forCurve((ControlCurve)gc, hwpFile);
                break;
            case Container:
                forContainer((ControlContainer)gc, hwpFile); // HWPFile 전달
                break;
        }
    }

    private void forPicture(ControlPicture picture, HWPFile hwpFile) {
        try {
            // PictureInfo에서 BinItemID 가져오기
            PictureInfo pictureInfo = picture.getShapeComponentPicture().getPictureInfo();
            int binItemID = pictureInfo.getBinItemID();

            // BinData 추출
            byte[] binData = extractBinData(hwpFile, binItemID);
            if (binData != null) {
                // Base64 인코딩
                String base64EncodedImage = Base64.getEncoder().encodeToString(binData);
                System.out.println("Base64 Encoded Image:");
                System.out.println(base64EncodedImage);
            } else {
                System.out.println("No binary data found for the picture.");
            }
        } catch (Exception e) {
            System.err.println("Error while processing picture control: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private byte[] extractBinData(HWPFile hwpFile, int binItemID) {
        try {
            // DocInfo에서 BinData 리스트를 가져옵니다.
            List<kr.dogfoot.hwplib.object.docinfo.BinData> binDataList = hwpFile.getDocInfo().getBinDataList();

            // BinData ID를 기반으로 데이터 검색
            for (kr.dogfoot.hwplib.object.docinfo.BinData binData : binDataList) {
                if (binData.getBinDataID() == binItemID) {
                    // 여기에 바이너리 데이터를 실제로 추출하는 로직을 작성해야 합니다.
                    // 예: binData.getAbsolutePathForLink() 또는 binData.getRelativePathForLink()를 통해 데이터를 로드
                    String linkPath = binData.getAbsolutePathForLink();
                    if (linkPath == null || linkPath.isEmpty()) {
                        linkPath = binData.getRelativePathForLink(); // 상대 경로로 대체
                    }
                    if (linkPath != null && !linkPath.isEmpty()) {
                        return loadBinaryDataFromPath(linkPath);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error while extracting binary data: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    private byte[] loadBinaryDataFromPath(String path) {
        try {
            // 파일에서 바이너리 데이터를 로드
            Path filePath = Paths.get(path);
            if (Files.exists(filePath) && Files.isRegularFile(filePath)) {
                return Files.readAllBytes(filePath);
            } else {
                System.err.println("File not found or invalid path: " + path);
                return null;
            }
        } catch (IOException e) {
            System.err.println("Error while reading binary data from path: " + e.getMessage());
            return null;
        }
    }

    private void forRectangle(ControlRectangle rectangle, HWPFile hwpFile) {
        if (rectangle.getTextBox() != null)
            forParagraphList((ParagraphListInterface) rectangle.getTextBox().getParagraphList(), hwpFile);
    }

    private void forEllipse(ControlEllipse ellipse, HWPFile hwpFile) {
        if (ellipse.getTextBox() == null)
            return;
        forParagraphList((ParagraphListInterface) ellipse.getTextBox().getParagraphList(), hwpFile);
    }

    private void forArc(ControlArc arc, HWPFile hwpFile) {
        if (arc.getTextBox() == null)
            return;
        forParagraphList((ParagraphListInterface)arc.getTextBox().getParagraphList(), hwpFile);
    }

    private void forPolygon(ControlPolygon polygon, HWPFile hwpFile) {
        if (polygon.getTextBox() == null)
            return;
        forParagraphList((ParagraphListInterface)polygon.getTextBox().getParagraphList(), hwpFile);
    }

    private void forCurve(ControlCurve curve, HWPFile hwpFile) {
        if (curve.getTextBox() == null)
            return;
        forParagraphList((ParagraphListInterface)curve.getTextBox().getParagraphList(), hwpFile);
    }

    private void forContainer(ControlContainer container, HWPFile hwpFile) {
        for (GsoControl child : container.getChildControlList()) {
            if (this.filter != null && this.filter.isMatched((Control)child, this.currentParagraph, this.currentSection))
                this.resultList.add(child);
            forGso(child, hwpFile); // HWPFile 전달
        }
    }
}
