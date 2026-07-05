package com.jjt.platform.api.admin;

import com.jjt.platform.api.admin.service.ChildReportPdfService;
import com.jjt.platform.api.admin.service.ExcelExportService;
import com.jjt.platform.config.security.JwtUserDetails;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/export")
@PreAuthorize("hasAnyRole('JJT_ADMIN', 'ORG_ADMIN')")
public class AdminExportController {

    private static final MediaType XLSX_TYPE =
            MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private final ExcelExportService excelService;
    private final ChildReportPdfService pdfService;

    public AdminExportController(ExcelExportService excelService, ChildReportPdfService pdfService) {
        this.excelService = excelService;
        this.pdfService = pdfService;
    }

    @GetMapping("/children.xlsx")
    public ResponseEntity<byte[]> exportChildren(@AuthenticationPrincipal JwtUserDetails principal) throws IOException {
        byte[] bytes = excelService.exportChildren(principal.getOrgId());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"children.xlsx\"")
                .contentType(XLSX_TYPE)
                .body(bytes);
    }

    @GetMapping("/donations.xlsx")
    public ResponseEntity<byte[]> exportDonations(@AuthenticationPrincipal JwtUserDetails principal) throws IOException {
        byte[] bytes = excelService.exportDonations(principal.getOrgId());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"donations.xlsx\"")
                .contentType(XLSX_TYPE)
                .body(bytes);
    }

    @GetMapping("/reconciliation/{year}/{month}.xlsx")
    public ResponseEntity<byte[]> exportReconciliation(
            @PathVariable("year") int year,
            @PathVariable("month") int month,
            @AuthenticationPrincipal JwtUserDetails principal) throws IOException {
        byte[] bytes = excelService.exportReconciliation(principal.getOrgId(), year, month);
        String filename = String.format("reconciliation-%d-%02d.xlsx", year, month);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(XLSX_TYPE)
                .body(bytes);
    }

    @GetMapping("/children/{id}/report.pdf")
    public ResponseEntity<byte[]> exportChildReport(
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal JwtUserDetails principal) throws Exception {
        byte[] bytes = pdfService.generateReport(id, principal.getOrgId());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"student-report.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(bytes);
    }
}
