package com.jjt.platform.api.admin.service;

import com.jjt.platform.core.domain.entity.SponsorshipStatus;
import com.jjt.platform.infrastructure.persistence.entity.ChildEntity;
import com.jjt.platform.infrastructure.persistence.entity.LedgerEntryEntity;
import com.jjt.platform.infrastructure.persistence.entity.ProgressUpdateEntity;
import com.jjt.platform.infrastructure.persistence.entity.SponsorshipEntity;
import com.jjt.platform.infrastructure.persistence.repository.ChildJpaRepository;
import com.jjt.platform.infrastructure.persistence.repository.EducationSupportLedgerJpaRepository;
import com.jjt.platform.infrastructure.persistence.repository.LedgerEntryRepository;
import com.jjt.platform.infrastructure.persistence.repository.OrganisationJpaRepository;
import com.jjt.platform.infrastructure.persistence.repository.ProgressUpdateRepository;
import com.jjt.platform.infrastructure.persistence.repository.SponsorshipJpaRepository;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class ChildReportPdfService {

    private static final Color DARK_GREEN = new Color(28, 53, 44);
    private static final Color MEDIUM_GREEN = new Color(47, 93, 79);
    private static final Color CREAM = new Color(243, 238, 228);
    private static final Color BORDER = new Color(227, 220, 205);
    private static final Color TEXT = new Color(84, 98, 91);

    private final ChildJpaRepository childRepo;
    private final SponsorshipJpaRepository sponsorshipRepo;
    private final EducationSupportLedgerJpaRepository ledgerRepo;
    private final LedgerEntryRepository ledgerEntryRepo;
    private final ProgressUpdateRepository progressRepo;
    private final OrganisationJpaRepository orgRepo;

    public ChildReportPdfService(ChildJpaRepository childRepo,
                                  SponsorshipJpaRepository sponsorshipRepo,
                                  EducationSupportLedgerJpaRepository ledgerRepo,
                                  LedgerEntryRepository ledgerEntryRepo,
                                  ProgressUpdateRepository progressRepo,
                                  OrganisationJpaRepository orgRepo) {
        this.childRepo = childRepo;
        this.sponsorshipRepo = sponsorshipRepo;
        this.ledgerRepo = ledgerRepo;
        this.ledgerEntryRepo = ledgerEntryRepo;
        this.progressRepo = progressRepo;
        this.orgRepo = orgRepo;
    }

    @Transactional(readOnly = true)
    public byte[] generateReport(UUID childId, UUID orgId) throws IOException, DocumentException {
        ChildEntity child = childRepo.findById(childId)
                .orElseThrow(() -> new IllegalArgumentException("Child not found"));

        String orgName = orgId != null
                ? orgRepo.findById(orgId).map(o -> o.getName()).orElse("Junior Jinnah Trust")
                : "Junior Jinnah Trust";

        List<SponsorshipEntity> sponsorships = sponsorshipRepo.findByChildIdOrderByCreatedAtDesc(childId);

        List<LedgerEntryEntity> ledgerEntries = ledgerRepo.findByChild_Id(childId)
                .map(ledger -> ledgerEntryRepo.findByLedger_IdOrderByEntryMonth(ledger.getId()))
                .orElse(List.of());

        List<ProgressUpdateEntity> progressUpdates = progressRepo.findByChildIdOrderByUpdateMonth(childId);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 50, 50, 60, 60);
            PdfWriter.getInstance(doc, out);
            doc.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, DARK_GREEN);
            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 11, TEXT);
            Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, MEDIUM_GREEN);
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 10, TEXT);
            Font boldBodyFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, DARK_GREEN);
            Font smallFont = FontFactory.getFont(FontFactory.HELVETICA, 9, TEXT);
            Font tableHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);

            // ── Header ──────────────────────────────────────────────────────
            Paragraph orgPara = new Paragraph(orgName, titleFont);
            orgPara.setAlignment(Element.ALIGN_CENTER);
            doc.add(orgPara);

            Paragraph reportTitle = new Paragraph("Student Progress Report", subtitleFont);
            reportTitle.setAlignment(Element.ALIGN_CENTER);
            reportTitle.setSpacingBefore(4);
            doc.add(reportTitle);

            Paragraph dateLine = new Paragraph("Generated: " + LocalDate.now(), smallFont);
            dateLine.setAlignment(Element.ALIGN_CENTER);
            dateLine.setSpacingBefore(2);
            dateLine.setSpacingAfter(20);
            doc.add(dateLine);

            // ── Child Information ────────────────────────────────────────────
            addSectionHeader(doc, "Child Information", sectionFont);

            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setSpacingAfter(16);
            addInfoRow(infoTable, "Full Name", child.getFullName(), boldBodyFont, bodyFont);
            addInfoRow(infoTable, "Roll Number", child.getRollNumber(), boldBodyFont, bodyFont);
            addInfoRow(infoTable, "City", child.getCity(), boldBodyFont, bodyFont);
            addInfoRow(infoTable, "Campus", child.getCampusName(), boldBodyFont, bodyFont);
            if (child.getSchoolName() != null) {
                addInfoRow(infoTable, "School", child.getSchoolName(), boldBodyFont, bodyFont);
            }
            addInfoRow(infoTable, "Monthly Education Support",
                    child.getEducationCurrency() + " " + child.getEducationAmount().toPlainString(),
                    boldBodyFont, bodyFont);
            doc.add(infoTable);

            // ── Sponsorship History ──────────────────────────────────────────
            addSectionHeader(doc, "Sponsorship History", sectionFont);

            if (sponsorships.isEmpty()) {
                Paragraph noData = new Paragraph("No sponsorship records.", bodyFont);
                noData.setSpacingAfter(16);
                doc.add(noData);
            } else {
                PdfPTable spTable = new PdfPTable(new float[]{3, 3, 2, 2});
                spTable.setWidthPercentage(100);
                spTable.setSpacingAfter(16);
                addTableHeader(spTable, tableHeaderFont, "Sponsor", "Email", "Start Month", "Status");
                for (SponsorshipEntity s : sponsorships) {
                    String name = s.getSponsor() != null ? s.getSponsor().getDisplayName() : "-";
                    String email = s.getSponsor() != null ? s.getSponsor().getContactEmail() : "-";
                    addTableRow(spTable, bodyFont, name, email, s.getStartMonth(), s.getStatus().name());
                }
                doc.add(spTable);
            }

            // ── Education Ledger ─────────────────────────────────────────────
            addSectionHeader(doc, "Education Support Ledger", sectionFont);

            if (ledgerEntries.isEmpty()) {
                Paragraph noData = new Paragraph("No ledger entries recorded.", bodyFont);
                noData.setSpacingAfter(16);
                doc.add(noData);
            } else {
                PdfPTable ledgerTable = new PdfPTable(new float[]{2, 2, 2});
                ledgerTable.setWidthPercentage(100);
                ledgerTable.setSpacingAfter(16);
                addTableHeader(ledgerTable, tableHeaderFont, "Month", "Amount", "Coverage Type");
                for (LedgerEntryEntity e : ledgerEntries) {
                    addTableRow(ledgerTable, bodyFont,
                            e.getEntryMonth(),
                            e.getEducationCurrency() + " " + e.getEducationAmount().toPlainString(),
                            e.getCoverageType().name().replace("_", " "));
                }
                doc.add(ledgerTable);
            }

            // ── Progress Updates ─────────────────────────────────────────────
            addSectionHeader(doc, "Monthly Progress Updates", sectionFont);

            if (progressUpdates.isEmpty()) {
                Paragraph noData = new Paragraph("No progress updates recorded.", bodyFont);
                noData.setSpacingAfter(16);
                doc.add(noData);
            } else {
                List<ProgressUpdateEntity> sorted = progressUpdates.stream()
                        .sorted((a, b) -> b.getUpdateMonth().compareTo(a.getUpdateMonth()))
                        .toList();
                for (ProgressUpdateEntity p : sorted) {
                    Paragraph monthLabel = new Paragraph(p.getUpdateMonth(), boldBodyFont);
                    monthLabel.setSpacingBefore(8);
                    doc.add(monthLabel);
                    Paragraph summary = new Paragraph(p.getSummary(), bodyFont);
                    summary.setIndentationLeft(12);
                    summary.setSpacingAfter(6);
                    doc.add(summary);
                }
            }

            // ── Footer ───────────────────────────────────────────────────────
            Paragraph footer = new Paragraph(
                    "This report is confidential and intended for internal use only. © " + orgName,
                    smallFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setSpacingBefore(30);
            doc.add(footer);

            doc.close();
            return out.toByteArray();
        }
    }

    private void addSectionHeader(Document doc, String title, Font font) throws DocumentException {
        Paragraph header = new Paragraph(title, font);
        header.setSpacingBefore(10);
        header.setSpacingAfter(6);
        doc.add(header);
    }

    private void addInfoRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBackgroundColor(CREAM);
        labelCell.setBorderColor(BORDER);
        labelCell.setPadding(6);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value != null ? value : "", valueFont));
        valueCell.setBorderColor(BORDER);
        valueCell.setPadding(6);
        table.addCell(valueCell);
    }

    private void addTableHeader(PdfPTable table, Font font, String... headers) {
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, font));
            cell.setBackgroundColor(DARK_GREEN);
            cell.setBorderColor(BORDER);
            cell.setPadding(6);
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            table.addCell(cell);
        }
    }

    private void addTableRow(PdfPTable table, Font font, String... values) {
        for (String v : values) {
            PdfPCell cell = new PdfPCell(new Phrase(v != null ? v : "", font));
            cell.setBorderColor(BORDER);
            cell.setPadding(5);
            table.addCell(cell);
        }
    }
}
