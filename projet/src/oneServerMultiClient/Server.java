package oneServerMultiClient;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Locale;

public class Server {
    private  ServerSocket serverSocket;
    private  String folder;
    private  ServerFolderManager manager;

    Server(String folder, int port){
        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setReuseAddress(true);
            this.folder = folder;
            manager = new ServerFolderManager(folder);
        }catch (Exception e){
            System.err.println("impossible d'ouvrir le serveur");
            System.exit(1);
        }
    }

    public void start(){
        try {
            while (true) {
                new Thread(new Handler(serverSocket.accept())).start();
            }
        }catch (Exception e){
            System.out.println("Arrêt anormal du serveur.");
        }
    }

    class Handler implements Runnable{

        private final Socket socket;
        private final OutputStream out;
        private final InputStream in;


        Handler(Socket socket) throws IOException {
            this.socket = socket;
            out = socket.getOutputStream();
            in = socket.getInputStream();
        }

        @Override
        public void run() {
            byte[] buffer = new byte[2048];
            try {
                out.write(("connection\n").getBytes());
                int length = in.read(buffer);
                String[] msg = Translate.translateByteInString(buffer,length).split(" ");
                msg[0] = msg[0].toUpperCase(Locale.ROOT);
                System.out.println(msg[0]);
                if (msg.length == 1 && msg[0].equals("LIST")){
                    list(socket);
                }
                else if(msg.length == 2) {
                    modificationRequest(socket,msg);
                }
                in.close();
                out.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void modificationRequest(Socket socket, String[] request) throws IOException {
        switch (request[0]) {
            case "GET" -> get(request[1], socket);
            case "CREATE" -> created(request[1], socket);
            case "WRITE" -> write(request[1], socket);
            case "DELETE" -> delete(request[1], socket);
        }
    }


    private void list(Socket socket) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        for (String nameFile : manager.getListFile()){
            stringBuilder.append(nameFile);
            stringBuilder.append('\n');
        }
        if (!stringBuilder.isEmpty()) {
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        }
        socket.getOutputStream().write(stringBuilder.toString().getBytes());
        System.out.println(stringBuilder.toString());
    }

    private void get(String nameFile, Socket socket) throws IOException{
        if (manager.isInFolder(nameFile)) {
            FileHandle.OperationStatus result = manager.readFile(nameFile, socket);
            switch (result) {
                case ERROR_FILE_DELETED -> socket.getOutputStream().write(
                        "ERROR: file deleted by another user".getBytes());
                case ERROR_FILE_STREAM -> socket.getOutputStream().write(
                        "ERROR: an error occurred while opening, writing or closing the file".getBytes());
                case ERROR_INTERRUPTED -> socket.getOutputStream().write(
                        "ERROR: you were interrupted during your expectation".getBytes());
            }
        }
        else {
            socket.getOutputStream().write("ERROR: the requested file doesn't exist".getBytes());
        }
    }

    private void created(String nameFile, Socket socket) throws IOException {
        if (manager.isInFolder(nameFile)){
            socket.getOutputStream().write(
                    "ERROR: the requested file exist".getBytes());
        }
        else {
            boolean result = manager.createFile(folder, nameFile);
            if (!result) {
                socket.getOutputStream().write("ERROR: cannot create this file".getBytes());
            }
        }
    }

    private void write(String nameFile,Socket socket) throws IOException {
        FileHandle.OperationStatus result = manager.writeFile(nameFile, socket);
        switch (result){
            case ERROR_FILE_DELETED ->
                    socket.getOutputStream().write("ERROR: file deleted by another user".getBytes());
            case ERROR_FILE_STREAM ->
                    socket.getOutputStream().write(
                            "ERROR: an error occurred while opening, writing or closing the file".getBytes());
            case ERROR_INTERRUPTED ->
                    socket.getOutputStream().write("ERROR: you were interrupted during your expectation".getBytes());
            case ERROR_FILE_DOES_NOT_EXIST ->
                    socket.getOutputStream().write("ERROR: the requested file doesn't exist".getBytes());
        }
    }

    private void delete(String nameFile, Socket socket) throws IOException{
        if (manager.isInFolder(nameFile)) {
            FileHandle.OperationStatus result = manager.deleteFile(nameFile);
            switch (result) {
                case ERROR_INTERRUPTED -> socket.getOutputStream().write(
                        "ERROR: you were interrupted during your expectation".getBytes());
                case ERROR_DELETION -> socket.getOutputStream().write(
                        "ERROR: an error occurred while deleting the file".getBytes());
            }
        }
        else {
            socket.getOutputStream().write("ERROR: the requested file doesn't exist".getBytes());
        }
    }
}
