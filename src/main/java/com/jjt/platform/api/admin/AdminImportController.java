package com.jjt.platform.api.admin;

import com.jjt.platform.api.admin.dto.ImportChildrenResponse;
import com.jjt.platform.api.admin.service.BulkChildImportService;
import com.jjt.platform.config.security.JwtUserDetails;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@RestController
@RequestMapping("/api/admin")
public class AdminImportController {

    private static final String[] TEMPLATE_HEADERS = {
            "Roll Number", "Full Name *", "City *", "Campus Name *",
            "School Name", "Education Amount", "Education Currency"
    };

    private final BulkChildImportService importService;

    public AdminImportController(BulkChildImportService importService) {
        this.importService = importService;
    }

    @PostMapping("/children/import")
    public ResponseEntity<ImportChildrenResponse> importChildren(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal JwtUserDetails principal) throws IOException {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        ImportChildrenResponse result = importService.importFromExcel(
                file.getInputStream(), principal.getOrgId(), principal.getId());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/children/import/template")
    public ResponseEntity<byte[]> downloadTemplate() throws IOException {
        byte[] bytes = buildTemplate();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"children-import-template.xlsx\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }

    private byte[] buildTemplate() throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XSSFSheet sheet = wb.createSheet("Children Import");

            XSSFCellStyle headerStyle = wb.createCellStyle();
            XSSFFont headerFont = wb.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_GREEN.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);

            XSSFCellStyle exampleStyle = wb.createCellStyle();
            XSSFFont exampleFont = wb.createFont();
            exampleFont.setItalic(true);
            exampleFont.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
            exampleStyle.setFont(exampleFont);

            XSSFRow headerRow = sheet.createRow(0);
            for (int i = 0; i < TEMPLATE_HEADERS.length; i++) {
                var cell = headerRow.createCell(i);
                cell.setCellValue(TEMPLATE_HEADERS[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 5000);
            }

            XSSFRow exampleRow = sheet.createRow(1);
            String[] example = {"JJT-2024-001", "Ahmed Ali", "Karachi", "Gulshan Campus", "ABC School", "5000", "PKR"};
            for (int i = 0; i < example.length; i++) {
                var cell = exampleRow.createCell(i);
                cell.setCellValue(example[i]);
                cell.setCellStyle(exampleStyle);
            }

            wb.write(out);
            return out.toByteArray();
        }
    }
}
