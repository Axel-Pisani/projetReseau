package multiServerMultiClient;


import comuneCode.ErrorArgs;

import java.net.InetAddress;

public class MainMultiServer1 {
    public static void main(String[] args) {
        if (args.length != 4){
            ErrorArgs.error();
        }
        else {
            String serversFile;
            String folder;
            int port;
            InetAddress adr;
            try {
                serversFile = args[0];
                folder = args[1];
                port = Integer.parseInt(args[2]);
                adr = InetAddress.getByName(args[3]);
                MultiServer server = new MultiServer(serversFile, folder, port, adr);
                server.start();
            }catch (Exception e){
                ErrorArgs.error();
            }

        }
    }
}
