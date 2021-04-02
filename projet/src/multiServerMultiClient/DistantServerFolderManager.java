package multiServerMultiClient;

import comuneCode.FileHandle;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


public class DistantServerFolderManager {
    private final ConcurrentHashMap<String, FileHandle> fileMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Server> serverMap = new ConcurrentHashMap<>();
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

    public void addFileForDistantServer(String nameFile, String adr, String port){
        System.out.println("addFile :" + nameFile);
        try {
            serverMap.put(nameFile, new Server(InetAddress.getByName(adr.split("/")[0]), Integer.parseInt(port)));
        }catch (UnknownHostException e){
            System.err.println("ERROR");
            System.err.println(adr);
        }
    }

    public void removeFile(String nameFile) {
        serverMap.remove(nameFile);
    }

    public boolean isInLocalServer(String nameFile){
        return fileMap.containsKey(nameFile);
    }

    public boolean isInFederation(String nameFile){
        return serverMap.containsKey(nameFile);
    }

    public FileHandle.OperationStatus readFile(String folder, String nameFile, PrintWriter out) {
        long size = new File(folder, nameFile).length();
        out.println(size);
        System.out.println("size " + size);
        out.flush();
        return fileMap.get(nameFile).readFile(out);
    }

    public FileHandle.OperationStatus writeFile(String nameFile, BufferedReader in) throws IOException{
        return fileMap.get(nameFile).replaceFile(new Scanner(in));
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
        FileHandle fileDelete = fileMap.remove(fileName);
        return fileDelete.delete();
    }

    public Set<String> getListLocalFile(){
        return fileMap.keySet();
    }

    public Set<String> getListALLFile(){
        return serverMap.keySet();
    }
}
