package com.jjt.platform.api.common.mapper;

import com.jjt.platform.api.common.dto.ChildDto;
import com.jjt.platform.api.common.dto.AvailabilityStatus;
import com.jjt.platform.api.common.dto.LedgerDto;
import com.jjt.platform.api.common.dto.LedgerEntryDto;
import com.jjt.platform.api.common.dto.ProgressUpdateDto;
import com.jjt.platform.core.domain.entity.Child;
import com.jjt.platform.core.domain.entity.EducationSupportLedger;
import com.jjt.platform.core.domain.entity.LedgerEntry;
import com.jjt.platform.core.domain.entity.ProgressUpdate;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public final class DtoMapper {

    private DtoMapper() {}

    public static ChildDto toChildDto(Child child, AvailabilityStatus availabilityStatus, LocalDate enrolledAt) {
        return new ChildDto(
                child.getId(),
                child.getRollNumber(),
                child.getFullName(),
                child.getCity(),
                child.getCampusName(),
                child.getSchoolName(),
                child.getEducationCost().getAmount().toPlainString(),
                child.getEducationCost().getCurrency().getCurrencyCode(),
                availabilityStatus,
                enrolledAt
        );
    }

    public static LedgerDto toLedgerDto(EducationSupportLedger ledger) {
        List<LedgerEntryDto> entries = ledger.getEntriesByMonth().values().stream()
                .map(DtoMapper::toLedgerEntryDto)
                .toList();
        return new LedgerDto(ledger.getChildId(), entries);
    }

    public static LedgerEntryDto toLedgerEntryDto(LedgerEntry entry) {
        return new LedgerEntryDto(
                entry.getId(),
                entry.getMonth().getValue().toString(),
                entry.getEducationCost().getAmount().toPlainString(),
                entry.getEducationCost().getCurrency().getCurrencyCode(),
                entry.getCoverageType().name()
        );
    }

    public static List<ProgressUpdateDto> toProgressDtos(List<ProgressUpdate> updates) {
        return updates.stream()
                .map(u -> new ProgressUpdateDto(
                        u.getId(),
                        u.getMonth().getValue().toString(),
                        u.getSummary()))
                .collect(Collectors.toList());
    }
}
