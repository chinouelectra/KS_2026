package master;

import common.Request;
import common.Response;
import common.WorkerInfo;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class WorkerClient {

    public Response sendRequest(WorkerInfo workerInfo, Request request) {
        try (Socket socket = new Socket(workerInfo.getHost(), workerInfo.getPort());
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject(request);
            out.flush();

            return (Response) in.readObject();

        } catch (Exception e) {
            return new Response(false, "Failed to communicate with worker: " + e.getMessage());
        }
    }
}