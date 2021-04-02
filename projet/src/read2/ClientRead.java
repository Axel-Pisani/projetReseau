package read2;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.Locale;
import java.util.Scanner;

public class ClientRead {
    private Socket socket;
    private PrintWriter writer;
    private BufferedReader buffer;
    private final InetAddress adr;
    private String msg;
    private final int port;
    private final DecimalFormat df = new DecimalFormat("0.00");

    public ClientRead(InetAddress adr, int port,String pathname, String nameFile) throws IOException {
        this.adr = adr;
        this.port = port;
        get(pathname, nameFile, "copy" + nameFile);
    }


    private boolean request(String request) throws IOException {
        sendRequest(request);
        readResponse();
        if (isRedirection(msg)){
            redirection(request,msg);
        }
        return requestIsPossible();
    }


    private void get(String pathname,String nameFile,String fileNameDst) throws IOException {
        connect(adr,port);
        if (isConnect()) {
            if (request("get " + nameFile)) {
                System.out.println("make enter");
                new Scanner(System.in).nextLine();
                long size = takeFileSize();
                File file = createFileForGet(pathname,fileNameDst);
                writeFileForGet(file, size);
                readResponse();
                closeConnection();
            }
        }
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

    private File createFileForGet(String pathname, String fileName) throws IOException {
        File file = new File(pathname,fileName);
        file.createNewFile();
        return file;
    }

    private long takeFileSize() throws IOException {
        msg = buffer.readLine();
        return Long.parseLong(msg);
    }

    private void writeFileForGet(File file, long size) throws IOException {
        PrintWriter printWriter = new PrintWriter(file);
        double sum = 0;
        System.out.println(size);
        while ( sum < size) {
            msg = buffer.readLine();
            System.out.println(msg);
            System.out.println(sum);
            sum = calculateReadingProgression(sum,size,msg.length());
            printWriter.println(msg);
            printWriter.flush();
        }
    }

    private double calculateReadingProgression(double sum, long size, int add){
        sum += add + 1;
        if (sum > size){
            sum -= 1;
        }
        System.out.println(df.format(sum / size * 100.0) + "%");
        return sum;
    }
}
