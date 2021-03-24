package multiServerMultiClient;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.concurrent.ConcurrentLinkedDeque;

public class MultiServer {
    private ServerSocket serverSocket;
    private String folder;
    private DistantServerFolderManager manager;
    private final ConcurrentLinkedDeque<Server> servers = new ConcurrentLinkedDeque<>();

    MultiServer(String folder, int port, InetAddress address) {
        try {
            serverSocket = new ServerSocket(port, -1, address);
            serverSocket.setReuseAddress(true);
            this.folder = folder;
            this.manager = new DistantServerFolderManager(folder, new Server(address, port));
            BufferedReader reader = new BufferedReader(
                    new FileReader("/home/marius/cours/l3s2/ApRéseau/projet/multi-severs/servers"));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] info = line.split(",");
                if (InetAddress.getByName(info[0]).equals(address) && info[1].equals(String.valueOf(port)))
                    servers.add(new Server(InetAddress.getByName(info[0]), Integer.parseInt(info[1])));
                initialization();
            }

        } catch (Exception e) {
            System.err.println("impossible d'ouvrir le serveur");
            System.exit(1);
        }
    }

    private void initialization() throws IOException {
        for (String nameFile : manager.getListLocalFile()) {
            diffusion(null, nameFile, "SERVER_CREATE");
        }
    }

    public void start() {
        try {
            while (true) {
                new Thread(new Handler(serverSocket.accept())).start();
            }
        } catch (Exception e) {
            System.out.println("Arrêt anormal du serveur.");
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
        socket.getOutputStream().write(stringBuilder.toString().getBytes());
        System.out.println(stringBuilder.toString());
    }

    private void redirection(String nameFile, Socket socket) throws IOException {
        String msg = "REDIRECTION" + " " + manager.getServer(nameFile).getAddress() + " "
                + manager.getServer(nameFile).getPort();
        socket.getOutputStream().write(msg.getBytes());
    }

    private void get(String nameFile, Socket socket) throws IOException {
        if (manager.isInServer(nameFile)) {
            if (manager.isInFolder(nameFile)) {
                FileHandle.OperationStatus result = manager.readFile(nameFile, socket);
                switch (result) {
                case ERROR_FILE_DELETED:
                    socket.getOutputStream().write("ERROR: file deleted by another user".getBytes());
                    break;
                case ERROR_FILE_STREAM:
                    socket.getOutputStream().write("ERROR: an error occurred while opening the file".getBytes());
                    break;
                case ERROR_INTERRUPTED:
                    socket.getOutputStream().write("ERROR: you were interrupted during your expectation".getBytes());
                    break;
                case OK:
                    socket.getOutputStream().write("ok".getBytes(StandardCharsets.UTF_8));
                    break;
                }
                socket.getOutputStream().flush();
            } else {
                redirection(nameFile, socket);
            }
        } else {
            socket.getOutputStream().write("ERROR: the requested file doesn't exist".getBytes());
        }
    }

    private void diffusion(Socket socket, String nameFile, String request) throws IOException {
        String msg;
        for (Server server : servers) {
            Socket diffusion = new Socket(server.getAddress(), server.getPort());
            msg = request + " " + nameFile + " " + manager.getLocalServer().getAddress() + " "
                    + manager.getLocalServer().getPort();
            diffusion.getOutputStream().write(msg.getBytes());
            diffusion.getOutputStream().flush();
            diffusion.getOutputStream().close();
            diffusion.close();
            if (socket != null) {
                socket.getOutputStream().write("ok".getBytes());
            }
        }
    }

    private void created(String nameFile, Socket socket) throws IOException {
        if (manager.isInServer(nameFile)) {
            socket.getOutputStream().write("ERROR: the requested file exist".getBytes());
        } else {
            boolean result = manager.createFile(folder, nameFile);
            if (!result) {
                socket.getOutputStream().write("ERROR: cannot create this file".getBytes());
            } else {
                diffusion(socket, nameFile, "SERVER_CREATE");
            }
        }
    }

    private void write(String nameFile, Socket socket) throws IOException {
        if (manager.isInServer(nameFile)) {
            FileHandle.OperationStatus result = manager.writeFile(nameFile, socket);
            switch (result) {
            case ERROR_FILE_DELETED:
                socket.getOutputStream().write("ERROR: file deleted by another user".getBytes());
                break;
            case ERROR_FILE_STREAM:
                socket.getOutputStream()
                        .write("ERROR: an error occurred while opening, writing or closing the file".getBytes());
                break;
            case ERROR_INTERRUPTED:
                socket.getOutputStream().write("ERROR: you were interrupted during your expectation".getBytes());
                break;
            case ERROR_FILE_DOES_NOT_EXIST:
                redirection(nameFile, socket);
                break;
            case OK:
                socket.getOutputStream().write("ok".getBytes());
                break;
            }
        } else {
            socket.getOutputStream().write("ERROR: the requested file doesn't exist".getBytes());
        }
    }

    private void delete(String nameFile, Socket socket) throws IOException {
        if (manager.isInServer(nameFile)) {
            if (manager.isInFolder(nameFile)) {
                FileHandle.OperationStatus result = manager.deleteFile(nameFile);
                switch (result) {
                case ERROR_INTERRUPTED:
                    socket.getOutputStream().write("ERROR: you were interrupted during your expectation".getBytes());
                    break;
                case ERROR_DELETION:
                    socket.getOutputStream().write("ERROR: an error occurred while deleting the file".getBytes());
                    break;
                case OK:
                    diffusion(socket, nameFile, "SERVER_DELETE");
                    break;
                }
            } else {
                redirection(nameFile, socket);
            }
        } else {
            socket.getOutputStream().write("ERROR: the requested file doesn't exist".getBytes());
        }
    }
}
