package multiServerMultiClient;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;


public class ClientForMultiServer {
    private Socket socket;
    private InputStream in;
    private OutputStream out;
    private int length;
    private byte[] buffer = new byte[2048];

    public ClientForMultiServer(InetAddress adr, int port, String name) throws IOException {
        String pathname = "/home/marius/cours/l3s2/ApRéseau/projetReseau/projet/clients" + name;
        new File(pathname).mkdir();
        connect(adr, port);
        if (isConnect()) {
            request("list");
            System.out.println(Translate.translateByteInString(buffer, length));
            closeConnection();
        }
        connect(adr, port);
        if (isConnect()) {
            request("get papier");
            File file = new File(pathname, "result");
            boolean alors = file.createNewFile();
            length = in.read(buffer);
            String msg = Translate.translateByteInString(buffer,length);
            PrintWriter writer = new PrintWriter(file);
            if (!msg.contains("ok to start the transmission\n")) {
                while ((length = in.read(buffer)) > 0) {
                    writer.write(Translate.translateByteInString(buffer, length));
                    writer.flush();
                }
            }
            else {
                for (String string : Translate.translateByteInString(buffer,length).split("\n")){
                    if (!string.equals("ok to start the transmission")){
                        writer.write(string +'\n');
                        writer.flush();
                    }
                }
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
            System.out.println(Translate.translateByteInString(buffer,length));
        }
        closeConnection();
        connect(adr, port);
        if (isConnect()) {
            request("create papier1");
        }
        closeConnection();
        connect(adr, port);
        if (isConnect()) {
            request("list");
            System.out.println(Translate.translateByteInString(buffer, length));
        }
        closeConnection();
    }

    private void redirection(String request, String target) throws IOException {
        closeConnection();
        String[] info = target.split(" ");
        connect(InetAddress.getByName(info[1].split("/")[0]),Integer.parseInt(info[2]));
        if (isConnect()) {
            socket.getOutputStream().write(request.getBytes());
        }
    }

    private boolean isConnect() throws IOException {
        length = socket.getInputStream().read(buffer);
        System.out.println(Translate.translateByteInString(buffer, length));
        return Translate.translateByteInString(buffer, length).equals("connected");
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
        length = socket.getInputStream().read(buffer);
        String target = Translate.translateByteInString(buffer,length);
        if (isRedirection(Translate.translateByteInString(buffer,length))){
            redirection(request,target);
        }
    }

    private boolean isRedirection(String msg){
        return msg.contains("REDIRECTION");
    }


}
