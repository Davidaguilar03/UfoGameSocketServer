package co.edu.uptc.models;

public class UfoRunner implements Runnable {
    private UfoSocketServer ufoSocketServer;

    public UfoRunner(UfoSocketServer ufoSocketServer) {
        this.ufoSocketServer = ufoSocketServer;
    }

    @Override
    public void run() {
        while (true) {
            ufoSocketServer.moveAll();
            ufoSocketServer.updateUfosOrder();
            try {
                Thread.sleep(20); 
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

