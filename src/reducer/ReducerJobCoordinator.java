package reducer;

import common.Request;
import common.RequestType;
import common.Response;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class ReducerJobCoordinator {
    private final Map<String, ReduceJob> jobs = new HashMap<>();
    private final ReducerAccumulator accumulator;

    public ReducerJobCoordinator(ReducerAccumulator accumulator) {
        this.accumulator = accumulator;
    }

    public Response initAndWait(Request request, long timeoutMillis) {
        if (request.getJobId() == null || request.getExpectedResults() == null || request.getExpectedResults() <= 0) {
            return new Response(false, "Invalid reduce job request");
        }

        ReduceJob job = new ReduceJob(request.getType(), request.getExpectedResults());

        synchronized (this) {
            if (jobs.containsKey(request.getJobId())) {
                return new Response(false, "Reduce job already exists: " + request.getJobId());
            }
            jobs.put(request.getJobId(), job);
        }

        synchronized (job) {
            long deadline = System.currentTimeMillis() + timeoutMillis;
            while (!job.completed) {
                long remaining = deadline - System.currentTimeMillis();
                if (remaining <= 0) {
                    synchronized (this) {
                        jobs.remove(request.getJobId());
                    }
                    return new Response(false, "Timed out waiting for map results for job " + request.getJobId());
                }

                try {
                    job.wait(remaining);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    synchronized (this) {
                        jobs.remove(request.getJobId());
                    }
                    return new Response(false, "Reducer wait interrupted for job " + request.getJobId());
                }
            }

            return new Response(true, "Reduced totals ready", job.finalTotals);
        }
    }

    public Response submitMapResult(Request request) {
        ReduceJob job;
        synchronized (this) {
            job = jobs.get(request.getJobId());
        }

        if (job == null) {
            return new Response(false, "Reduce job not found: " + request.getJobId());
        }

        synchronized (job) {
            if (job.completed) {
                return new Response(false, "Reduce job already completed: " + request.getJobId());
            }

            accumulator.accumulate(job.mergedTotals, request.getPartialTotals());
            job.receivedResults++;

            if (job.receivedResults >= job.expectedResults) {
                job.finalTotals = job.type == RequestType.INIT_PROVIDER_REDUCE_JOB
                        ? accumulator.finalizeProviderTotals(job.mergedTotals)
                        : accumulator.finalizePlayerTotals(job.mergedTotals);
                job.completed = true;
                synchronized (this) {
                    jobs.remove(request.getJobId());
                }
                job.notifyAll();
                return new Response(true, "Map result accepted and reduce job completed");
            }

            return new Response(true, "Map result accepted by reducer");
        }
    }

    private static class ReduceJob {
        private final RequestType type;
        private final int expectedResults;
        private final Map<String, Double> mergedTotals = new HashMap<>();
        private Map<String, Double> finalTotals = new LinkedHashMap<>();
        private int receivedResults;
        private boolean completed;

        private ReduceJob(RequestType type, int expectedResults) {
            this.type = type;
            this.expectedResults = expectedResults;
        }
    }
}
