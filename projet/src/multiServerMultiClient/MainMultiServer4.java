package multiServerMultiClient;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class MainMultiServer4 {
    public static void main(String[] args) throws IOException {
        int port = 1237;
        MultiServer server = new MultiServer("/home/marius/cours/l3s2/ApRéseau/projetReseau/projet/multi-severs/server4",port,
                InetAddress.getByName("localhost"));
        server.start();
    }
}
