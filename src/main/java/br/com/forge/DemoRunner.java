package br.com.forge;

import br.com.forge.outbox.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class DemoRunner implements CommandLineRunner {

    private final JobService jobService;

    @Override
    public void run(String... args) throws Exception {

        jobService.clear();
        jobService.createJobsForTest(10);

        ExecutorService exec = Executors.newFixedThreadPool(2);
        Runnable w1 = () -> jobService.processBatch("Worker-1");
        Runnable w2 = () -> jobService.processBatch("Worker-2");

        for (int i = 0; i < 2; i++) {
            exec.submit(w1);
            exec.submit(w2);
        }

        exec.shutdown();

        if (!exec.awaitTermination(15, TimeUnit.SECONDS)) {
            System.err.println("Timeout: wait for termination");
        }

        jobService.printJobsReportWithHistory();

    }
}
