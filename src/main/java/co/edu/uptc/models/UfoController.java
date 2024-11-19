package co.edu.uptc.models;

import co.edu.uptc.pojos.Ufo;

import java.awt.Point;
import java.util.List;
import java.util.Random;

public class UfoController {
    private Random random;
    private UfoGameModel ufoGameModel;
    private static final int LANDING_STRIP_X_MIN = 580;
    private static final int LANDING_STRIP_X_MAX = 660;
    private static final int LANDING_STRIP_Y_MIN = 145;
    private static final int LANDING_STRIP_Y_MAX = 195;

    public UfoController(UfoGameModel ufoGameModel) {
        this.ufoGameModel = ufoGameModel;
        this.random = new Random();
    }

    public Ufo createUfo(int speed) {
        Point randomPosition = new Point(getRandomX(), getRandomY());
        double randomAngle = getRandomAngle();
        return new Ufo(speed, randomPosition, randomAngle);
    }

    private int getRandomX() {
        return random.nextInt(1125);
    }

    private int getRandomY() {
        return random.nextInt(632);
    }

    private double getRandomAngle() {
        return random.nextDouble() * 2 * Math.PI;
    }

    public void moveUfo(Ufo ufo, List<Ufo> allUfos) {
        if (ufo.isMoving() && ufo.getTrajectory() != null && !ufo.getTrajectory().isEmpty()) {
            followTrajectory(ufo);
        } else if (ufo.isMoving()) {
            moveInLastDirection(ufo);
            handleCollisions(ufo, allUfos);
        }
    }

    private void moveInLastDirection(Ufo ufo) {
        int deltaX = (int) (ufo.getSpeed() * Math.cos(ufo.getLastAngle()));
        int deltaY = (int) (ufo.getSpeed() * Math.sin(ufo.getLastAngle()));
        ufo.getPosition().translate(deltaX, deltaY);
    }

    private void handleCollisions(Ufo ufo, List<Ufo> allUfos) {
        if (checkWallCollision(ufo)) {
            handleWallCollision(ufo, allUfos);
        } else if (checkLandingStripCollision(ufo)) {
            handleLandingStripCollision(ufo, allUfos);
        } else {
            handleUfoCollisions(ufo, allUfos);
        }
    }

    private void handleWallCollision(Ufo ufo, List<Ufo> allUfos) {
        removeUfo(ufo, allUfos);
        //Todo: ufoGameModel.getPresenter().playCrashSound();
        //Todo: ufoGameModel.getPresenter().incrementCrashedUfoCount(1);
    }

    private void handleLandingStripCollision(Ufo ufo, List<Ufo> allUfos) {
        removeUfo(ufo, allUfos);
        //Todo: ufoGameModel.getPresenter().playLandingSound();
        //Todo: ufoGameModel.getPresenter().incrementLandedUfoCount();
    }

    private void handleUfoCollisions(Ufo ufo, List<Ufo> allUfos) {
        for (Ufo otherUfo : allUfos) {
            if (ufo != otherUfo && ufo.getBounds().intersects(otherUfo.getBounds())) {
                removeUfo(ufo, allUfos);
                removeUfo(otherUfo, allUfos);
                //Todo: ufoGameModel.getPresenter().playCrashSound();
                //Todo: ufoGameModel.getPresenter().incrementCrashedUfoCount(2);
                return;
            }
        }
    }

    private boolean checkWallCollision(Ufo ufo) {
        return ufo.getPosition().x < 0 || ufo.getPosition().x > 1125 ||
                ufo.getPosition().y < 0 || ufo.getPosition().y > 632;
    }

    private boolean checkLandingStripCollision(Ufo ufo) {
        Point position = ufo.getPosition();
        return position.x >= LANDING_STRIP_X_MIN && position.x <= LANDING_STRIP_X_MAX &&
                position.y >= LANDING_STRIP_Y_MIN && position.y <= LANDING_STRIP_Y_MAX;
    }

    private void removeUfo(Ufo ufo, List<Ufo> allUfos) {
        allUfos.remove(ufo);
    }

    private void followTrajectory(Ufo ufo) {
        Point currentPos = ufo.getPosition();
        double speed = Math.max(ufo.getSpeed(), 2);

        if (!ufo.getTrajectory().isEmpty()) {
            moveToNextPoint(ufo, currentPos, speed);
        } else {
            moveInLastDirection(ufo, currentPos, speed);
        }
    }

    private void moveToNextPoint(Ufo ufo, Point currentPos, double speed) {
        Point targetPos = ufo.getNextPoint();
        int deltaX = targetPos.x - currentPos.x;
        int deltaY = targetPos.y - currentPos.y;
        double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        if (distance > 0) {
            double angle = Math.atan2(deltaY, deltaX);
            ufo.updateAngle(angle);
            double normalizedSpeed = Math.min(speed, distance);
            int moveX = (int) (normalizedSpeed * Math.cos(angle));
            int moveY = (int) (normalizedSpeed * Math.sin(angle));
            currentPos.translate(moveX, moveY);
            if (distance <= speed) {
                currentPos.setLocation(targetPos);
                ufo.removeReachedPoint();
            }
        }
    }

    private void moveInLastDirection(Ufo ufo, Point currentPos, double speed) {
        double angle = ufo.getLastAngle();
        int moveX = (int) (speed * Math.cos(angle));
        int moveY = (int) (speed * Math.sin(angle));
        currentPos.translate(moveX, moveY);
    }

}
