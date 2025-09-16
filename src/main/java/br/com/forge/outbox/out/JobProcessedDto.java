package br.com.forge.outbox.out;

import java.util.List;

public record JobProcessedDto(
        String worker,
        List<JobDto> jobs
) {
}
