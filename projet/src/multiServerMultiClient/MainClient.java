package multiServerMultiClient;

import comuneCode.ErrorArgs;

import java.net.InetAddress;

public class MainClient {
    public static void main(String[] args) {
        if (args.length != 4) {
            ErrorArgs.error();
        } else {
            int port;
            InetAddress adr;
            try {
                port = Integer.parseInt(args[1]);
                adr = InetAddress.getByName(args[0]);
                new ClientForMultiServer(adr, port);
            } catch (Exception e) {
                ErrorArgs.error();
            }
        }
    }
}
