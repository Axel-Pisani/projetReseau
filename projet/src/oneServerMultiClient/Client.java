package oneServerMultiClient;

import multiServerMultiClient.Translate;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Client {
    private Socket socket;
    private byte[] buffer = new byte[2048];
    private int length;
    private InputStream in;
    private OutputStream out;

    public Client(InetAddress adr, int port, String name) throws IOException {
        String pathname = "/home/marius/cours/l3s2/ApRéseau/projetReseau/projet/clients/" + name;
        new File(pathname).mkdir();
        connect(adr,port);
        if (isConnect()) {
            out.write(("list").getBytes());
            length = in.read(buffer);
            System.out.println("list:\n" + Translate.translateByteInString(buffer,length));
            closeConnection();
        }
        connect(adr, port);
        if (isConnect()) {
            out.write("get papier".getBytes());
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
        if (isConnect()) {
            out.write("write papier".getBytes());
            out.flush();
            out.write("le papier est pas si fantastic".getBytes());
            closeConnection();
            System.out.println("write is terminate");
        }
        connect(adr, port);
        if (isConnect()) {
            out.write("delete papier1".getBytes());
            closeConnection();
            System.out.println("delete is terminate");
        }
        connect(adr, port);
        if (isConnect()) {
            out.write("create papier1".getBytes());
            closeConnection();
            System.out.println("create is terminate");
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

}
