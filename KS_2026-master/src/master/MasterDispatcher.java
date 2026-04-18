package master;

import common.Request;
import common.RequestType;
import common.Response;
import common.WorkerInfo;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MasterDispatcher {
    private final HashRouter hashRouter;
    private final WorkerRegistry workerRegistry;
    private final WorkerClient workerClient;
    private final ReducerClient reducerClient;
    private final CasinoState casinoState;

    public MasterDispatcher(HashRouter hashRouter,
                            WorkerRegistry workerRegistry,
                            WorkerClient workerClient,
                            ReducerClient reducerClient) {
        this.hashRouter = hashRouter;
        this.workerRegistry = workerRegistry;
        this.workerClient = workerClient;
        this.reducerClient = reducerClient;
        this.casinoState = new CasinoState();
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
            case GET_PROVIDER_STATS -> reduceFromWorkers(
                    Request.providerMapPayload(request.getProviderName(), Collections.emptyMap()),
                    RequestType.MAP_PROVIDER_STATS
            );
            case GET_PLAYER_STATS -> reduceFromWorkers(
                    Request.playerMapPayload(request.getPlayerId(), Collections.emptyMap()),
                    RequestType.MAP_PLAYER_STATS
            );
            case GET_ALL_GAMES -> casinoState.getAllAvailableGames();
            case SEARCH_GAMES -> casinoState.search(request.getProviderName(), request.getRiskLevel(), request.getBetCategory(), request.getMinStars());
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

    private Response reduceFromWorkers(Request request, RequestType reduceType) {
        List<Response> mapResponses = workerClient.broadcast(workerRegistry.getWorkers(), request);
        Map<String, Double> mergedPartials = new LinkedHashMap<>();

        for (Response mapResponse : mapResponses) {
            if (!mapResponse.isSuccess()) {
                return new Response(false, "Worker map phase failed: " + mapResponse.getMessage());
            }
            accumulate(mergedPartials, mapResponse.getTotals());
        }

        Request reducerRequest = reduceType == RequestType.MAP_PROVIDER_STATS
                ? Request.providerMapPayload(request.getProviderName(), mergedPartials)
                : Request.playerMapPayload(request.getPlayerId(), mergedPartials);

        return reducerClient.reduce(reducerRequest);
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

    private void accumulate(Map<String, Double> mergedPartials, Map<String, Double> totals) {
        for (Map.Entry<String, Double> entry : totals.entrySet()) {
            mergedPartials.merge(entry.getKey(), entry.getValue(), Double::sum);
        }
    }
}
