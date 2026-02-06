package com.jjt.platform.api.publics.dto;

import java.util.UUID;

public record PublicSponsorshipRequest(UUID childId, PublicSponsorInfo sponsor) {
}
