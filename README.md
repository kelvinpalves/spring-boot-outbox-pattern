# Outbox Pattern – Concurrent Job Processing Demo

This project demonstrates a **database-backed queue** using  
PostgreSQL `FOR UPDATE SKIP LOCKED` with full **status history** tracking.
It shows how multiple workers can safely process jobs in parallel
while keeping an audit trail of every status change.

---

## Features
- **Spring Boot 3 + JPA (Jakarta)**  
- **PostgreSQL 16** running in Docker
- Concurrency control with  
  ```sql
  SELECT ... FOR UPDATE SKIP LOCKED

* `Job` entity with life-cycle states:
  `PENDING → IN_PROGRESS → PUBLISHED`
* Complete **status history** (`JobStatusHistory`) for every transition
* Cascading delete: removing a `Job` automatically deletes its history

## Running the Demo

### Requirements

* **Docker / Colima** (or Docker Desktop)
* **Java 17+**
* **Maven**

### Start with Docker Compose

```bash
colima start          # or ensure Docker is running
docker compose up --build
```

This will:

* Start PostgreSQL (`postgres` service)
* Build and run the Spring Boot app (`app` service)

The application creates jobs and runs two concurrent workers.
Watch the logs to see jobs move from `PENDING` → `IN_PROGRESS` → `PUBLISHED`.

---

## How It Works

1. **Job Creation**
   `DemoRunner` seeds sample jobs on startup.

2. **Parallel Processing**
   Two worker threads repeatedly call:

   ```sql
   SELECT *
     FROM job
    WHERE status = 'PENDING'
    ORDER BY created_at
    FOR UPDATE SKIP LOCKED
    LIMIT 5;
   ```

   Each transaction locks and updates its batch to `IN_PROGRESS`
   so no two workers can process the same rows.

3. **Status History**
   Every transition (including the worker and timestamp) is
   persisted to `job_status_history`.

4. **Report**
   After all workers finish, a report groups processed jobs by worker
   and lists the full status history.

---

## Sample Output

```
=== Worker: Worker-1 ===
Job 1406 | status=PUBLISHED
  history:
    PENDING -> IN_PROGRESS | by=Worker-1 | at=...
    IN_PROGRESS -> PUBLISHED | by=Worker-1 | at=...
...
=== Worker: Worker-2 ===
Job 1410 | status=PUBLISHED
  history:
    PENDING -> IN_PROGRESS | by=Worker-2 | at=...
    IN_PROGRESS -> PUBLISHED | by=Worker-2 | at=...
```

---

## Key Takeaways

* **Safe concurrency**: `SKIP LOCKED` guarantees that each job is processed only once.
* **Auditability**: every status change is recorded with timestamp, user/worker, and optional notes.
* **Clean-up ready**: `ON DELETE CASCADE` ensures that deleting a job removes its entire history.

---

## Cleaning Up

Stop and remove containers and volumes:

```bash
docker compose down -v
```

---

This repository serves as a **reference implementation** of the Outbox pattern and
a template for any system that needs **reliable, concurrent job processing with
complete status history**.
