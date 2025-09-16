package br.com.forge.outbox.out;

import java.util.List;

public record JobDto(
        Long id,
        String payload,
        JobStatus status,
        String processedBy,
        List<JobStatusChangeDto> history
) {
}
