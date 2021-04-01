package multiServerMultiClient;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Locale;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MultiServer {
    private ServerSocket serverSocket;
    private String folder;
    private DistantServerFolderManager manager;
    private final ConcurrentLinkedDeque<Server> servers = new ConcurrentLinkedDeque<>();
    private Executor executor;

    MultiServer(String folder, int port, InetAddress address) {

        try {
            serverSocket = new ServerSocket(port, -1, address);
            serverSocket.setReuseAddress(true);
            this.folder = folder;
            this.manager = new DistantServerFolderManager(folder, new Server(address, port));
            executor = Executors.newWorkStealingPool();
            BufferedReader reader = new BufferedReader(
                    new FileReader("/home/marius/cours/l3s2/ApRéseau/projetReseau/projet/multi-severs/servers.txt"));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] info = line.split(",");
                servers.add(new Server(InetAddress.getByName(info[0]), Integer.parseInt(info[1])));
            }
            new Scanner(System.in).nextLine();
            initialization();
            System.out.println("end initialization");

        } catch (Exception e) {
            System.err.println("ERROR : impossible to open this server");
            System.exit(1);
        }
    }

    public void initialization() throws IOException {
        for (Server currentServer : servers) {
            if (currentServer.equals(serverSocket.getInetAddress(), serverSocket.getLocalPort())) {
                System.out.println(currentServer);
                for (String nameFile : manager.getListLocalFile()) {
                    diffusion(nameFile, "SERVER_CREATE");
                }
                endTransmissionFile();
            }
            else{
                boolean finish = false;
                BufferedReader buffer;
                System.out.println("rentre en boucle");
                do{
                    Socket socket = serverSocket.accept();
                    buffer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String msg = buffer.readLine();
                    System.out.println("message : " + msg);
                    if (msg.equals("OK")){
                        finish = true;
                        socket.close();
                    }
                    else {
                        modificationRequestDistantFileSet(msg.split(" "));
                    }
                }while (!finish);
                System.out.println("other");
            }
        }

    }

    private void endTransmissionFile() throws IOException {
        Socket diffusion;
        PrintWriter out;
        for (Server server : servers) {
            if (isDistantServer(server)) {
                diffusion = new Socket(server.getAddress(), server.getPort());
                out = new PrintWriter(diffusion.getOutputStream());
                sendResponse(out, "OK");
                closeConnection(new BufferedReader(new InputStreamReader(diffusion.getInputStream())), out, diffusion);
            }
        }
    }

    public void start() {
        try {
            while (true) {
                executor.execute(new Thread(new Handler(serverSocket.accept())));
            }
        } catch (Exception e) {
            System.err.println("ERROR: abnormal shutdown of server" + serverSocket.toString());
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

    private void sendInvalidResponse(PrintWriter out, String request){
        sendResponse(out,"ERROR: invalid request :\n" + request);
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
            modificationRequestFile(info,out,in);
        } else if (info.length == 4) {
            modificationRequestDistantFileSet(info);
        }else {
            sendInvalidResponse(out,Arrays.toString(info));
        }
    }

    private void closeConnection(BufferedReader in, PrintWriter out, Socket socket) throws IOException {
        System.out.println("requet terminer");
        in.close();
        out.close();
        socket.close();
    }

    private void modificationRequestFile(String[] request,PrintWriter out, BufferedReader in) throws IOException {
        switch (request[0]){
            case "GET":
                get(request[1],out);
                break;
            case "CREATE":
                sendValidResponse(out);
                created(request[1],out);
                break;
            case "WRITE":
                write(request[1],out,in);
                break;
            case "DELETE":
                delete(request[1],out);
                break;
            default:
                sendInvalidResponse(out,Arrays.toString(request));
        }
    }


    private void modificationRequestDistantFileSet(String[] request) {
        switch (request[0]) {
        case "SERVER_CREATE":
            serverCreate(request);
            break;
        case "SERVER_DELETE":
            serverDelete(request);
            break;
        }
    }

    private void serverCreate(String[] msg) {
        manager.addFileForDistantServer(msg[1], msg[2], msg[3]);
    }

    private void serverDelete(String[] msg) {
        manager.removeFile(msg[1]);
    }

    private void list(PrintWriter out) {
        String msg = makeFileList();
        System.out.println(msg);
        out.println(msg);
        out.flush();
    }

    private String makeFileList(){
        StringBuilder stringBuilder = new StringBuilder();
        for (String nameFile : manager.getListALLFile()) {
            stringBuilder.append(nameFile);
            stringBuilder.append('\n');
        }
        if (!(stringBuilder.length() == 0)) {
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        }
        return stringBuilder.toString();
    }

    private void redirection(String nameFile, PrintWriter out) {
        String msg = createMsgRedirection(nameFile);
        System.out.println(msg);
        sendResponse(out,msg);
    }

    private String createMsgRedirection(String nameFile){
        return "REDIRECTION" + " " + manager.getServer(nameFile).getAddress() + " "
                + manager.getServer(nameFile).getPort();
    }

    private void get(String nameFile,PrintWriter out) {
        if (manager.isInFederation(nameFile)) {
            if (manager.isInLocalServer(nameFile)) {
                sendValidResponse(out);
                FileHandle.OperationStatus result = manager.readFile(folder, nameFile, out);
                switch (result) {
                    case ERROR_FILE_DELETED:
                        sendResponse(out, "ERROR: file deleted by another user");
                        break;
                    case ERROR_FILE_STREAM:
                        sendResponse(out, "ERROR: an error occurred while opening the file");
                        break;
                    case ERROR_INTERRUPTED:
                        sendResponse(out, "ERROR: you were interrupted during your expectation");
                        break;
                    case OK:
                        sendValidResponse(out);
                        break;
                }
            }else {
                redirection(nameFile, out);
            }
        } else {
            sendResponse(out,"ERROR: the requested file doesn't exist");
        }
    }

    private void diffusion(String nameFile, String request) throws IOException {
        String msg;
        PrintWriter out;
        for (Server server : servers) {
            if (isDistantServer(server)) {
                System.out.println("diffusion for " + server.getAddress() + ":" + server.getPort());
                Socket diffusion = new Socket(server.getAddress(), server.getPort());
                out = new PrintWriter(diffusion.getOutputStream());
                msg = createDiffusionRequest(nameFile,request);
                sendResponse(out,msg);
                closeConnection(new BufferedReader(new InputStreamReader(diffusion.getInputStream())),out,diffusion);
            }
        }
    }

    private boolean isDistantServer(Server server){
        return !server.equals(serverSocket.getInetAddress(), serverSocket.getLocalPort());
    }

    private String createDiffusionRequest(String nameFile, String request){
        return  request + " " +
                nameFile + " " +
                manager.getLocalServer().getAddress() + " "
                + manager.getLocalServer().getPort();
    }

    private void created(String nameFile, PrintWriter out) throws IOException {
        if (manager.isInFederation(nameFile)) {
            sendResponse(out,"ERROR: the requested file exist");
        } else {
            boolean result = manager.createFile(folder, nameFile);
            if (!result) {
               sendResponse(out,"ERROR: cannot create this file");
            } else {
                diffusion(nameFile, "SERVER_CREATE");
                sendValidResponse(out);
            }
        }
    }

    private void write(String nameFile,PrintWriter out,BufferedReader in) throws IOException {
        if (manager.isInFederation(nameFile)) {
            if (manager.isInLocalServer(nameFile)) {
                sendValidResponse(out);
                FileHandle.OperationStatus result = manager.writeFile(nameFile, in);
                switch (result) {
                    case ERROR_FILE_DELETED:
                        sendResponse(out,"ERROR: file deleted by another user");
                        break;
                    case ERROR_FILE_STREAM:
                        sendResponse(out,"ERROR: an error occurred while opening, writing or closing the file");
                        break;
                    case ERROR_INTERRUPTED:
                        sendResponse(out,"ERROR: you were interrupted during your expectation");
                        break;
                    case ERROR_FILE_DOES_NOT_EXIST:
                        sendResponse(out, "ERROR: the requested file doesn't exist");
                        break;
                    case OK:
                        sendValidResponse(out);
                        break;
                }
            }
            else {
                redirection(nameFile, out);
            }
        } else {
            sendResponse(out,"ERROR: the requested file doesn't exist");
        }
    }

    private void delete(String nameFile, PrintWriter out) throws IOException {
        if (manager.isInFederation(nameFile)) {
            if (manager.isInLocalServer(nameFile)) {
                sendValidResponse(out);
                FileHandle.OperationStatus result = manager.deleteFile(nameFile);
                switch (result) {
                case ERROR_INTERRUPTED:
                    sendResponse(out,"ERROR: you were interrupted during your expectation");
                    break;
                case ERROR_DELETION:
                   sendResponse(out,"ERROR: an error occurred while deleting the file");
                    break;
                case OK:
                    diffusion(nameFile, "SERVER_DELETE");
                    sendValidResponse(out);
                    break;
                }
            } else {
                redirection(nameFile, out);
            }
        } else {
            sendResponse(out,"ERROR: the requested file doesn't exist");
        }
    }
}
