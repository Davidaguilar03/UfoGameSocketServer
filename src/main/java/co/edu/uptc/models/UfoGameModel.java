package co.edu.uptc.models;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import co.edu.uptc.pojos.Ufo;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UfoGameModel{
    private UfoRunner ufoRunner;
    private UfoSpawnRunner spawnRunner;
    private List<Ufo> Ufos;
    private int spawnRate;
    private int speed;
    private int numberofUfos;

    public UfoGameModel() {
        this.Ufos = new CopyOnWriteArrayList<>();
        ufoRunner = new UfoRunner(this);
        spawnRunner = new UfoSpawnRunner(this);
    }

    public synchronized void addUfo(int speed) {
        UfoController ufoController = new UfoController(this);
        Ufo newUfo = ufoController.createUfo(speed);
        Ufos.add(newUfo);
    }

    public void startGame() {
        Thread moveThread = new Thread(ufoRunner);
        moveThread.start();
        Thread spawnThread = new Thread(spawnRunner);
        spawnThread.start();
    }

    public List<Ufo> getUfos() {
        return Ufos;
    }

    public void setSpawnRate(int spawnRate) {
        this.spawnRate = spawnRate;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public void setNumberofUfos(int numberofUfos) {
        this.numberofUfos = numberofUfos;
    }
    
    public synchronized void moveAll() {
        Iterator<Ufo> iterator = Ufos.iterator();
        while (iterator.hasNext()) {
            Ufo ufo = iterator.next();
            UfoController ufoController = new UfoController(this);
            ufoController.moveUfo(ufo,Ufos);
            //Todo: presenter.updateUfoCount(Ufos.size());
        }
    }
}
