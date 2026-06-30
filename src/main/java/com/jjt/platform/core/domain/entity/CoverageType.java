package com.jjt.platform.core.domain.entity;

public enum CoverageType {
    EARLY_SUPPORT,
    SPONSOR,
    /** Correction entry that supersedes an earlier erroneous entry. Introduced in Phase 2. */
    CORRECTION
}
