package master;

import common.WorkerInfo;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MasterServer {
    public static void main(String[] args) throws Exception {
        int masterPort = 5000;

        List<WorkerInfo> workers = new ArrayList<>();
        workers.add(new WorkerInfo("localhost", 6001));
        workers.add(new WorkerInfo("localhost", 6002));

        WorkerRegistry workerRegistry = new WorkerRegistry(workers);
        HashRouter hashRouter = new HashRouter(workerRegistry);
        WorkerClient workerClient = new WorkerClient();
        MasterDispatcher dispatcher = new MasterDispatcher(hashRouter, workerClient);

        try (ServerSocket serverSocket = new ServerSocket(masterPort)) {
            System.out.println("Master started on port " + masterPort);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                Thread thread = new Thread(new MasterClientHandler(clientSocket, dispatcher));
                thread.start();
            }
        }
    }
}