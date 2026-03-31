package TestWorker;

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

            GameInfo gameInfo = new GameInfo();
            gameInfo.setGameName("PokerStars");
            gameInfo.setProviderName("ProviderA");
            gameInfo.setStars(5);
            gameInfo.setNoOfVotes(100);
            gameInfo.setGameLogo("logo.png");
            gameInfo.setMinBet(1.0);
            gameInfo.setMaxBet(100.0);
            gameInfo.setRiskLevel("medium");
            gameInfo.setHashKey("secret123");
            gameInfo.setActive(true);
            gameInfo.setBetAmount(10.0);

            Request request = new Request(RequestType.ADD_GAME, gameInfo);

            out.writeObject(request);
            out.flush();

            Response response = (Response) in.readObject();

            System.out.println("====== RESPONSE FROM WORKER ======");
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