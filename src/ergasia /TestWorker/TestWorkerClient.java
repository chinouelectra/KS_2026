package test;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import common.GameInfo;
import common.Request;
import common.RequestType;
import common.Response;

public class TestWorkerClient {

    public static void main(String[] args) {
        Socket socket = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;

        try {
            socket = new Socket("localhost", 8081);

            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            // Δημιουργία GameInfo
            GameInfo gameInfo = new GameInfo(
                    "PokerStars",
                    "ProviderA",
                    5,
                    100,
                    "logo.png",
                    1.0,
                    100.0,
                    "medium",
                    "secret123"
            );

            // Δημιουργία Request
            Request request = new Request(RequestType.ADD_GAME, gameInfo);

            // Αποστολή Request
            out.writeObject(request);
            out.flush();

            // Λήψη Response
            Response response = (Response) in.readObject();

            System.out.println("Success: " + response.isSuccess());
            System.out.println("Message: " + response.getMessage());

            if (response.getData() != null) {
                System.out.println("Data: " + response.getData());
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) in.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                if (out != null) out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                if (socket != null && !socket.isClosed()) socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}