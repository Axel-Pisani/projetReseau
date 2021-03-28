package multiServerMultiClient;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class MainMultiServer2 {
    public static void main(String[] args) throws UnknownHostException {
        int port = 1235;
        MultiServer server = new MultiServer("/home/marius/cours/l3s2/ApRéseau/projetReseau/projet/multi-severs/server2",port,
                InetAddress.getByName("localhost"));
        server.start();
    }
}
