package readDelete;

import delete2.ClientDelete;

import java.net.InetAddress;

public class Main2 {
    public static void main(String[] args) {
        int port = 1234;
        try {
            new ClientDelete(InetAddress.getByName("localhost"), port,"readDelete.txt");
            System.exit(0);
        } catch (Exception e) {
            System.err.println("erreur impossible d'ouvrir le client");
            System.exit(1);
        }
    }
}
