package master;

import common.Request;
import common.RequestType;
import common.Response;
import common.WorkerInfo;

public class MasterDispatcher {
    private final HashRouter hashRouter;
    private final WorkerClient workerClient;

    public MasterDispatcher(HashRouter hashRouter, WorkerClient workerClient) {
        this.hashRouter = hashRouter;
        this.workerClient = workerClient;
    }

    public Response dispatch(Request request) {
        if (request == null || request.getType() == null) {
            return new Response(false, "Invalid request");
        }

        if (request.getType() == RequestType.ADD_GAME) {
            if (request.getGameInfo() == null || request.getGameInfo().getGameName() == null) {
                return new Response(false, "Game info or game name is missing");
            }

            WorkerInfo targetWorker =
                    hashRouter.routeByGameName(request.getGameInfo().getGameName());

            return workerClient.sendRequest(targetWorker, request);
        }

        return new Response(false, "Unsupported request type");
    }
}