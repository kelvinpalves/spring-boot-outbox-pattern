// br.com.forge.outbox.out.JobMapper
package br.com.forge.outbox.out;

import br.com.forge.outbox.out.persistence.Job;
import br.com.forge.outbox.out.persistence.JobStatusHistory;

import java.util.*;
import java.util.stream.Collectors;

public final class JobMapper {
    private JobMapper() {}

    public static List<JobProcessedDto> groupByWorkerWithHistory(List<Job> jobs,
                                                                 List<JobStatusHistory> histories) {
        Map<Long, List<JobStatusHistory>> historyByJobId = histories.stream()
                .collect(Collectors.groupingBy(h -> h.getJob().getId(),
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> list.stream()
                                        .sorted(Comparator.comparing(JobStatusHistory::getChangedAt))
                                        .toList()
                        )
                ));

        List<JobDto> jobDtos = jobs.stream()
                .map(j -> new JobDto(
                        j.getId(),
                        j.getPayload(),
                        j.getStatus(),
                        j.getProcessedBy(),
                        historyByJobId.getOrDefault(j.getId(), List.of()).stream()
                                .map(h -> new JobStatusChangeDto(
                                        h.getFromStatus(),
                                        h.getToStatus(),
                                        h.getChangedBy(),
                                        h.getChangedAt(),
                                        h.getNote()
                                ))
                                .toList()
                ))
                .toList();

        Map<String, List<JobDto>> grouped = jobDtos.stream()
                .collect(Collectors.groupingBy(dto ->
                        dto.processedBy() == null ? "UNKNOWN" : dto.processedBy()));

        return grouped.entrySet().stream()
                .map(e -> new JobProcessedDto(e.getKey(), e.getValue()))
                .toList();
    }
}
