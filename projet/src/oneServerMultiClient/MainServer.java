package oneServerMultiClient;

public class MainServer {
    public static void main(String[] args) {
        int port = 1264;
        Server server = new Server("/home/marius/cours/l3s2/ApRéseau/projetReseau/projet/server",port);
        server.start();
    }
}
