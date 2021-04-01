package oneServerMultiClient;

import comuneCode.ErrorArgs;


import java.net.InetAddress;

public class MainClient {
    public static void main(String[] args) {
        if (args.length != 2) {
            ErrorArgs.error();
        } else {
            int port;
            try {
                port = Integer.parseInt(args[1]);
                InetAddress adr = InetAddress.getByName(args[0]);
                new Client(adr, port);
            } catch (Exception e) {
                ErrorArgs.error();
            }
        }
    }
}
