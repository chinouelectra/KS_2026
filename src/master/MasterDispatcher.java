package master;

import common.Request;
import common.Response;
import common.WorkerInfo;

import java.util.List;
import java.util.UUID;

public class MasterDispatcher {
    private static final long REDUCE_TIMEOUT_MILLIS = 15000L;

    private final HashRouter hashRouter;
    private final WorkerRegistry workerRegistry;
    private final WorkerClient workerClient;
    private final ReducerClient reducerClient;
    private final RandomGeneratorClient randomGeneratorClient;
    private final CasinoState casinoState;
    private final String reducerHost;
    private final int reducerPort;

    public MasterDispatcher(HashRouter hashRouter,
                            WorkerRegistry workerRegistry,
                            WorkerClient workerClient,
                            ReducerClient reducerClient,
                            RandomGeneratorClient randomGeneratorClient,
                            String reducerHost,
                            int reducerPort) {
        this.hashRouter = hashRouter;
        this.workerRegistry = workerRegistry;
        this.workerClient = workerClient;
        this.reducerClient = reducerClient;
        this.randomGeneratorClient = randomGeneratorClient;
        this.casinoState = new CasinoState();
        this.reducerHost = reducerHost;
        this.reducerPort = reducerPort;
    }

    public Response dispatch(Request request) {
        if (request == null || request.getType() == null) {
            return new Response(false, "Invalid request");
        }

        return switch (request.getType()) {
            case ADD_GAME -> addGameToMasterAndWorker(request);
            case REMOVE_GAME -> removeGameFromMasterAndWorker(request);
            case UPDATE_GAME_RISK -> updateRiskOnMasterAndWorker(request);
            case UPDATE_GAME_BET_LIMITS -> updateBetLimitsOnMasterAndWorker(request);
            case GET_PROVIDER_STATS -> reduceProviderStats(request.getProviderName());
            case GET_PLAYER_STATS -> reducePlayerStats(request.getPlayerId());
            case GET_ALL_GAMES -> casinoState.getAllAvailableGames();
            case SEARCH_GAMES ->
                    casinoState.search(request.getProviderName(), request.getRiskLevel(), request.getBetCategory(), request.getMinStars());
            case PLACE_BET -> routeByGameName(request.getGameName(), request);
            case ADD_BALANCE -> broadcastToWorkers(request);
            default -> new Response(false, "Unsupported request type for master: " + request.getType());
        };
    }

    private Response addGameToMasterAndWorker(Request request) {
        Response masterResponse = casinoState.addGame(request.getGameInfo());
        if (!masterResponse.isSuccess()) {
            return masterResponse;
        }

        Response rngResponse = randomGeneratorClient.registerGame(request.getGameInfo());
        if (!rngResponse.isSuccess()) {
            casinoState.removeGame(request.getGameInfo().getGameName());
            return rngResponse;
        }

        return routeByGameName(request.getGameInfo().getGameName(), request);
    }

    private Response removeGameFromMasterAndWorker(Request request) {
        Response masterResponse = casinoState.removeGame(request.getGameName());
        if (!masterResponse.isSuccess()) {
            return masterResponse;
        }
        return routeByGameName(request.getGameName(), request);
    }

    private Response updateRiskOnMasterAndWorker(Request request) {
        Response masterResponse = casinoState.updateRisk(request.getGameName(), request.getRiskLevel());
        if (!masterResponse.isSuccess()) {
            return masterResponse;
        }
        return routeByGameName(request.getGameName(), request);
    }

    private Response updateBetLimitsOnMasterAndWorker(Request request) {
        Response masterResponse = casinoState.updateBetLimits(request.getGameName(), request.getMinBet(), request.getMaxBet());
        if (!masterResponse.isSuccess()) {
            return masterResponse;
        }
        return routeByGameName(request.getGameName(), request);
    }

    private Response routeByGameName(String gameName, Request request) {
        try {
            WorkerInfo targetWorker = hashRouter.routeByGameName(gameName);
            return workerClient.sendRequest(targetWorker, request);
        } catch (Exception e) {
            return new Response(false, e.getMessage());
        }
    }

    private Response reduceProviderStats(String providerName) {
        String jobId = UUID.randomUUID().toString();
        Request reducerInitRequest = Request.initProviderReduceJob(providerName, jobId, workerRegistry.getWorkers().size());
        Request workerMapRequest = Request.providerMapTask(providerName, jobId, reducerHost, reducerPort);
        return reduceUsingReducerWait(workerMapRequest, reducerInitRequest);
    }

    private Response reducePlayerStats(String playerId) {
        String jobId = UUID.randomUUID().toString();
        Request reducerInitRequest = Request.initPlayerReduceJob(playerId, jobId, workerRegistry.getWorkers().size());
        Request workerMapRequest = Request.playerMapTask(playerId, jobId, reducerHost, reducerPort);
        return reduceUsingReducerWait(workerMapRequest, reducerInitRequest);
    }

    private Response reduceUsingReducerWait(Request workerMapRequest, Request reducerInitRequest) {
        ReducerResponseWaiter waiter = new ReducerResponseWaiter();
        Thread reducerThread = new Thread(() -> waiter.complete(reducerClient.send(reducerInitRequest)));
        reducerThread.start();

        List<Response> workerResponses = workerClient.broadcast(workerRegistry.getWorkers(), workerMapRequest);
        for (Response workerResponse : workerResponses) {
            if (!workerResponse.isSuccess()) {
                return new Response(false, "Worker map phase failed: " + workerResponse.getMessage());
            }
        }

        return waiter.await(REDUCE_TIMEOUT_MILLIS);
    }

    private Response broadcastToWorkers(Request request) {
        List<Response> responses = workerClient.broadcast(workerRegistry.getWorkers(), request);
        for (Response response : responses) {
            if (!response.isSuccess()) {
                return new Response(false, "Worker request failed: " + response.getMessage());
            }
        }
        return new Response(true, "Request applied to all workers");
    }

    private static class ReducerResponseWaiter {
        private Response response;
        private boolean completed;

        public synchronized void complete(Response response) {
            if (completed) {
                return;
            }
            this.response = response;
            this.completed = true;
            notifyAll();
        }

        public synchronized Response await(long timeoutMillis) {
            long deadline = System.currentTimeMillis() + timeoutMillis;
            while (!completed) {
                long remaining = deadline - System.currentTimeMillis();
                if (remaining <= 0) {
                    return new Response(false, "Timed out waiting for reducer response");
                }
                try {
                    wait(remaining);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return new Response(false, "Interrupted while waiting for reducer response");
                }
            }
            return response;
        }
    }
}
