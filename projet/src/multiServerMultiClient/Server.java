package multiServerMultiClient;

import java.net.InetAddress;
import java.util.Objects;

public class Server {
    private InetAddress address;
    private int port;

    public Server(InetAddress address, int port) {
        this.address = address;
        this.port = port;
    }

    public InetAddress getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Server server = (Server) o;
        return port == server.port && Objects.equals(address, server.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address, port);
    }
}
