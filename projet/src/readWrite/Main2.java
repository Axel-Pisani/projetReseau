package readWrite;

import Write2.ClientWrite;

import java.net.InetAddress;

public class Main2 {
    public static void main(String[] args) {
        int port = 1234;
        try {
            new ClientWrite(InetAddress.getByName("localhost"), port,"readWrite.txt");
            System.exit(0);
        } catch (Exception e) {
            System.err.println("erreur impossible d'ouvrir le client");
            System.exit(1);
        }
    }
}
