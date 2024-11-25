package co.edu.uptc.models;

import lombok.Data;

@Data
public class UfoSpawnRunner implements Runnable {
    private UfoSocketServer ufoSocketServer;
    private int createdUfos;

    public UfoSpawnRunner(UfoSocketServer ufoGameModel) {
        this.ufoSocketServer = ufoGameModel;
        createdUfos = 0;
    }

    @Override
    public void run() {
        while (createdUfos < ufoSocketServer.getNumberofUfos()) {
            ufoSocketServer.addUfo(ufoSocketServer.getSpeed(), createdUfos);
            createdUfos++;
            ufoSocketServer.updateUfosOrder();
            try {
                Thread.sleep(ufoSocketServer.getSpawnRate());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}