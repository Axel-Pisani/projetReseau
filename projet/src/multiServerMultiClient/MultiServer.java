package multiServerMultiClient;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
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
                    new FileReader("/home/marius/cours/l3s2/ApRéseau/projet/multi-severs/servers"));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] info = line.split(",");
                servers.add(new Server(InetAddress.getByName(info[0]), Integer.parseInt(info[1])));
            }
            new Scanner(System.in).nextLine();
            initialization();

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
                do{
                    Socket socket = serverSocket.accept();
                    byte[] buffer = new byte[2048];
                    String msg = Translate.translateByteInString(buffer,socket.getInputStream().read(buffer));
                    if (msg.equals("FINISH")){
                        finish = true;
                    }
                    else {
                        modificationRequestDistantFileSet(msg.split(" "));
                    }
                }while (!finish);
            }
        }

    }

    private void endTransmissionFile() throws IOException {
        for (Server server : servers) {
            Socket diffusion = new Socket(server.getAddress(), server.getPort());
            diffusion.getOutputStream().write("FINISH".getBytes());
            diffusion.getOutputStream().flush();
            diffusion.getOutputStream().close();
            diffusion.close();
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
                socket.getOutputStream().write("connected".getBytes());
                int length = in.read(buffer);
                String[] msg = Translate.translateByteInString(buffer, length).split(" ");
                msg[0] = msg[0].toUpperCase(Locale.ROOT);
                System.out.println(msg[0]);
                if (msg.length == 1 && msg[0].equals("LIST")) {
                    list(socket);
                } else if (msg.length == 2) {
                    modificationRequestFile(socket, msg);
                } else if (msg.length == 4) {
                    modificationRequestDistantFileSet(msg);
                }
                in.close();
                out.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void modificationRequestFile(Socket socket, String[] msg) throws IOException {
        switch (msg[0]) {
        case "GET":
            get(msg[1], socket);
            break;
        case "CREATE":
            created(msg[1], socket);
            break;
        case "WRITE":
            write(msg[1], socket);
            break;
        case "DELETE":
            delete(msg[1], socket);
            break;
        }
    }

    private void modificationRequestDistantFileSet(String[] msg) throws IOException {
        switch (msg[0]) {
        case "SERVER_CREATE":
            serverCreate(msg);
            break;
        case "SERVER_DELETE":
            serverDelete(msg);
            break;
        }
    }

    private void serverCreate(String[] msg) throws UnknownHostException {
        manager.addFile(msg[1], msg[2], msg[3]);
    }

    private void serverDelete(String[] msg) {
        manager.removeFile(msg[1]);
    }

    private void list(Socket socket) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        for (String nameFile : manager.getListALLFile()) {
            stringBuilder.append(nameFile);
            stringBuilder.append('\n');
        }
        if (!(stringBuilder.length() == 0)) {
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        }
        System.out.println(stringBuilder.toString());
        socket.getOutputStream().write(stringBuilder.toString().getBytes());
        socket.getOutputStream().flush();
    }

    private void redirection(String nameFile, Socket socket) throws IOException {
        String msg = "REDIRECTION" + " " + manager.getServer(nameFile).getAddress() + " "
                + manager.getServer(nameFile).getPort();
        socket.getOutputStream().write(msg.getBytes());
    }

    private void get(String nameFile, Socket socket) throws IOException {
        socket.getOutputStream().flush();
        if (manager.isInServers(nameFile)) {
            if (manager.isInFolder(nameFile)) {
                socket.getOutputStream().write("ok to start the transmission\n".getBytes());
                socket.getOutputStream().flush();
                FileHandle.OperationStatus result = manager.readFile(folder,nameFile, socket);
                switch (result) {
                case ERROR_FILE_DELETED:
                    socket.getOutputStream().write("ERROR: file deleted by another user".getBytes());
                    socket.getOutputStream().flush();
                    break;
                case ERROR_FILE_STREAM:
                    socket.getOutputStream().write("ERROR: an error occurred while opening the file".getBytes());
                    socket.getOutputStream().flush();
                    break;
                case ERROR_INTERRUPTED:
                    socket.getOutputStream().write("ERROR: you were interrupted during your expectation".getBytes());
                    socket.getOutputStream().flush();
                    break;
                }
                socket.getOutputStream().flush();
            } else {
                redirection(nameFile, socket);
            }
        } else {
            socket.getOutputStream().write("ERROR: the requested file doesn't exist".getBytes());
            socket.getOutputStream().flush();
        }
    }

    private void diffusion(String nameFile, String request) throws IOException {
        String msg;
        for (Server server : servers) {
            if (!server.equals(serverSocket.getInetAddress(), serverSocket.getLocalPort())) {
                Socket diffusion = new Socket(server.getAddress(), server.getPort());
                msg = request + " " + nameFile + " " + manager.getLocalServer().getAddress() + " "
                        + manager.getLocalServer().getPort();
                System.out.println(msg);
                System.out.println(diffusion.getInetAddress() + ":" + diffusion.getPort());
                diffusion.getOutputStream().write(msg.getBytes());
                diffusion.getOutputStream().flush();
                diffusion.getOutputStream().close();
                diffusion.close();
            }
        }
    }

    private void created(String nameFile, Socket socket) throws IOException {
        if (manager.isInServers(nameFile)) {
            socket.getOutputStream().write("ERROR: the requested file exist".getBytes());
            socket.getOutputStream().flush();
        } else {
            boolean result = manager.createFile(folder, nameFile);
            if (!result) {
                socket.getOutputStream().write("ERROR: cannot create this file".getBytes());
                socket.getOutputStream().flush();
            } else {
                diffusion(nameFile, "SERVER_CREATE");
            }
        }
    }

    private void write(String nameFile, Socket socket) throws IOException {
        if (manager.isInServers(nameFile)) {
            if (manager.isInFolder(nameFile)) {
                FileHandle.OperationStatus result = manager.writeFile(nameFile, socket);
                switch (result) {
                    case ERROR_FILE_DELETED:
                        socket.getOutputStream().write("ERROR: file deleted by another user".getBytes());
                        socket.getOutputStream().flush();
                        break;
                    case ERROR_FILE_STREAM:
                        socket.getOutputStream()
                                .write("ERROR: an error occurred while opening, writing or closing the file".getBytes());
                        socket.getOutputStream().flush();
                        break;
                    case ERROR_INTERRUPTED:
                        socket.getOutputStream().write("ERROR: you were interrupted during your expectation".getBytes());
                        socket.getOutputStream().flush();
                        break;
                    case ERROR_FILE_DOES_NOT_EXIST:
                        redirection(nameFile, socket);
                        break;
                }
            }
            else {
                redirection(nameFile, socket);
            }
        } else {
            socket.getOutputStream().write("ERROR: the requested file doesn't exist".getBytes());
            socket.getOutputStream().flush();
        }
    }

    private void delete(String nameFile, Socket socket) throws IOException {
        if (manager.isInServers(nameFile)) {
            if (manager.isInFolder(nameFile)) {
                FileHandle.OperationStatus result = manager.deleteFile(nameFile);
                switch (result) {
                case ERROR_INTERRUPTED:
                    socket.getOutputStream().write("ERROR: you were interrupted during your expectation".getBytes());
                    socket.getOutputStream().flush();
                    break;
                case ERROR_DELETION:
                    socket.getOutputStream().write("ERROR: an error occurred while deleting the file".getBytes());
                    socket.getOutputStream().flush();
                    break;
                case OK:
                    diffusion(nameFile, "SERVER_DELETE");
                    socket.getOutputStream().write("successful deletion".getBytes());
                    socket.getOutputStream().flush();
                    break;
                }
            } else {
                redirection(nameFile, socket);
            }
        } else {
            socket.getOutputStream().write("ERROR: the requested file doesn't exist".getBytes());
            socket.getOutputStream().flush();
        }
    }
}
