package co.edu.uptc.pojos;

import java.awt.Point;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import java.awt.Rectangle;

@Data
public class Ufo implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private boolean isSelected;
    private Point position;
    private int speed;
    private double angle;
    private boolean isMoving;
    private List<Point> trajectory;
    private double lastAngle;
    public static final int UFO_WIDTH = 75;
    public static final int UFO_HEIGHT = 54;

    public Ufo(int speed, Point position, double angle) {
        this.position = position;
        this.speed = speed;
        this.angle = angle;
        this.isMoving = true;
        this.trajectory = new ArrayList<>();
        this.lastAngle = angle;
        this.isSelected = false;
    }

    public void setTrajectory(List<Point> trajectory) {
        this.trajectory = new ArrayList<>(trajectory);
        this.isMoving = true;
    }

    public boolean hasTrajectory() {
        return trajectory != null && !trajectory.isEmpty();
    }

    public Point getNextPoint() {
        return hasTrajectory() ? trajectory.get(0) : null;
    }

    public void removeReachedPoint() {
        if (hasTrajectory()) {
            trajectory.remove(0);
        }
    }

    public Rectangle getBounds() {
        return new Rectangle(position.x, position.y, UFO_WIDTH, UFO_HEIGHT);
    }

    public void updateAngle(double newAngle) {
        this.angle = newAngle;
        this.lastAngle = newAngle;
    }
}
