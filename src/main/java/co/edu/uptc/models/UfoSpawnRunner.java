package co.edu.uptc.models;

import lombok.Data;

@Data
public class UfoSpawnRunner implements Runnable {
    private UfoGameModel ufoGameModel;
    private int createdUfos;

    public UfoSpawnRunner(UfoGameModel ufoGameModel) {
        this.ufoGameModel = ufoGameModel;
        createdUfos = 0;
    }

    @Override
    public void run() {
        while (createdUfos < ufoGameModel.getNumberofUfos()) {
            ufoGameModel.addUfo(ufoGameModel.getSpeed()); 
            createdUfos++;
            // Todo: ufoGameModel.getPresenter().updateUFOs(); 
            try {
                Thread.sleep(ufoGameModel.getSpawnRate()); 
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}