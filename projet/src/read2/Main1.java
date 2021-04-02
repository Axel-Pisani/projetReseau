package read2;

import java.net.InetAddress;

public class Main1 {
    public static void main(String[] args) {
        int port = 1234;
        try {
            System.out.println("ici");
            new ClientRead(InetAddress.getByName("localhost"), port,
                    "/home/marius/cours/l3s2/ApRéseau/projetReseau/projet/clients/jean",
                    "read2.txt");
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("erreur impossible d'ouvrir le client");
            System.exit(1);
        }
    }
}
