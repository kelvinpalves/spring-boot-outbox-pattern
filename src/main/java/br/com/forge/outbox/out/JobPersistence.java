package br.com.forge.outbox.out;

import java.util.List;

public interface JobPersistence {

    List<JobDto> fetchNextBatch(int batchSize);

    void createJobsForTest(int numberOfJobs);

    void updateJobDataById(Long jobId, String worker, JobStatus jobStatus);

    List<JobProcessedDto> findAll();

    void clearDatabase();

    List<JobProcessedDto> jobsReportWithHistory();

}
