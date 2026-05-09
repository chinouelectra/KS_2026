package reducer;

import common.Request;
import common.RequestType;
import common.Response;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ReducerRequestHandler implements Runnable {
    private final Socket socket;
    private final ReducerJobCoordinator coordinator;

    public ReducerRequestHandler(Socket socket, ReducerJobCoordinator coordinator) {
        this.socket = socket;
        this.coordinator = coordinator;
    }

    @Override
    public void run() {
        try (Socket client = socket;
             ObjectInputStream in = new ObjectInputStream(client.getInputStream());
             ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream())) {

            Request request = (Request) in.readObject();
            Response response = handle(request);
            out.writeObject(response);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Response handle(Request request) {
        if (request == null || request.getType() == null) {
            return new Response(false, "Invalid reducer request");
        }

        if (request.getType() == RequestType.INIT_PROVIDER_REDUCE_JOB
                || request.getType() == RequestType.INIT_PLAYER_REDUCE_JOB) {
            return coordinator.initAndWait(request, 15000L);
        }

        if (request.getType() != RequestType.SUBMIT_PROVIDER_MAP_RESULT
                && request.getType() != RequestType.SUBMIT_PLAYER_MAP_RESULT) {
            return new Response(false, "Reducer only supports reduce-job init and worker map submissions");
        }

        return coordinator.submitMapResult(request);
    }
}
