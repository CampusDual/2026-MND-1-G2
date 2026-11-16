package com.campusdual.cd2026mnd1g1.api.dto;

import java.time.LocalDateTime;

public record RequestStatusHistoryDto(String previousStatus, String newStatus, LocalDateTime changedAt) {
}
