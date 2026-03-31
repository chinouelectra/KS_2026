package master;

import common.Request;
import common.RequestType;
import common.Response;
import common.WorkerInfo;

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
            case ADD_GAME -> casinoState.addGame(request.getGameInfo());
            case REMOVE_GAME -> casinoState.removeGame(request.getGameName());
            case UPDATE_GAME_RISK -> casinoState.updateRisk(request.getGameName(), request.getRiskLevel());
            case UPDATE_GAME_BET_LIMITS -> casinoState.updateBetLimits(request.getGameName(), request.getMinBet(), request.getMaxBet());
            case GET_PROVIDER_STATS -> casinoState.providerStats(request.getProviderName());
            case GET_PLAYER_STATS -> casinoState.playerStats(request.getPlayerId());
            case SEARCH_GAMES -> casinoState.search(request.getProviderName(), request.getRiskLevel(), request.getBetCategory(), request.getMinStars());
            case PLACE_BET -> casinoState.placeBet(request.getPlayerId(), request.getGameName(), request.getBetAmount());
            case ADD_BALANCE -> casinoState.addBalance(request.getPlayerId(), request.getBetAmount());
            case HEALTH_CHECK -> new Response(true, "MASTER_OK");
            default -> new Response(false, "Unsupported request type for master: " + request.getType());
        };
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

    private void accumulate(Map<String, Double> mergedPartials, Map<String, Double> totals) {
        for (Map.Entry<String, Double> entry : totals.entrySet()) {
            mergedPartials.merge(entry.getKey(), entry.getValue(), Double::sum);
        }
    }
}
