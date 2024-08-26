package core;

import java.util.List;
import java.util.ArrayList;

public class Room {
    private Coordinate coo;
    private int width, height;
    private List<Coordinate> spread, barrier;
    public Room(Coordinate startPosition, int width, int height) {
        this.coo = startPosition;
        this.width = width;
        this.height = height;
        this.spread = calculateSpread();
        this.barrier = calculateBarriers();
    }
    private List<Coordinate> calculateSpread() {
        List<Coordinate> spanList = new ArrayList<>();
        for (int x = coo.getX(); x < coo.getX() + width; x++) {
            for (int y = coo.getY(); y < coo.getY() + height; y++) {
                spanList.add(new Coordinate(x, y));
            }
        }
        return spanList;
    }

    private List<Coordinate> calculateBarriers() {
        List<Coordinate> wallsList = new ArrayList<>();
        int xStart = coo.getX() - 1;
        int yStart = coo.getY() - 1;
        int xEnd = coo.getX() + width;
        int yEnd = coo.getY() + height;

        // Add horizontal walls
        for (int x = xStart; x <= xEnd; x++) {
            wallsList.add(new Coordinate(x, yStart)); // top wall
            wallsList.add(new Coordinate(x, yEnd));   // bottom wall
        }

        // Add vertical walls
        for (int y = yStart + 1; y < yEnd; y++) {
            wallsList.add(new Coordinate(xStart, y)); // left wall
            wallsList.add(new Coordinate(xEnd, y));   // right wall
        }

        return wallsList;
    }

    public List<Coordinate> getSpread() {
        return this.spread;
    }
    public List<Coordinate> getBarrier() {
        return this.barrier;
    }
    public int getX() {
        return this.coo.getX();
    }
    public int getY() {
        return this.coo.getY();
    }
    public int getWidth() {
        return this.width;
    }
    public int getHeight() {
        return this.height;
    }
}
