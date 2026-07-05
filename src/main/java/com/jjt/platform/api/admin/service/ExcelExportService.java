package com.jjt.platform.api.admin.service;

import com.jjt.platform.core.domain.entity.SponsorshipStatus;
import com.jjt.platform.infrastructure.persistence.entity.ChildEntity;
import com.jjt.platform.infrastructure.persistence.entity.DonationEntity;
import com.jjt.platform.infrastructure.persistence.entity.DonorEntity;
import com.jjt.platform.infrastructure.persistence.entity.SponsorPaymentEntity;
import com.jjt.platform.infrastructure.persistence.entity.SponsorshipEntity;
import com.jjt.platform.infrastructure.persistence.repository.ChildJpaRepository;
import com.jjt.platform.infrastructure.persistence.repository.DonationJpaRepository;
import com.jjt.platform.infrastructure.persistence.repository.DonorJpaRepository;
import com.jjt.platform.infrastructure.persistence.repository.SponsorPaymentJpaRepository;
import com.jjt.platform.infrastructure.persistence.repository.SponsorshipJpaRepository;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ExcelExportService {

    private final ChildJpaRepository childRepo;
    private final SponsorshipJpaRepository sponsorshipRepo;
    private final DonationJpaRepository donationRepo;
    private final DonorJpaRepository donorRepo;
    private final SponsorPaymentJpaRepository paymentRepo;

    public ExcelExportService(ChildJpaRepository childRepo,
                               SponsorshipJpaRepository sponsorshipRepo,
                               DonationJpaRepository donationRepo,
                               DonorJpaRepository donorRepo,
                               SponsorPaymentJpaRepository paymentRepo) {
        this.childRepo = childRepo;
        this.sponsorshipRepo = sponsorshipRepo;
        this.donationRepo = donationRepo;
        this.donorRepo = donorRepo;
        this.paymentRepo = paymentRepo;
    }

    @Transactional(readOnly = true)
    public byte[] exportChildren(UUID orgId) throws IOException {
        List<ChildEntity> children = orgId != null
                ? childRepo.findByOrganisationId(orgId)
                : childRepo.findAll();

        List<SponsorshipEntity> sponsorships = orgId != null
                ? sponsorshipRepo.findByOrganisationId(orgId)
                : sponsorshipRepo.findAll();

        Map<UUID, SponsorshipEntity> activeByChild = new HashMap<>();
        Map<UUID, SponsorshipEntity> pendingByChild = new HashMap<>();
        for (SponsorshipEntity s : sponsorships) {
            if (s.getStatus() == SponsorshipStatus.ACTIVE) {
                activeByChild.putIfAbsent(s.getChildId(), s);
            } else if (s.getStatus() == SponsorshipStatus.PENDING) {
                pendingByChild.putIfAbsent(s.getChildId(), s);
            }
        }

        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XSSFSheet sheet = wb.createSheet("Children");
            XSSFCellStyle headerStyle = createHeaderStyle(wb);

            String[] headers = {"Roll Number", "Full Name", "City", "Campus", "School",
                    "Education Amount", "Currency", "Status", "Current Sponsor"};
            writeHeaderRow(sheet, headerStyle, headers);

            int rowNum = 1;
            for (ChildEntity child : children) {
                SponsorshipEntity active = activeByChild.get(child.getId());
                SponsorshipEntity pending = active == null ? pendingByChild.get(child.getId()) : null;
                SponsorshipEntity current = active != null ? active : pending;
                String status = active != null ? "ALLOCATED" : pending != null ? "RESERVED" : "AVAILABLE";
                String sponsorName = current != null && current.getSponsor() != null
                        ? current.getSponsor().getDisplayName() : "";

                XSSFRow row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(child.getRollNumber());
                row.createCell(1).setCellValue(child.getFullName());
                row.createCell(2).setCellValue(child.getCity());
                row.createCell(3).setCellValue(child.getCampusName());
                row.createCell(4).setCellValue(child.getSchoolName() != null ? child.getSchoolName() : "");
                row.createCell(5).setCellValue(child.getEducationAmount().doubleValue());
                row.createCell(6).setCellValue(child.getEducationCurrency());
                row.createCell(7).setCellValue(status);
                row.createCell(8).setCellValue(sponsorName);
            }

            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);
            wb.write(out);
            return out.toByteArray();
        }
    }

    @Transactional(readOnly = true)
    public byte[] exportDonations(UUID orgId) throws IOException {
        List<DonationEntity> donations = orgId != null
                ? donationRepo.findByOrganisationId(orgId, Pageable.unpaged()).getContent()
                : donationRepo.findAll();

        Map<UUID, DonorEntity> donorMap = new HashMap<>();
        List<DonorEntity> donors = orgId != null
                ? donorRepo.findByOrganisationIdOrderByDisplayNameAsc(orgId)
                : donorRepo.findAll();
        for (DonorEntity d : donors) donorMap.put(d.getId(), d);

        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XSSFSheet sheet = wb.createSheet("Donations");
            XSSFCellStyle headerStyle = createHeaderStyle(wb);

            String[] headers = {"Date", "Donor Name", "Type", "Amount", "Currency",
                    "Receipt No", "Status", "Notes"};
            writeHeaderRow(sheet, headerStyle, headers);

            int rowNum = 1;
            for (DonationEntity d : donations) {
                DonorEntity donor = d.getDonorId() != null ? donorMap.get(d.getDonorId()) : null;
                String donorName = donor != null ? donor.getDisplayName() : "";

                XSSFRow row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(d.getDonationDate() != null ? d.getDonationDate().toString() : "");
                row.createCell(1).setCellValue(donorName);
                row.createCell(2).setCellValue(d.getDonationType() != null ? d.getDonationType().name() : "");
                row.createCell(3).setCellValue(d.getAmount() != null ? d.getAmount().doubleValue() : 0);
                row.createCell(4).setCellValue(d.getCurrency() != null ? d.getCurrency() : "");
                row.createCell(5).setCellValue(d.getReceiptNumber() != null ? d.getReceiptNumber() : "");
                row.createCell(6).setCellValue(d.getStatus() != null ? d.getStatus().name() : "");
                row.createCell(7).setCellValue(d.getNotes() != null ? d.getNotes() : "");
            }

            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);
            wb.write(out);
            return out.toByteArray();
        }
    }

    @Transactional(readOnly = true)
    public byte[] exportReconciliation(UUID orgId, int year, int month) throws IOException {
        YearMonth ym = YearMonth.of(year, month);
        String monthStr = ym.format(DateTimeFormatter.ofPattern("yyyy-MM"));

        List<SponsorPaymentEntity> payments = orgId != null
                ? paymentRepo.findByOrganisationIdAndPaymentMonth(orgId, monthStr)
                : paymentRepo.findByPaymentMonth(monthStr);

        List<SponsorshipEntity> sponsorships = orgId != null
                ? sponsorshipRepo.findByOrganisationId(orgId)
                : sponsorshipRepo.findAll();

        Map<UUID, SponsorshipEntity> sponsorshipMap = new HashMap<>();
        for (SponsorshipEntity s : sponsorships) sponsorshipMap.put(s.getId(), s);

        List<ChildEntity> allChildren = orgId != null
                ? childRepo.findByOrganisationId(orgId)
                : childRepo.findAll();
        Map<UUID, ChildEntity> childMap = new HashMap<>();
        for (ChildEntity c : allChildren) childMap.put(c.getId(), c);

        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XSSFSheet sheet = wb.createSheet("Reconciliation " + monthStr);
            XSSFCellStyle headerStyle = createHeaderStyle(wb);

            String[] headers = {"Month", "Child Name", "Roll Number", "Sponsor Name",
                    "Expected Amount", "Currency", "Status", "Received Amount", "Bank Reference", "Received Date"};
            writeHeaderRow(sheet, headerStyle, headers);

            int rowNum = 1;
            for (SponsorPaymentEntity p : payments) {
                SponsorshipEntity sp = sponsorshipMap.get(p.getSponsorshipId());
                ChildEntity child = childMap.get(p.getChildId());
                String childName = child != null ? child.getFullName() : "";
                String rollNo = child != null ? child.getRollNumber() : "";
                String sponsorName = sp != null && sp.getSponsor() != null
                        ? sp.getSponsor().getDisplayName() : "";

                XSSFRow row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(p.getPaymentMonth());
                row.createCell(1).setCellValue(childName);
                row.createCell(2).setCellValue(rollNo);
                row.createCell(3).setCellValue(sponsorName);
                row.createCell(4).setCellValue(p.getExpectedAmount().doubleValue());
                row.createCell(5).setCellValue(p.getExpectedCurrency());
                row.createCell(6).setCellValue(p.getStatus().name());
                row.createCell(7).setCellValue(p.getReceivedAmount() != null ? p.getReceivedAmount().doubleValue() : 0);
                row.createCell(8).setCellValue(p.getBankReference() != null ? p.getBankReference() : "");
                row.createCell(9).setCellValue(p.getReceivedDate() != null ? p.getReceivedDate().toString() : "");
            }

            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);
            wb.write(out);
            return out.toByteArray();
        }
    }

    private XSSFCellStyle createHeaderStyle(XSSFWorkbook wb) {
        XSSFCellStyle style = wb.createCellStyle();
        XSSFFont font = wb.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_GREEN.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        return style;
    }

    private void writeHeaderRow(XSSFSheet sheet, XSSFCellStyle style, String[] headers) {
        XSSFRow row = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            var cell = row.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(style);
        }
    }
}
