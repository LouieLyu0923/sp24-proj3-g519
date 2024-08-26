package core;

import tileengine.TETile;

public class MainCharacter {
    private Coordinate pos;
    private TETile tile;
    public MainCharacter(Coordinate pos, TETile tile) {
        this.pos = pos;
        this.tile = tile;
    }

    public int getX() {
        return this.pos.getX();
    }
    public int getY() {
        return this.pos.getY();
    }
    public void setX(int x) {
        this.pos = new Coordinate(x, getY());
    }
    public void setY(int y) {
        this.pos = new Coordinate(getX(), y);
    }
    public Coordinate getPosition() {
        return this.pos;
    }
    public TETile getTile() {
        return this.tile;
    }
}
