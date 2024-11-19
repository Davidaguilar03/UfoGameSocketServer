package co.edu.uptc.models;

public class UfoRunner implements Runnable {
    private UfoGameModel ufoGameModel;

    public UfoRunner(UfoGameModel ufoGameModel) {
        this.ufoGameModel = ufoGameModel;
    }

    @Override
    public void run() {
        while (true) {
            ufoGameModel.moveAll(); 
            //Todo: ufoGameModel.getPresenter().updateUFOs(); 
            try {
                Thread.sleep(20); 
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

