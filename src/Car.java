import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Car {
    public double x;
    public double y;
    public int rotation = 0;
    public int wheelRotation = 0;
    public int speed = 0;
    public int width = 20;
    public int length = 40;
    public float [] sensors = new float[Constants.SENSOR_COUNT];
    public Color color = Color.ORANGE;
    public int fitness = 0;
    public boolean crashed = false;
    public double savedFitness = 0;
    public int inactiveCount = 0;
    public int level = 0;

    public Car() {
        x = 120;
        y = 175;
    }

    public Car(int angle) {
        x = 120;
        y = 175;
        rotation = angle;
    }

    public Car(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public List<Coord> getBounderies() {
        List<Coord> coords = new ArrayList<>();
        double cos = Math.cos(Math.toRadians(rotation));
        double sin = Math.sin(Math.toRadians(rotation));
        int w = width/2;
        int h = length;

        Coord coord1 = new Coord(x + (int)(cos * h - sin * w), y + (int)(sin * h + cos * w));
        Coord coord2 = new Coord(x + (int)(cos * h + sin * w), y + (int)(sin * h - cos * w));
        Coord coord3 = new Coord(x + (int)(sin * w), y - (int)(cos * w));
        Coord coord4 = new Coord(x - (int)(sin * w), y + (int)(cos * w));
        coords.add(coord1);
        coords.add(coord2);
        coords.add(coord3);
        coords.add(coord4);
        return coords;
    }


    public void move() {
        if (crashed) {
            speed = 0;
            return;
        }
        rotation +=  (speed * wheelRotation)/700;
        wheelRotation *= 0.8;
        if (wheelRotation < 2 && wheelRotation > -2) {
            wheelRotation = 0;
        }

        x += Math.cos(Math.toRadians(rotation)) * speed /10;
        y += Math.sin(Math.toRadians(rotation)) * speed /10;
        speed *= 0.90;
        if (speed > -2 && speed < 2) {
            speed = 0;
        }
    }

    public void addEnergy() {
        if (speed > 100) {
            speed = 100;
            return;
        }
        speed += 10;
    }

    public void removeEnergy() {
        if (speed < -60) {
            speed = -60;
            return;
        }
        speed -= 6;
    }

    public void rotateWheel(int i) {
        wheelRotation += i;
        if (wheelRotation > 60) {
            wheelRotation = 60;
        } else if (wheelRotation < -60) {
            wheelRotation = -60;
        }
    }

    public boolean checkInactive() {
        if (savedFitness >= fitness) {
            inactiveCount++;
            if (inactiveCount > Constants.INACTIVE_COUNT) {
                return true;
            }
        } else {
            if (fitness > savedFitness) savedFitness = fitness;
        }
        return false;
    }
}
