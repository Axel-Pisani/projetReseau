package oneServerMultiClient;

import comuneCode.FileHandle;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class Server {
    private ServerSocket serverSocket;
    private String folder;
    private ServerFolderManager manager;
    private Executor executor;

    Server(String folder, int port) {
        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setReuseAddress(true);
            this.folder = folder;
            manager = new ServerFolderManager(folder);
            executor = Executors.newWorkStealingPool();
        } catch (Exception e) {
            System.err.println("ERROR : impossible to open the sever");
            System.exit(1);
        }
    }

    public void start() {
        try {
            while (true) {
                executor.execute(new Thread(new Handler(serverSocket.accept())));
            }
        } catch (Exception e) {
            System.out.println("WARNING : abnormal shutdown");
        }
    }

    class Handler implements Runnable {

        private final Socket socket;
        private final PrintWriter out;
        private final BufferedReader in;



        Handler(Socket socket) throws IOException {
            this.socket = socket;
            out = new PrintWriter(socket.getOutputStream());
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }


        @Override
        public void run() {
            try {
                acceptConnection(out);
                String[] request = recoversRequest(in);
                managedRequest(request,out,in);
                closeConnection(in,out,socket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String[] recoversRequest(BufferedReader in) throws IOException {
        String msg = in.readLine();
        System.out.println("reçut " + msg);
        return extractRequest(msg);
    }

    private void acceptConnection(PrintWriter out){
        sendResponse(out,"connection");
    }

    private String[] extractRequest(String msg){
        String[] info = msg.split(" ");
        info[0] = info[0].toUpperCase(Locale.ROOT);
        System.out.println("info " + Arrays.toString(info));
        return info;
    }

    private void sendResponse(PrintWriter out, String response){
        System.out.println("reponse " + response);
        out.println(response);
        out.flush();
    }

    private void sendInvalidResponse(PrintWriter out){
        sendResponse(out,"ERROR: invalid request");
    }

    private void sendValidResponse(PrintWriter out){
        sendResponse(out,"OK");
    }

    private void managedRequest(String[] info, PrintWriter out, BufferedReader in) throws IOException {
        System.out.println("requet " + Arrays.toString(info));
        if (info.length == 1 && info[0].equals("LIST")) {
            System.out.println("c'est une liste ");
            sendValidResponse(out);
            list(out);
        } else if (info.length == 2) {
            modificationRequest(out,in, info);
        } else {
            sendInvalidResponse(out);
        }
    }

    private void closeConnection(BufferedReader in, PrintWriter out, Socket socket) throws IOException {
        System.out.println("requet terminer");
        in.close();
        out.close();
        socket.close();
    }

    private void modificationRequest(PrintWriter out,BufferedReader in, String[] request) throws IOException {
        switch (request[0]) {
            case "GET":
                sendValidResponse(out);
                get(request[1],out);
                break;
            case "CREATE":
                sendValidResponse(out);
                created(request[1],out);
                break;
            case "WRITE":
                sendValidResponse(out);
                write(request[1],out,in);
                break;
            case "DELETE":
                sendValidResponse(out);
                delete(request[1],out);
                break;
            default:
                sendInvalidResponse(out);
        }
    }

    private void list(PrintWriter out) {
        String msg = makeFileList();
        System.out.println("list " + msg);
        sendResponse(out,msg);
    }

    private String makeFileList(){
        StringBuilder stringBuilder = new StringBuilder();
        for (String nameFile : manager.getListFile()) {
            stringBuilder.append(nameFile);
            stringBuilder.append('\n');
        }
        if (!(stringBuilder.length() == 0)) {
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        }
        return stringBuilder.toString();
    }

    private void get(String nameFile, PrintWriter out) throws IOException {
        System.out.println("c'est un get");
        if (manager.isInFolder(nameFile)) {
            FileHandle.OperationStatus result = manager.readFile(folder,nameFile, out);
            switch (result) {
                case ERROR_FILE_DELETED:
                    sendResponse(out, "ERROR: file deleted by another user");
                    break;
                case ERROR_FILE_STREAM:
                    sendResponse(out, "ERROR: an error occurred while opening, writing or closing the file");
                    break;
                case ERROR_INTERRUPTED:
                    sendResponse(out, "ERROR: you were interrupted during your expectation");
                    break;
                case OK:
                    sendValidResponse(out);
                    break;
            }
        } else {
            sendResponse(out,"ERROR: the requested file doesn't exist");
        }
    }

    private void created(String nameFile, PrintWriter out) throws IOException {
        if (manager.isInFolder(nameFile)) {
            sendResponse(out,"ERROR: the requested file exist");
        } else {
            boolean result = manager.createFile(folder, nameFile);
            if (!result) {
                sendResponse(out,"ERROR: cannot create this file");
            }
            else {
                System.out.println("ici");
                sendValidResponse(out);
            }
        }
    }

    private void write(String nameFile, PrintWriter out, BufferedReader in) throws IOException {
        FileHandle.OperationStatus result = manager.writeFile(nameFile,new BufferedReader(in));
        switch (result) {
            case ERROR_FILE_DELETED:
                sendResponse(out, "ERROR: file deleted by another user");
                break;
            case ERROR_FILE_STREAM:
                sendResponse(out, "ERROR: an error occurred while opening, writing or closing the file");
                break;
            case ERROR_INTERRUPTED:
                sendResponse(out, "ERROR: you were interrupted during your expectation");
                break;
            case ERROR_FILE_DOES_NOT_EXIST:
                sendResponse(out, "ERROR: the requested file doesn't exist");
                break;
            case OK:
                sendValidResponse(out);
                break;
        }
    }

    private void delete(String nameFile, PrintWriter out) {
        if (manager.isInFolder(nameFile)) {
            FileHandle.OperationStatus result = manager.deleteFile(nameFile);
            switch (result) {
                case ERROR_INTERRUPTED:
                    sendResponse(out, "ERROR: you were interrupted during your expectation");
                    break;
                case ERROR_DELETION:
                    sendResponse(out, "ERROR: an error occurred while deleting the file");
                    break;
                case OK:
                    sendValidResponse(out);
                    break;
            }
        } else {
            sendResponse(out,"ERROR: the requested file doesn't exist");
        }
    }
}
