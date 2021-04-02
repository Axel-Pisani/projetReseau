package mainMultiServer;

import multiServerMultiClient.MultiServer;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class MainServer4 {
    public static void main(String[] args) throws UnknownHostException {
        int port = 1237;
        MultiServer server = new MultiServer(
                "/home/marius/cours/l3s2/ApRéseau/projetReseau/projet/multi-severs/servers.txt",
                "/home/marius/cours/l3s2/ApRéseau/projetReseau/projet/multi-severs/server4",
                port,
                InetAddress.getByName("localhost"));
        server.start();
    }
}
