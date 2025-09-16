package br.com.forge.outbox;

import br.com.forge.outbox.out.JobDto;
import br.com.forge.outbox.out.JobPersistence;
import br.com.forge.outbox.out.JobProcessedDto;
import br.com.forge.outbox.out.JobStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobPersistence jobPersistence;

    public void clear() {
        jobPersistence.clearDatabase();
    }

    public void createJobsForTest(int numberOfJobs) {
        jobPersistence.createJobsForTest(numberOfJobs);
    }

    @Transactional
    public void processBatch(String worker) {
        List<JobDto> jobs = jobPersistence.fetchNextBatch(4);
        if (jobs.isEmpty()) {
            System.out.println("No jobs found");
            return;
        }

        for (JobDto job : jobs) {
            System.out.println("Job will be set to in progress " + job.id());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            jobPersistence.updateJobDataById(job.id(), worker, JobStatus.IN_PROGRESS);

        }

        for (JobDto job : jobs) {
            System.out.println("Job will be set to in published " + job.id());
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            jobPersistence.updateJobDataById(job.id(), worker, JobStatus.PUBLISHED);
        }
    }

    public void printJobsReportWithHistory() {
        List<JobProcessedDto> groups = jobPersistence.jobsReportWithHistory();
        for (JobProcessedDto grp : groups) {
            System.out.println("=== Worker: " + grp.worker() + " ===");
            for (JobDto j : grp.jobs()) {
                System.out.printf("Job %d | status=%s | payload=%s%n",
                        j.id(), j.status(), j.payload());
                if (!j.history().isEmpty()) {
                    System.out.println("  history:");
                    j.history().forEach(h ->
                            System.out.printf("    %s -> %s | by=%s | at=%s | note=%s%n",
                                    h.fromStatus(), h.toStatus(), h.changedBy(), h.changedAt(), h.note()));
                }
            }
        }
    }

    public void jobsReport() {
        System.out.println("Jobs Report");
        final var workersReport = jobPersistence.findAll();
        workersReport.forEach(worker -> {
            System.out.println("----");
            System.out.println(worker.worker());
            worker.jobs().forEach(System.out::println);
            System.out.println("----");
        });
    }

}
