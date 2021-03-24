package multiServerMultiClient;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


public class DistantServerFolderManager {
    private final ConcurrentHashMap<String, FileHandle> fileMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String,Server> serverMap = new ConcurrentHashMap<>();
    private final Server localServer;

    public DistantServerFolderManager(String folder, Server server) {
        String[] arrayNameFile = new File(folder).list();
        localServer = server;
        try {
            if (arrayNameFile == null){
                throw new NullPointerException();
            }
            for (String nameFile : arrayNameFile) {
                fileMap.put(nameFile, new FileHandle(new File(folder, nameFile)));
                serverMap.put(nameFile,localServer);
            }
        }
        catch (NullPointerException e){
            System.err.println("ERROR: abstract pathname doesn't denote a directory");
            System.exit(-1);
        }
    }


    public Server getServer(String nameFile){
        return serverMap.get(nameFile);
    }

    public void addFile(String nameFile, String adr, String port) throws UnknownHostException {
        serverMap.put(nameFile,new Server(InetAddress.getByName(adr),Integer.parseInt(port)));
    }

    public void removeFile(String nameFile) {
        serverMap.remove(nameFile);
    }

    public boolean isInFolder(String nameFile){
        return fileMap.containsKey(nameFile);
    }

    public boolean isInServer(String nameFile){
        return serverMap.get(nameFile).equals(localServer);
    }

    public FileHandle.OperationStatus readFile(String nameFile, Socket socket) throws IOException {
        return fileMap.get(nameFile).readFile(new PrintWriter(socket.getOutputStream()));
    }

    public FileHandle.OperationStatus writeFile(String nameFile, Socket socket) throws IOException{
        return fileMap.get(nameFile).replaceFile(new Scanner(socket.getInputStream()));
    }

    public boolean createFile(String folder, String nameFile) throws IOException {
        File file = new File(folder,nameFile);
        if (!file.createNewFile()){
            return false;
        }
        fileMap.put(nameFile,new FileHandle(file));
        serverMap.put(nameFile,localServer);
        return true;
    }

    public Server getLocalServer() {
        return localServer;
    }

    public FileHandle.OperationStatus deleteFile(String fileName){
        return fileMap.get(fileName).delete();
    }

    public Set<String> getListLocalFile(){
        return fileMap.keySet();
    }

    public Set<String> getListALLFile(){
        return serverMap.keySet();
    }
}
