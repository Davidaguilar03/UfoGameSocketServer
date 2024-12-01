package co.edu.uptc.models;

import co.edu.uptc.pojos.Ufo;

import java.awt.Point;
import java.util.List;
import java.util.Random;

public class UfoController {
    private Random random;
    private UfoSocketServer ufoSocketServer;
    private static final int LANDING_STRIP_X_MIN = 580;
    private static final int LANDING_STRIP_X_MAX = 660;
    private static final int LANDING_STRIP_Y_MIN = 145;
    private static final int LANDING_STRIP_Y_MAX = 195;

    public UfoController(UfoSocketServer ufoSocketServer) {
        this.ufoSocketServer = ufoSocketServer;
        this.random = new Random();
    }

    public Ufo createUfo(int speed, int id) {
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
        ufoSocketServer.playCrashSoundOrder();
        ufoSocketServer.incrementCrashedUfoCountOrder(1);
    }

    private void handleLandingStripCollision(Ufo ufo, List<Ufo> allUfos) {
        removeUfo(ufo, allUfos);
        ufoSocketServer.playLandingSoundOrder();
        ufoSocketServer.incrementLandedUfoCountOrder();
    }

    private void handleUfoCollisions(Ufo ufo, List<Ufo> allUfos) {
        for (Ufo otherUfo : allUfos) {
            if (ufo != otherUfo && ufo.getBounds().intersects(otherUfo.getBounds())) {
                removeUfo(ufo, allUfos);
                removeUfo(otherUfo, allUfos);
                ufoSocketServer.playCrashSoundOrder();
                ufoSocketServer.incrementCrashedUfoCountOrder(2);
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

    public void moveToNextPoint(Ufo ufo, Point currentPos, double speed) {
        Point targetPos = ufo.getNextPoint();
        int deltaX = targetPos.x - currentPos.x;
        int deltaY = targetPos.y - currentPos.y;
        double distance = calculateDistance(deltaX, deltaY);

        if (distance > 0) {
            double angle = calculateAngle(deltaY, deltaX);
            ufo.updateAngle(angle);
            moveUfo(ufo, currentPos, speed, distance, angle, targetPos);
        }
    }

    private double calculateDistance(int deltaX, int deltaY) {
        return Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    }

    private double calculateAngle(int deltaY, int deltaX) {
        return Math.atan2(deltaY, deltaX);
    }

    private void moveUfo(Ufo ufo, Point currentPos, double speed, double distance, double angle, Point targetPos) {
        double normalizedSpeed = Math.min(speed, distance);
        int moveX = (int) (normalizedSpeed * Math.cos(angle));
        int moveY = (int) (normalizedSpeed * Math.sin(angle));
        currentPos.translate(moveX, moveY);

        if (distance <= speed) {
            currentPos.setLocation(targetPos);
            ufo.removeReachedPoint();
        }
    }

    private void moveInLastDirection(Ufo ufo, Point currentPos, double speed) {
        double angle = ufo.getLastAngle();
        int moveX = (int) (speed * Math.cos(angle));
        int moveY = (int) (speed * Math.sin(angle));
        currentPos.translate(moveX, moveY);
    }

}
