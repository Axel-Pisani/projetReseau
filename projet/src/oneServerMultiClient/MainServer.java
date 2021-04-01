package oneServerMultiClient;

import comuneCode.ErrorArgs;


public class MainServer {
    public static void main(String[] args) {
        if (args.length != 2){
            ErrorArgs.error();
        }
        else {
            int port;
            try {
                port = Integer.parseInt(args[1]);
                String folder = args[0];
                Server server =
                        new Server(folder, port);
                server.start();
            }catch (Exception e){
                ErrorArgs.error();
            }

        }
    }
}
