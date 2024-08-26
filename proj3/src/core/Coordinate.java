package core;

import java.util.Objects;

public class Coordinate { //new version of Position
    private int x, y;
    public Coordinate(int x, int y) {
        this.x = x;
        this.y = y;
    }
    public int getX() {
        return this.x;
    }
    public int getY() {
        return this.y;
    }

    public static int substractX(Coordinate a, Coordinate b) {
        return a.getX() - b.getX();
    }
    public static int substractY(Coordinate a, Coordinate b) {
        return a.getY() - b.getY();
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Coordinate that = (Coordinate) o;
        return x == that.x && y == that.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }


}
