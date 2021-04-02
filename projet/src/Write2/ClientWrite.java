package Write2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Locale;

public class ClientWrite {
    private Socket socket;
    private PrintWriter writer;
    private BufferedReader buffer;
    private final InetAddress adr;
    private String msg;
    private final int port;

    public ClientWrite(InetAddress adr, int port, String nameFile) throws IOException {
        this.adr = adr;
        this.port = port;
        write(nameFile);
    }

    private boolean request(String request) throws IOException {
        sendRequest(request);
        readResponse();
        if (isRedirection(msg)){
            redirection(request,msg);
        }
        return requestIsPossible();
    }
    private void redirection(String request, String target) throws IOException {
        closeConnection();
        String[] info = target.split(" ");
        connect(InetAddress.getByName(info[1].split("/")[0]),Integer.parseInt(info[2]));
        if (isConnect()) {
            sendRequest(request);
            readResponse();
        }
    }

    private boolean isRedirection(String msg){
        return msg.contains("REDIRECTION");
    }


    private boolean isConnect() throws IOException {
        msg = buffer.readLine();
        System.out.println(msg);
        return msg.equals("connection");
    }

    private void connect(InetAddress adr, int port) throws IOException {
        socket = new Socket(adr, port);
        buffer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new PrintWriter(socket.getOutputStream());
    }

    private void closeConnection() throws IOException {
        buffer.close();
        writer.close();
        socket.close();
    }

    private boolean requestIsPossible() {
        return msg.toUpperCase(Locale.ROOT).equals("OK");
    }

    private void sendRequest(String request){
        writer.println(request);
        writer.flush();
    }

    private void readResponse() throws IOException {
        msg = buffer.readLine();
    }

    private void write(String nameFile) throws IOException {
        connect(adr, port);
        if (isConnect()) {
            if (request("write " + nameFile)) {
                System.out.println("write message and finish with Ctrl d");
                BufferedReader scanner = new BufferedReader(new InputStreamReader(System.in));
                String tampon = scanner.readLine();
                while (tampon != null){
                    writer.println(tampon);
                    writer.flush();
                    tampon = scanner.readLine();
                }
                socket.shutdownOutput();
                readResponse();
                buffer.close();
                socket.close();
            }
        }
    }
}
