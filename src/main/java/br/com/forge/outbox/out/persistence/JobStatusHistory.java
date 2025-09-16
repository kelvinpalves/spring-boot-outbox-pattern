package br.com.forge.outbox.out.persistence;

import br.com.forge.outbox.out.JobStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "job_status_history",
        indexes = {
                @Index(name = "ix_job_status_history_job", columnList = "job_id"),
                @Index(name = "ix_job_status_history_changed_at", columnList = "changed_at")
        })
public class JobStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", length = 20, nullable = false)
    private JobStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", length = 20, nullable = false)
    private JobStatus toStatus;

    @Column(name = "changed_by", length = 100)
    private String changedBy; // ex.: nome do worker

    @Column(name = "changed_at", nullable = false)
    private OffsetDateTime changedAt = OffsetDateTime.now();

    @Column(name = "note", length = 500)
    private String note;

    public static JobStatusHistory of(Job job, JobStatus from, JobStatus to, String by, String note) {
        return new JobStatusHistory(null, job, from, to, by, OffsetDateTime.now(), note);
    }
}
