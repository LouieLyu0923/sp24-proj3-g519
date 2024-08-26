package core;

import edu.princeton.cs.algs4.StdDraw;

import java.awt.*;

public class Main {

    public static void main(String[] args) {
        StdDraw.clear(Color.BLACK);
        StdDraw.enableDoubleBuffering();

        World engine = new World();
        engine.buildAndNavigate();

    }
}
