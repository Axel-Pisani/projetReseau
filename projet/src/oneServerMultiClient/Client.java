package oneServerMultiClient;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.Locale;
import java.util.Scanner;


public class Client {
    private Socket socket;
    private BufferedReader buffer;
    private PrintWriter writer;
    private String msg;
    private InetAddress adr;
    private int port;
    private DecimalFormat df = new DecimalFormat("0.00");

    public Client(InetAddress adr, int port) throws IOException {
        this.adr = adr;
        this.port = port;
        manageClientChose();
    }


    private String getMenu(){
        return "chose request among possibility and enter this number:\n1:list\n2:get\n3:write\n4:delete\n5:create\n6:finish\n";
    }

    private void manageClientChose() throws IOException {
        String menu = getMenu();
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.println(menu);
        String enter;
        while (true) {
            enter = reader.readLine();
            switch (enter) {
                case "1":
                    list();
                    break;
                case "2":
                    get(reader);
                    break;
                case "3":
                    write(reader);
                    return;
                case "4":
                    delete(reader);
                    break;
                case "5":
                    create(reader);
                    break;
                case "6":
                    return;
                default:
                    System.err.println("ERROR : the request isn't valid");
            }
            System.out.println(menu);
        }
    }

    private String getFileName(BufferedReader reader) throws IOException {
        System.out.println("enter the name of file targeted");
        return reader.readLine();
    }

    private void get(BufferedReader reader) throws IOException {
        System.out.println("enter the pathname for save");
        String pathname = reader.readLine();
        new File(pathname).mkdir();
        System.out.println("enter the name file for save");
        String localFileName = reader.readLine();
        String fileName = getFileName(reader);
        get(pathname,fileName,localFileName);
    }

    private void write(BufferedReader reader) throws IOException {
        String fileName = getFileName(reader);
        write(fileName);

    }

    private void  delete(BufferedReader reader) throws IOException {
        String fileName = getFileName(reader);
        delete(fileName);
    }

    private void create(BufferedReader reader) throws IOException {
        String fileName = getFileName(reader);
        create(fileName);
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

    private boolean requestIsPossible(String request) throws IOException {
        sendRequest(request);
        readResponse();
        System.out.println(msg);
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
        System.out.println("request : " + request);
        writer.flush();
    }

    private void readResponse() throws IOException {
        msg = buffer.readLine();
    }

    private void get(String pathname,String nameFile, String nameFileDst) throws IOException {
        connect(adr,port);
        System.out.println("ini");
        if (isConnect()) {
            System.out.println("co");
            if (requestIsPossible("get " + nameFile)) {
                System.out.println("send");
                long size = takeFileSize();
                System.out.println("size" + size);
                File file = createFileForGet(pathname,nameFileDst);
                System.out.println("avant boucle");
                writeFileForGet(file, size);
                readResponse();
                closeConnection();
            }
        }
    }

    private File createFileForGet(String pathname, String nameFile) throws IOException {
        System.out.println("creation du fichier");
        File file = new File(pathname,nameFile);
        boolean result = file.createNewFile();
        System.out.println("creation : " + result);
        return file;
    }

    private long takeFileSize() throws IOException {
        msg = buffer.readLine();
        return Long.parseLong(msg);
    }

    private void writeFileForGet(File file, long size) throws IOException {
        PrintWriter printWriter = new PrintWriter(file);
        double sum = 0;
        System.out.println(sum);
        while (sum != size) {
            msg = buffer.readLine();
            System.out.println("msg : " + msg);
            sum = calculateReadingProgression(sum,size,msg.length());
            printWriter.println(msg);
            printWriter.flush();
        }

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
            if (requestIsPossible("list")) {
                StringBuilder builder = new StringBuilder();
                while ((msg = buffer.readLine()) != null) {
                    builder.append(msg).append('\n');
                }
                System.out.println("list:\n" + builder.toString());
            }
            closeConnection();
        }
    }

    private void write(String nameFile) throws IOException {
        connect(adr, port);
        if (isConnect()) {
            if (requestIsPossible("write " + nameFile)) {
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
            }
        }
    }

    private void delete(String nameFile) throws IOException {
        connect(adr, port);
        if (isConnect()) {
            if (requestIsPossible("delete " + nameFile)) {
                readResponse();
                System.out.println(msg);
                closeConnection();
            }
        }
    }

    private void create(String nameFile) throws IOException {
        connect(adr, port);
        if (isConnect()) {
            if (requestIsPossible("create " + nameFile)) {
                readResponse();
                System.out.println(msg);
                closeConnection();
            }
        }
    }

}
