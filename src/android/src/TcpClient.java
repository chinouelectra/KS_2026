public class TcpClient {

    private String host;
    private int port;

    public TcpClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public Response sendRequest(Request request) {
        Socket socket = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;

        try {
            socket = new Socket(host, port);

            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();

            in = new ObjectInputStream(socket.getInputStream());

            out.writeObject(request);
            out.flush();

            return (Response) in.readObject();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try { if (in != null) in.close(); } catch (Exception ignored) {}
            try { if (out != null) out.close(); } catch (Exception ignored) {}
            try { if (socket != null) socket.close(); } catch (Exception ignored) {}
        }
    }
}