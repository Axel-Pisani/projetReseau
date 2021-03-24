package multiServerMultiClient;

import java.net.InetAddress;

public class MainClient {
    public static void main(String[] args) {
        int port = 1234;
        try {
            new ClientForMultiServer(InetAddress.getLocalHost(),port,"jean");
            System.exit(0);
        }catch (Exception e){
            System.err.println("erreur impossible d'ouvrir le client");
            System.exit(1);
        }
    }
}
