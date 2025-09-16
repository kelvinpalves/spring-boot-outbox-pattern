package br.com.forge.outbox.out;

import java.time.OffsetDateTime;

public record JobStatusChangeDto(
        JobStatus fromStatus,
        JobStatus toStatus,
        String changedBy,
        OffsetDateTime changedAt,
        String note
) {}
