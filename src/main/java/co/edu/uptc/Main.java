package co.edu.uptc;

import co.edu.uptc.models.UfoSocketServer;

public class Main {
    public static void main(String[] args) {
        // if (args.length != 1) {
        //     System.out.println("Uso: java Main <puerto>");
        //     System.exit(1);
        // }

        // int port;
        // try {
        //     port = Integer.parseInt(args[0]);
        // } catch (NumberFormatException e) {
        //     System.out.println("Número de puerto inválido.");
        //     System.exit(1);
        //     return; 
        // }

        UfoSocketServer server = new UfoSocketServer();
        server.startServer(1234);
    }
}
