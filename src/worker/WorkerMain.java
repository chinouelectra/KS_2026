package worker;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class WorkerMain {

    public static void main(String[] args) {
        ServerSocket server = null;
        WorkerStorage storage = new WorkerStorage();

        try {
            server = new ServerSocket(8081);
            System.out.println("Worker server started at port 8081");

            while (true) {
                Socket client = server.accept();
                System.out.println("New connection from: " + client.getInetAddress());

                HandleThreadWorker thread = new HandleThreadWorker(client, storage);
                thread.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (server != null && !server.isClosed()) {
                    server.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}