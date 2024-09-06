package com.yfckevin.bingBao.utils;

import com.yfckevin.bingBao.dto.InventoryDTO;
import com.yfckevin.bingBao.dto.MemberDTO;
import com.yfckevin.bingBao.enums.PackageForm;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {

    public static Path saveUploadedFile(MultipartFile file, String uploadPath) throws IOException {

        Path path = Paths.get(uploadPath);

        InputStream fileStream = file.getInputStream();
        Files.copy(fileStream, path);
        fileStream.close();

        return path;
    }

    public static void delete(Path path) throws IOException{
        if(Files.exists(path)){  //檢查檔案還在不在
            Files.delete(path);
        }
    }


    public static void constructExcel(List<InventoryDTO> inventoryDTOList, String type, HttpServletResponse response, MemberDTO memberDTO) throws Exception {
        System.out.println("exportExcel.......");

        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             XSSFWorkbook workbook = new XSSFWorkbook()) {

            // Creating a blank Excel sheet
            XSSFSheet sheet = workbook.createSheet(type);

            // 建一個儲存格的Style，設定不換行
            CellStyle style = workbook.createCellStyle();
            style.setWrapText(false);

            // 創建字體大小和粗體的樣式
            Font boldFont = workbook.createFont();
            boldFont.setBold(true);  // 設置粗體

            Font titleFont = workbook.createFont();
            titleFont.setBold(true);  // 設置粗體
            titleFont.setFontHeightInPoints((short) 16);  // 設置字體大小為16

            // 設定置中樣式，並應用字體
            CellStyle centerStyle = workbook.createCellStyle();
            centerStyle.setAlignment(HorizontalAlignment.CENTER);
            centerStyle.setFont(titleFont);  // 套用標題字體

            // 設定置右樣式
            CellStyle rightAlignStyle = workbook.createCellStyle();
            rightAlignStyle.setAlignment(HorizontalAlignment.RIGHT);

            // 設定標題行的樣式（粗體）
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(boldFont);  // 套用粗體字

            // 第一列: type
            XSSFRow rowType = sheet.createRow(0);
            rowType.setHeightInPoints(25);  // 調整列高
            Cell cellType = rowType.createCell(0);
            cellType.setCellValue(type);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 9));
            cellType.setCellStyle(centerStyle);

            // 第二列: 輸出者
            XSSFRow rowExporter = sheet.createRow(1);
            rowExporter.setHeightInPoints(20);  // 調整列高
            Cell cellExporter = rowExporter.createCell(0);
            String exporter = (memberDTO != null && memberDTO.getName() != null) ? memberDTO.getName() : "NA";
            cellExporter.setCellValue("輸出者: " + exporter);
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 9));
            cellExporter.setCellStyle(rightAlignStyle);

            // 第三列: 輸出日期
            XSSFRow rowDate = sheet.createRow(2);
            rowDate.setHeightInPoints(20);  // 調整列高
            Cell cellDate = rowDate.createCell(0);
            String currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            cellDate.setCellValue("繪製日期: " + currentDate);
            sheet.addMergedRegion(new CellRangeAddress(2, 2, 0, 9));
            cellDate.setCellStyle(rightAlignStyle);

            XSSFRow rowSpace = sheet.createRow(3);
            Cell cellSpace = rowSpace.createCell(0);
            cellSpace.setCellValue("");
            sheet.addMergedRegion(new CellRangeAddress(3, 3, 0, 9));

            //這段設定Excel的Title
            List<String> CELL_HEADS = new ArrayList<>();
            CELL_HEADS.add("食材名稱");
            CELL_HEADS.add("剩餘數量");
            CELL_HEADS.add("有效期限");
            CELL_HEADS.add("再過多久過期");
            CELL_HEADS.add("已存放多久");
            CELL_HEADS.add("存放位置");
            CELL_HEADS.add("食材主分類");
            CELL_HEADS.add("食材副分類");
            CELL_HEADS.add("包裝類型");
            CELL_HEADS.add("包裝方式");

            XSSFRow head = sheet.createRow(4);
            head.setHeightInPoints(20);  // 調整行高
            for (int i = 0; i < CELL_HEADS.size(); i++) {
                Cell cell = head.createCell(i);
                cell.setCellValue(CELL_HEADS.get(i));
                cell.setCellStyle(headerStyle);
            }

            //從第4行開始
            int rownum = 5;
            for (InventoryDTO dto : inventoryDTOList) {
                int cellnum = 0;
                Cell cell;
                XSSFRow row = sheet.createRow(rownum++);

                row.setHeightInPoints(30);

                cell = row.createCell(cellnum++);
                cell.setCellValue(dto.getName());

                cell = row.createCell(cellnum++);
                cell.setCellValue(dto.getTotalAmount());
                cell.setCellStyle(style);

                cell = row.createCell(cellnum++);
                cell.setCellValue(dto.getExpiryDate());
                cell.setCellStyle(style);

                cell = row.createCell(cellnum++);
                cell.setCellValue(dto.getExpiryTime());
                cell.setCellStyle(style);

                cell = row.createCell(cellnum++);
                cell.setCellValue(dto.getExistedTime());
                cell.setCellStyle(style);

                cell = row.createCell(cellnum++);
                cell.setCellValue(dto.getStorePlaceLabel());
                cell.setCellStyle(style);

                cell = row.createCell(cellnum++);
                cell.setCellValue(dto.getMainCategoryLabel());
                cell.setCellStyle(style);

                cell = row.createCell(cellnum++);
                cell.setCellValue(dto.getSubCategory() == null ? "-" : dto.getSubCategoryLabel());
                cell.setCellStyle(style);

                cell = row.createCell(cellnum++);
                cell.setCellValue(dto.getPackageFormLabel());
                cell.setCellStyle(style);

                cell = row.createCell(cellnum++);
                if (PackageForm.COMPLETE.equals(dto.getPackageForm())) {
                    cell.setCellValue("每" + dto.getPackageUnitLabel() + "有" + dto.getPackageQuantity() + "個");
                } else {
                    cell.setCellValue("-");
                }
                cell.setCellStyle(style);
            }

            // 自動調整列高和欄寬
            for (int i = 0; i < CELL_HEADS.size(); i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(bos);

            filehandle(response, bos, "inventory-list.xlsx");
        }
    }


    //處理檔案強制下載
    private static void filehandle(HttpServletResponse response, ByteArrayOutputStream baos, String fileName) throws Exception {
        // forces download
        String headerKey = "Content-Disposition";
        String headerValue = String.format("attachment; filename=\"%s\"", fileName);
        response.setHeader(headerKey, headerValue);

        // setting the content type
        String mimeType = fileName.endsWith(".xlsx") ? "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" : "application/vnd.ms-excel";
        response.setContentType(mimeType);
        // the contentlength
        response.setContentLength(baos.size());
        // write ByteArrayOutputStream to the ServletOutputStream
        try (OutputStream os = response.getOutputStream()) {
            baos.writeTo(os);
            os.flush(); // os.close() 不需要，因為try-with-resources會自動處理
        }

    }
}
