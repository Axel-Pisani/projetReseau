package multiServerMultiClient;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class MainMultiServer {
    public static void main(String[] args) throws UnknownHostException {
        int port = 1234;
        MultiServer server = new MultiServer("/home/marius/cours/l3s2/ApRéseau/projet/server",port,
                InetAddress.getByName("localhost"));
        server.start();
    }
}
