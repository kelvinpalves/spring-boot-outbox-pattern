package br.com.forge.outbox.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobStatusHistoryRepository extends JpaRepository<JobStatusHistory, Long> {

    List<JobStatusHistory> findByJobIdInOrderByChangedAtAsc(List<Long> jobIds);

    List<JobStatusHistory> findByJobIdOrderByChangedAtAsc(Long jobId);

}
