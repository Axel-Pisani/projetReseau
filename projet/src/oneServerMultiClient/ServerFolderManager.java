package oneServerMultiClient;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


public class ServerFolderManager {
    private final ConcurrentHashMap<String, FileHandle> map = new ConcurrentHashMap<>();

    public ServerFolderManager(String folder) {
        String[] arrayNameFile = new File(folder).list();
        try {
            if (arrayNameFile == null){
                throw new NullPointerException();
            }
            for (String nameFile : arrayNameFile) {
                map.put(nameFile, new FileHandle(new File(folder, nameFile)));
            }
        }
        catch (NullPointerException e){
            System.err.println("ERROR: abstract pathname doesn't denote a directory");
            System.exit(1);
        }
    }

    public boolean isInFolder(String nameFile){
        return map.containsKey(nameFile);
    }

    public FileHandle.OperationStatus readFile(String nameFile, Socket socket) throws IOException {
        return map.get(nameFile).readFile(new PrintWriter(socket.getOutputStream()));
    }

    public FileHandle.OperationStatus writeFile(String nameFile, Socket socket) throws IOException{
        return map.get(nameFile).replaceFile(new Scanner(socket.getInputStream()));
    }

    public boolean createFile(String folder, String nameFile) throws IOException {
        File file = new File(folder,nameFile);
        if (!file.createNewFile()){
            return false;
        }
        ;
        map.put(nameFile,new FileHandle(file));
        return true;
    }

    public FileHandle.OperationStatus deleteFile(String fileName){
        return map.get(fileName).delete();
    }

    public Set<String> getListFile(){
        return map.keySet();
    }
}
