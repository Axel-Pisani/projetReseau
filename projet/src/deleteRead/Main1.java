package deleteRead;

import delete2.ClientDelete;

import java.net.InetAddress;

public class Main1 {
    public static void main(String[] args) {
        int port = 1234;
        try {
            new ClientDelete(InetAddress.getByName("localhost"), port,"deleteRead.txt");
            System.exit(0);
        } catch (Exception e) {
            System.err.println("erreur impossible d'ouvrir le client");
            System.exit(1);
        }
    }
}
