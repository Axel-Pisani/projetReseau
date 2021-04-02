package readDelete;

import read2.ClientRead;

import java.net.InetAddress;

public class Main1 {
    public static void main(String[] args) {
        int port = 1234;
        try {
            new ClientRead(InetAddress.getLocalHost(), port,
                    "/home/marius/cours/l3s2/ApRéseau/projetReseau/projet/clients/paul",
                    "readDelete.txt");
            System.exit(0);
        } catch (Exception e) {
            System.err.println("erreur impossible d'ouvrir le client");
            System.exit(1);
        }
    }
}
