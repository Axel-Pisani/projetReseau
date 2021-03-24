package multiServerMultiClient;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ClientForMultiServer {
    Socket socket;

    public ClientForMultiServer(InetAddress adr, int port, String name) throws IOException {
        String pathname = "/home/marius/cours/l3s2/ApRéseau/projet/clients/" + name;
        new File(pathname).mkdir();
        socket = new Socket(adr, port);
        InputStream in = socket.getInputStream();
        OutputStream out = socket.getOutputStream();
        byte[] buffer = new byte[2048];
        int length = in.read(buffer);
        System.out.println(Translate.translateByteInString(buffer, length));
        out.write(("list").getBytes());
        length = in.read(buffer);
        System.out.println(Translate.translateByteInString(buffer, length));
        in.close();
        socket.close();
        socket = new Socket(adr,port);
        in = socket.getInputStream();
        out = socket.getOutputStream();
        System.out.println(Translate.translateByteInString(buffer, length));
        redirection("get papier",socket,buffer);
        File file =  new File(pathname,"papier");
        file.createNewFile();
        PrintWriter writer = new PrintWriter(file);
        System.out.println("start while");
        while ((length = in.read(buffer)) > 0){
            writer.write(Translate.translateByteInString(buffer,length));
            writer.flush();
        }
        System.out.println("end while");
        in.close();
        out.close();
        socket.close();
        socket = new Socket(adr,port);
        in = socket.getInputStream();
        out = socket.getOutputStream();
        redirection("write papier",socket,buffer);
        out.write("le papier est pas si fantastic".getBytes());
        out.close();
        in.close();
        socket.close();
        socket = new Socket(adr,port);
        in = socket.getInputStream();
        out = socket.getOutputStream();
        redirection("delete papier2",socket,buffer);
        out.close();
        in.close();
        socket.close();
        socket = new Socket(adr,port);
        in = socket.getInputStream();
        out = socket.getOutputStream();
        redirection("create papier1",socket,buffer);
        out.close();
        in.close();
        socket.close();
    }

    private void redirection(String request, Socket socket, byte[] buffer) throws IOException {
        InetAddress address = socket.getInetAddress();
        int port = socket.getPort();
        socket.getOutputStream().write(request.getBytes());
        socket.getOutputStream().flush();
        int length = socket.getInputStream().read(buffer);
        String msg = Translate.translateByteInString(buffer,length);
        if (msg.contains("REDIRECTION")){
            socket.getInputStream().close();
            socket.getOutputStream().close();
            socket.close();
            String[] info = msg.split(" ");
            socket = new Socket(InetAddress.getByName(info[1]),Integer.parseInt(info[2]));
            socket.getOutputStream().write(request.getBytes(StandardCharsets.UTF_8));
        }
        socket.getInputStream().read(buffer);
    }


}
