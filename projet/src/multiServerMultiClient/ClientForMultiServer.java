package multiServerMultiClient;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ClientForMultiServer {
    private Socket socket;
    private InputStream in;
    private OutputStream out;
    private int length;
    private byte[] buffer;

    public ClientForMultiServer(InetAddress adr, int port, String name) throws IOException {
        String pathname = "/home/marius/cours/l3s2/ApRéseau/projet/clients/" + name;
        new File(pathname).mkdir();
        connect(adr, port);
        if (isConnect()) {
            request("list");
            length = in.read(buffer);
            System.out.println(Translate.translateByteInString(buffer, length));
            closeConnection();
        }
        connect(adr, port);
        if (isConnect()) {
            request("get papier");
            length = in.read(buffer);
            long size = Long.parseLong(Translate.translateByteInString(buffer,length));
            File file = new File(pathname, "papier");
            file.createNewFile();
            PrintWriter writer = new PrintWriter(file);
            double sum = 0;
            double progress;
            while ((length = in.read(buffer)) > 0) {
                sum += length;
                progress = sum/size * 100.0;
                System.out.println(progress);
                writer.write(Translate.translateByteInString(buffer, length));
                writer.flush();
            }
            closeConnection();
        }
        connect(adr, port);
        if (isConnect()){
            request("write papier");
            out.write("le papier est pas si fantastic".getBytes());

        }
        closeConnection();
        connect(adr, port);
        if (isConnect()) {
            request("delete papier2");
        }
        closeConnection();
        connect(adr, port);
        if (isConnect()) {
            request("create papier1");
        }
        closeConnection();
    }

    private void redirection(String request, String target) throws IOException {
        closeConnection();
        String[] info = target.split(" ");
        connect(InetAddress.getByName(info[1]),Integer.parseInt(info[2]));
        if (isConnect()) {
            socket.getOutputStream().write(request.getBytes());
        }
    }

    private boolean isConnect() throws IOException {
        length = socket.getInputStream().read(buffer);
        return Translate.translateByteInString(buffer, length).equals("connection\n");
    }

    private void connect(InetAddress adr, int port) throws IOException {
        socket = new Socket(adr, port);
        in = socket.getInputStream();
        out = socket.getOutputStream();
    }

    private void closeConnection() throws IOException {
        in.close();
        out.close();
        socket.close();
    }

    private void request(String request) throws IOException {
        socket.getOutputStream().write(request.getBytes());
        socket.getOutputStream().flush();
        int length = socket.getInputStream().read(buffer);
        String target = Translate.translateByteInString(buffer,length);
        if (isRedirection(Translate.translateByteInString(buffer,length))){
            redirection(request,target);
        }
    }

    private boolean isRedirection(String msg){
        return msg.contains("REDIRECTION");
    }


}
