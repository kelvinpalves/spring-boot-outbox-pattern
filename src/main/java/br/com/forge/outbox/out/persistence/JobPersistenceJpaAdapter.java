package br.com.forge.outbox.out.persistence;

import br.com.forge.outbox.out.JobDto;
import br.com.forge.outbox.out.JobMapper;
import br.com.forge.outbox.out.JobPersistence;
import br.com.forge.outbox.out.JobProcessedDto;
import br.com.forge.outbox.out.JobStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobPersistenceJpaAdapter implements JobPersistence {

    private final JobRepository jobRepository;
    private final JobStatusHistoryRepository jobStatusHistoryRepository;

    @Override
    public List<JobDto> fetchNextBatch(int batchSize) {
        return jobRepository.fetchNextBatch(batchSize)
                .stream()
                .map(job -> new JobDto(job.getId(), job.getPayload(), job.getStatus(), null, null))
                .collect(Collectors.toList());
    }

    @Override
    public void createJobsForTest(int numberOfJobs) {
        for (int i = 0; i < numberOfJobs; i++) {
            Job job = new Job();
            job.setPayload("Payload + " + LocalDateTime.now());
            job.setStatus(JobStatus.PENDING);
            jobRepository.save(job);
            System.out.println("Job #" + i + " created");
        }
    }

    @Override
    public void updateJobDataById(Long jobId,
                                  String worker,
                                  JobStatus jobStatus) {
        final var job = jobRepository.findById(jobId);

        job.ifPresentOrElse(jobToBeUpdated -> {
                transitionStatus(jobToBeUpdated, jobStatus, worker, "Note");
        }, () -> {
            log.warn("Job not found for id {}", jobId);
        });
    }

    public void transitionStatus(Job job, JobStatus newStatus, String changedBy, String note) {
        JobStatus current = job.getStatus();
        if (current == newStatus) return;

        job.setStatus(newStatus);
        job.setProcessedBy(changedBy);
        job.setUpdatedAt(OffsetDateTime.now());

        jobStatusHistoryRepository.save(JobStatusHistory.of(job, current, newStatus, changedBy, note));
    }

    @Override
    public List<JobProcessedDto> findAll() {
        Map<String, List<JobDto>> grouped = jobRepository.findAllByStatus(JobStatus.PUBLISHED).stream()
                .collect(Collectors.groupingBy(
                        job -> job.getProcessedBy() == null ? "UNKNOWN" : job.getProcessedBy(),
                        Collectors.mapping(j -> new JobDto(
                                j.getId(),
                                j.getPayload(),
                                j.getStatus(),
                                j.getProcessedBy(),
                                null
                        ), Collectors.toList())
                ));

        return grouped.entrySet().stream()
                .map(e -> new JobProcessedDto(e.getKey(), e.getValue()))
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<JobProcessedDto> jobsReportWithHistory() {
        List<Job> jobs = jobRepository.findAllByStatusNot(JobStatus.PENDING);

        if (jobs.isEmpty()) return List.of();

        List<Long> ids = jobs.stream().map(Job::getId).toList();
        List<JobStatusHistory> histories = jobStatusHistoryRepository.findByJobIdInOrderByChangedAtAsc(ids);

        return JobMapper.groupByWorkerWithHistory(jobs, histories);
    }

    @Override
    public void clearDatabase() {
        jobStatusHistoryRepository.deleteAll();
        jobRepository.deleteAll();
    }
}
