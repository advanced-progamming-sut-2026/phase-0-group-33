package server;

import net.Protocol;

public final class ServerMain {

    private ServerMain() {
    }

    public static void main(String[] args) {
        int port = Protocol.DEFAULT_PORT;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                Log.warn("Ignoring '" + args[0] + "' as a port; using " + port + ".");
            }
        }
        GameServer server = new GameServer(port);
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop, "shutdown"));
        server.start();
    }
}
