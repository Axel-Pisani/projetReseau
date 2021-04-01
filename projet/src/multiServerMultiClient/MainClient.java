package multiServerMultiClient;

import java.net.InetAddress;

public class MainClient {
    public static void main(String[] args) {
        int port = 1235;
        try {
            new ClientForMultiServer(InetAddress.getByName("localhost"),port,"paul");
            System.exit(0);
        }catch (Exception e){
            System.err.println("erreur impossible d'ouvrir le client");
            System.exit(1);
        }
    }
}
