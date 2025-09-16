package br.com.forge.outbox.out.persistence;

import br.com.forge.outbox.out.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JobRepository extends JpaRepository<Job, Long> {

    @Query(value = """
    SELECT * FROM job
    WHERE status = 'PENDING'
    ORDER BY created_at ASC
    FOR UPDATE SKIP LOCKED
    LIMIT :batchSize
    """, nativeQuery = true)
    List<Job> fetchNextBatch(@Param("batchSize") int batchSize);

    List<Job> findAllByStatus(JobStatus status);

    List<Job> findAllByStatusNot(JobStatus status);

}
