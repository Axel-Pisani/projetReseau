package multiServerMultiClient;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.Locale;


public class ClientForMultiServer {
    private Socket socket;
    private PrintWriter writer;
    private BufferedReader buffer;
    private final InetAddress adr;
    private String msg;
    private final int port;
    private final DecimalFormat df = new DecimalFormat("0.00");

    public ClientForMultiServer(InetAddress adr, int port, String name) throws IOException {
        String pathname = "/home/marius/cours/l3s2/ApRéseau/projetReseau/projet/clientsMultiServer/" + name;
        new File(pathname).mkdir();
        this.adr = adr;
        this.port = port;
        //list();
        //get(pathname);
        //write();
        //delete();
        //create();
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
            System.out.println("redirection effectuer");
            sendRequest(request);
            readResponse();
            System.out.println(msg);
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
        if (msg.toUpperCase(Locale.ROOT).equals("OK")){
            return true;
        }
        else {
            System.out.println(msg);
            return false;
        }
    }

    private void sendRequest(String request){
        writer.println(request);
        writer.flush();
    }

    private void readResponse() throws IOException {
        msg = buffer.readLine();
        System.out.println("reponse "+ msg);
    }

    private void get(String pathname) throws IOException {
        connect(adr,port);
        if (isConnect()) {
            if (request("get papier.txt")) {
                long size = takeFileSize();
                File file = createFileForGet(pathname);
                writeFileForGet(file, size);
                readResponse();
                System.out.println("message de fin " + msg);
                closeConnection();
            }
        }
    }

    private File createFileForGet(String pathname) throws IOException {
        File file = new File(pathname, "papier.txt");
        file.createNewFile();
        System.out.println("file cree");
        return file;
    }

    private long takeFileSize() throws IOException {
        msg = buffer.readLine();
        System.out.println("size " + msg);
        return Long.parseLong(msg);
    }

    private void writeFileForGet(File file, long size) throws IOException {
        PrintWriter printWriter = new PrintWriter(file);
        System.out.println("demare ecriture");
        double sum = 0;
        while ( sum != size) {
            msg = buffer.readLine();
            System.out.println("reçut " + msg + " size " + msg.length());
            sum = calculateReadingProgression(sum,size,msg.length());
            printWriter.println(msg);
            printWriter.flush();
        }
        System.out.println("recut " + msg);
        System.out.println("fin ecriture");
    }

    private double calculateReadingProgression(double sum, long size, int add){
        sum += add;
        System.out.println(sum);
        System.out.println(df.format(sum / size * 100.0) + "%");
        return sum;
    }

    private void list() throws IOException {
        connect(adr,port);
        if (isConnect()) {
            if (request("list")) {
                StringBuilder builder = new StringBuilder();
                while ((msg = buffer.readLine()) != null) {
                    builder.append(msg).append('\n');
                }
                System.out.println("list:\n" + builder.toString());
            }
            closeConnection();
        }
    }

    private void write() throws IOException {
        connect(adr, port);
        if (isConnect()) {
            if (request("write papier1.txt")) {
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
                System.out.println("ecrie " + msg);
                buffer.close();
                socket.close();
            }
        }
    }

    private void delete() throws IOException {
        connect(adr, port);
        if (isConnect()) {
            if (request("delete papier2.txt")) {
                readResponse();
                System.out.println(msg);
                closeConnection();
            }
        }
    }

    private void create() throws IOException {
        connect(adr, port);
        if (isConnect()) {
            if (request("create papier2.txt")) {
                readResponse();
                System.out.println(msg);
                closeConnection();
            }
        }
    }

}
