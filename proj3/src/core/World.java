package core;
import edu.princeton.cs.algs4.StdDraw;
import tileengine.TERenderer;
import tileengine.TETile;
import tileengine.Tileset;

import java.awt.*;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.*;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.util.List;

public class World {
    private Random rand;
    private MainCharacter mainCharacter;
    private List<Room> rooms;
    private String mainCharacterHistorySteps, seed;
    private TETile[][] worldMap;
    private static final String FILENAME = "data.txt";

    public static final int WIDTH = 90;
    public static final int HEIGHT = 40;
    private boolean lineOfSightEnabled;
    public World() {
        mainCharacter = null;
        rand = null;
        rooms = new ArrayList<>();
        mainCharacterHistorySteps = "";
        seed = "";
        worldMap = new TETile[WIDTH][HEIGHT];
        lineOfSightEnabled = false;
    }

    public void start(String input) {
        TERenderer ter = new TERenderer();
        ter.initialize(WIDTH, HEIGHT + 3, 0, 0);
        interactWithInputString(input);
        boolean active = true;
        while (active) {
            StdDraw.clear(new Color(0, 0, 0));
            if (StdDraw.hasNextKeyTyped()) {
                char key = StdDraw.nextKeyTyped();
                active = updateMap(key);
            }
            drawStatus();
            Set<Coordinate> visibleTiles;
            if (lineOfSightEnabled) {
                visibleTiles = calcLineOfSight();
            } else {
                visibleTiles = getAllTiles();
            }

            ter.renderFrame(renderBoxTiles(visibleTiles));
        }
        System.exit(0);
    }
    public Set<Coordinate> calcLineOfSight() {
        Set<Coordinate> visibleTiles = new HashSet<>();

        // Add the main character's current position to the visible tiles
        Coordinate characterPos = mainCharacter.getPosition();
        visibleTiles.add(characterPos);

        int fovSize = 5; // box size

        /**
         * @source chatGPT generated for-loop: Iterate over the tiles within the box
         */
        for (int dx = -fovSize; dx <= fovSize; dx++) {
            for (int dy = -fovSize; dy <= fovSize; dy++) {
                int x = characterPos.getX() + dx;
                int y = characterPos.getY() + dy;

                if (isWithinBounds(x, y)) {
                    visibleTiles.add(new Coordinate(x, y));
                }
            }
        }
        return visibleTiles;
    }

    // if line-of-sight feature is disabled: this method will be called to show all tiles
    private Set<Coordinate> getAllTiles() {
        Set<Coordinate> allTiles = new HashSet<>();
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                allTiles.add(new Coordinate(x, y));
            }
        }
        return allTiles;
    }

    // renders line-of-sight feature
    private TETile[][] renderBoxTiles(Set<Coordinate> boxTiles) {
        TETile[][] visibleMap = new TETile[WIDTH][HEIGHT];
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                if (boxTiles.contains(new Coordinate(x, y))) {
                    visibleMap[x][y] = worldMap[x][y];
                } else {
                    visibleMap[x][y] = Tileset.NOTHING; // Set invisible tiles to NOTHING
                }
            }
        }
        return visibleMap;
    }

    public void drawMap() {
        StdDraw.clear();
        StdDraw.setPenColor(StdDraw.BLACK);
        StdDraw.text(WIDTH * 8, HEIGHT * 12, "CS61B: Dungeon");
        StdDraw.text(WIDTH * 8, HEIGHT * 5, "New Game (N)");
        StdDraw.text(WIDTH * 8, HEIGHT * 4, "Load Game (L)");
        StdDraw.text(WIDTH * 8, HEIGHT * 3, "Saved Records (R)");
        StdDraw.text(WIDTH * 8, HEIGHT * 2, "Quit Game (Q)");
        StdDraw.show();
    }
    public void drawSeedMap(String seedPassIn) {
        StdDraw.clear();
        StdDraw.setPenColor(StdDraw.BLACK);
        StdDraw.text(WIDTH * 8, HEIGHT * 12, "Please Enter a Seed");
        StdDraw.text(WIDTH * 8, HEIGHT * 10, "Seed: " + seedPassIn);
        StdDraw.show();
    }

    public String drawRecords() {
        List<String> records = findAllRecords();
        if (records.isEmpty()) {
            StdDraw.clear();
            StdDraw.setPenColor(StdDraw.BLACK);
            StdDraw.text(WIDTH * 2, HEIGHT * 15, "No saved records found.");
            StdDraw.show();
            return null;
        }

        StdDraw.clear();
        StdDraw.setPenColor(StdDraw.BLACK);
        StdDraw.text(WIDTH * 2, HEIGHT * 15, "Saved Game Records:");
        for (int i = 0; i < records.size(); i++) {
            StdDraw.text(WIDTH * 8, HEIGHT * (14 - i), "Record " + (i + 1) + ": " + records.get(i));
        }
        StdDraw.show();

        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                char key = StdDraw.nextKeyTyped();
                if (Character.isDigit(key)) {
                    int index = Character.getNumericValue(key) - 1; // Convert char to index
                    if (index >= 0 && index < records.size()) {
                        return records.get(index); // Return the selected record
                    }
                }
            }
        }
    }



    public void drawStatus() {
        StdDraw.setPenColor(StdDraw.WHITE);
        StdDraw.text(WIDTH * 0.5, HEIGHT + 2, "Player Coordinates: ");
        StdDraw.text(WIDTH * 0.6, HEIGHT + 2, "(" + mainCharacter.getX() + ", " + mainCharacter.getY() + ")");
        int mouseX = Math.min((int) StdDraw.mouseX(), WIDTH - 1);
        int mouseY = Math.min((int) StdDraw.mouseY(), HEIGHT - 1);
        TETile mouseTile = this.worldMap[mouseX][mouseY];
        // Assuming mouseX and mouseY updates are also frequent but light on changes
        StdDraw.textRight(WIDTH * 0.1, HEIGHT + 2, "Mouse: [" + mouseTile.description() + "]");
        // You can keep other UI elements static unless they need to change
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
        Date date = new Date(System.currentTimeMillis());
        StdDraw.text(WIDTH - 8, HEIGHT + 2, formatter.format(date));
        //StdDraw.show(); // Update the screen once after all changes are made
    }

    public String checkKey() {
        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                char key = StdDraw.nextKeyTyped();
                if (key == 'n' || key == 'N' || key == 'l' || key == 'L'
                        || key == 'q' || key == 'Q' || key == 'r' || key == 'R') {
                    return Character.toString(key);
                }
            }
        }
    }
    public String createSeed() {
        String seedString = "";
        char lastChar = 0;
        while (lastChar != 's' && lastChar != 'S') {
            drawSeedMap(seedString);
            if (StdDraw.hasNextKeyTyped()) {
                lastChar = StdDraw.nextKeyTyped();
                seedString += lastChar;
            }
        }
        return seedString.substring(0, seedString.length() - 1);
    }
    public List<String> findAllRecords() {
        List<String> records = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(FILENAME))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    records.add(line.trim());
                }
            }
        } catch (IOException e) {
            System.out.println("An error occurred while reading the file: " + e.getMessage());
        }
        return records;
    }


    public boolean updateMap(char c) {
        if (c == 't' || c == 'T') {
            lineOfSightEnabled = !lineOfSightEnabled;
            return true; // Continue on
        }
        if (shouldQuit(c)) {
            handleQuit();
            return false;
        }

        if (!processMainCharacterHistorySteps(c)) {
            return false;
        }
        return true;
    }
    private boolean shouldQuit(char c) {
        return (c == 'q' && !mainCharacterHistorySteps.isEmpty()
                && mainCharacterHistorySteps.charAt(mainCharacterHistorySteps.length() - 1) == ':');
    }
    private void handleQuit() {
        String gameData = generateGameData();
        saveGameDataToFile(gameData);
    }

    private String generateGameData() {
        int last = this.mainCharacterHistorySteps.length() - 1;
        return this.seed + "S" + this.mainCharacterHistorySteps.substring(0, last);
    }

    private void saveGameDataToFile(String gameData) {
        try (FileWriter writer = new FileWriter(FILENAME, true)) {
            writer.write("\n" + gameData);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to write into file:" + FILENAME);
        }
    }

    private boolean processMainCharacterHistorySteps(char c) {
        // Normalize the character to lowercase for uniformity in checks
        c = Character.toLowerCase(c);
        // Check if the command is the quit command (specifically ":q")
        if (mainCharacterHistorySteps.endsWith(":") && c == 'q') {
            return false;
        }

        // If there's a colon and the next character isn't 'q', undo the colon
        if (mainCharacterHistorySteps.endsWith(":") && c != 'q') {
            undoLastCommand();
        }

        // Append colon when it's part of the input but not followed by 'q' yet
        if (c == ':') {
            mainCharacterHistorySteps += c;
            return true; // Continue processing, awaiting next character
        }

        // Process valid movement commands
        if (isValidMovementCommand(c)) {
            handleMovementCommand(c);
        }

        return true;
    }

    private void undoLastCommand() {
        // Remove the last character (colon) from mainCharacterHistorySteps
        mainCharacterHistorySteps = mainCharacterHistorySteps.substring(0, mainCharacterHistorySteps.length() - 1);
    }

    private boolean isValidMovementCommand(char c) {
        // Check if the character is one of the movement commands
        return c == 'a' || c == 'd' || c == 's' || c == 'w';
    }

    private void handleMovementCommand(char c) {
        int[] change = getMovementChange(c);
        int newX = mainCharacter.getX() + change[0];
        int newY = mainCharacter.getY() + change[1];

        if (isValidMove(newX, newY)) {
            updatePlayerCoordinate(newX, newY);
            mainCharacterHistorySteps += c;
        }
    }
    private int[] getMovementChange(char c) {
        int[] change = new int[2]; // change[0] for x, change[1] for y
        if (c == 'a') {
            change[0] = -1;
        } else if (c == 'd') {
            change[0] = 1;
        } else if (c == 's') {
            change[1] = -1;
        } else if (c == 'w') {
            change[1] = 1;
        }
        return change;
    }
    private boolean isValidMove(int x, int y) {
        return !worldMap[x][y].equals(Tileset.WALL);
    }
    private void updatePlayerCoordinate(int newX, int newY) {
        int oldX = mainCharacter.getX();
        int oldY = mainCharacter.getY();
        worldMap[oldX][oldY] = Tileset.FLOOR;
        worldMap[newX][newY] = mainCharacter.getTile();
        mainCharacter.setX(newX);
        mainCharacter.setY(newY);
    }
    public void buildAndNavigate() {
        StdDraw.setCanvasSize(WIDTH * 16, HEIGHT * 16);
        Font font = new Font("Monaco", Font.BOLD, 30);
        StdDraw.setFont(font);
        StdDraw.setXscale(0, WIDTH * 16);
        StdDraw.setYscale(0, HEIGHT * 16);

        drawMap();
        String opt = checkKey();
        opt = opt.toLowerCase();
        switch (opt) {
            case "n" -> {
                String inputString = opt + createSeed() + "S";
                start(inputString);
            }
            case "l" -> start(opt);
            case "r" -> {
                String wantedRecord = drawRecords();
                String inputString = opt + wantedRecord;
                start(inputString);
            }
            case "q" -> System.exit(0);
            default ->  {

            }
        }
    }


    private String extractSeed(String input) {
        int startIndex = 1;  // Start after 'N'
        String lowerCaseInput = input.toLowerCase();
        int endIndex = lowerCaseInput.indexOf('s', startIndex); // Now searching for 's' will cover both 's' and 'S'
        if (endIndex > startIndex) {
            return input.substring(startIndex, endIndex);  // Extract the seed part, exclude 's'
        }
        return null;  // Return null if 's' is not found or format is incorrect
    }
    private String extractSeedWithoutMode(String input) {
        int startIndex = 0;  // Start after 'N'
        String lowerCaseInput = input.toLowerCase();
        int endIndex = lowerCaseInput.indexOf('s', startIndex); // Now searching for 's' will cover both 's' and 'S'
        if (endIndex > startIndex) {
            return input.substring(startIndex, endIndex);  // Extract the seed part, exclude 's'
        }
        return null;  // Return null if 's' is not found or format is incorrect
    }
    private void seedToRandSeed() {
        if (this.seed != null) {
            initializeRandomGenerator(this.seed);
        } else {
            // Handle error if seed extraction fails
            System.out.println("Invalid Empty Seed");
        }
    }
    private void initializeRandomGenerator(String seedPassIn) {
        try {
            BigInteger bigSeed = new BigInteger(seedPassIn);
            long seedString = bigSeed.mod(BigInteger.valueOf(Long.MAX_VALUE)).longValue();
            this.rand = new Random(seedString);
            System.out.println("Random generator initialized with seed: " + seedString);
        } catch (NumberFormatException e) {
            System.out.println("Invalid seed number format: " + seedPassIn);
            e.printStackTrace();
        }
    }
    public TETile[][] interactWithInputString(String input) {
        String appendix = input.substring(1);  // Everything after 'L' is stored here.
        // Load logic(put away)
        if (input.charAt(0) == 'L' || input.charAt(0) == 'l') {
            try {
                FileReader reader = new FileReader(FILENAME);
                BufferedReader bufferedReader = new BufferedReader(reader);
                String currentLine = null;
                while ((currentLine = bufferedReader.readLine()) != null) {
                    if (!currentLine.trim().isEmpty()) {
                        input = currentLine;  // Keep updating `lastLine` until the end of the file
                    }
                }
                // After reading the last non-empty line from the file, append the stored appendix.
                input += appendix;
                this.seed = extractSeedWithoutMode(input);
                seedToRandSeed();
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //ExtractSeed
        if (input.charAt(0) == 'N' || input.charAt(0) == 'n') {
            this.seed = extractSeed(input);
            seedToRandSeed();
        }

        //HistoryGame using slots
        if (input.charAt(0) == 'R' || input.charAt(0) == 'r') {
            this.seed = extractSeed(input);
            seedToRandSeed();
        }

        // initialize tiles
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                this.worldMap[x][y] = Tileset.NOTHING;
            }
        }

        // generate rooms
        int numRooms = 8 + this.rand.nextInt(5);
        for (int i = 0; i < numRooms; i++) {
            Room r = generateRoom();
            rooms.add(r);
            drawRoom(r);
        }

        for (int i = 0; i < rooms.size() - 1; i++) {
            int j = i + 1;
            connect(rooms.get(i), rooms.get(j));
        }


        setRandomStartCoordinate();
        // Process the input string to extract player moves
        String mainCharacterHistory = input.substring(input.indexOf('S') + 1);
        // Execute saved player moves
        for (char move : mainCharacterHistory.toCharArray()) {
            updateMap(move);
        }
        return this.worldMap;
    }


    public void setRandomStartCoordinate() {
        Room startRoom = getRandomRoom();
        Coordinate startCoordinate = getRandomCoordinateInRoom(startRoom);
        placeMainCharacter(startCoordinate);
    }

    private Room getRandomRoom() {
        return rooms.get(rand.nextInt(rooms.size()));
    }

    private Coordinate getRandomCoordinateInRoom(Room room) {
        List<Coordinate> roomCoordinates = room.getSpread();
        return roomCoordinates.get(rand.nextInt(roomCoordinates.size()));
    }

    private void placeMainCharacter(Coordinate coordinate) {
        mainCharacter = new MainCharacter(coordinate, Tileset.AVATAR);
        this.worldMap[coordinate.getX()][coordinate.getY()] = mainCharacter.getTile();
    }

    public void connect(Room roomA, Room roomB) {
        Coordinate pointA = getRandomCoordinate(roomA);
        Coordinate pointB = getRandomCoordinate(roomB);

        Coordinate start = getStartPoint(pointA, pointB);
        Coordinate end = getEndPoint(pointA, pointB, start);

        drawHallway(start, end);
    }

    private Coordinate getRandomCoordinate(Room room) {
        List<Coordinate> spread = room.getSpread();
        return spread.get(rand.nextInt(spread.size()));
    }

    private Coordinate getStartPoint(Coordinate a, Coordinate b) {
        return Coordinate.substractX(a, b) < 0 ? a : b;
    }

    private Coordinate getEndPoint(Coordinate a, Coordinate b, Coordinate start) {
        return start == a ? b : a;
    }

    private void drawHallway(Coordinate start, Coordinate end) {
        drawHorizontalCorridor(start, end);
        drawVerticalCorridor(start, end);
    }

    private void drawHorizontalCorridor(Coordinate start, Coordinate end) {
        // Draw the floor
        for (int col = start.getX(); col <= end.getX(); col++) {
            setTileType(col, start.getY(), Tileset.FLOOR);
        }
        // Draw the Barriers above and below the floor
        drawHorizontalBarriers(start, end);
    }

    private void drawVerticalCorridor(Coordinate start, Coordinate end) {
        Coordinate corner = new Coordinate(end.getX(), start.getY());
        start = Coordinate.substractY(corner, end) < 0 ? corner : end;
        end = start == corner ? end : corner;

        for (int row = start.getY(); row <= end.getY(); row++) {
            setTileType(start.getX(), row, Tileset.FLOOR);
        }
        drawVerticalBarriers(start, end);
    }

    private void drawHorizontalBarriers(Coordinate start, Coordinate end) {
        for (int col = start.getX() - 1; col <= end.getX() + 1; col++) {
            setBarrierIfNeeded(col, start.getY() - 1);
            setBarrierIfNeeded(col, start.getY() + 1);
        }
    }

    private void drawVerticalBarriers(Coordinate start, Coordinate end) {
        for (int row = start.getY() - 1; row <= end.getY() + 1; row++) {
            setBarrierIfNeeded(start.getX() - 1, row);
            setBarrierIfNeeded(start.getX() + 1, row);
        }
    }

    private void setTileType(int x, int y, TETile type) {
        if (isWithinBounds(x, y)) {
            this.worldMap[x][y] = type;
        }
    }

    private void setBarrierIfNeeded(int x, int y) {
        if (isWithinBounds(x, y) && this.worldMap[x][y] != Tileset.FLOOR) {
            this.worldMap[x][y] = Tileset.WALL;
        }
    }

    private boolean isWithinBounds(int x, int y) {
        return x >= 0 && x < this.worldMap.length && y >= 0 && y < this.worldMap[x].length;
    }


    public void drawRoom(Room r) {
        // Use lambda expressions
        r.getSpread().forEach(p -> setTile(p, Tileset.FLOOR));
        r.getBarrier().forEach(p -> setTile(p, Tileset.WALL));
    }

    private void setTile(Coordinate p, TETile tileType) {
        this.worldMap[p.getX()][p.getY()] = tileType;
    }


    public Room generateRoom() {
        Room newRoom;
        do {
            int width = getRandomRoomSize();
            int height = getRandomRoomSize();
            Coordinate startCoordinate = randomCoordinate(Math.max(width, height));
            newRoom = new Room(startCoordinate, width, height);
        } while (roomOverlapsWithExistingRooms(newRoom));

        return newRoom;
    }

    private int getRandomRoomSize() {
        return 5 + rand.nextInt(7);
    }

    private boolean roomOverlapsWithExistingRooms(Room newRoom) {
        for (Room existingRoom : rooms) {
            if (roomsOverlap(existingRoom, newRoom)) {
                return true;
            }
        }
        return false;
    }

    private boolean roomsOverlap(Room a, Room b) {
        for (Coordinate cooA : a.getBarrier()) {
            for (Coordinate cooB : b.getBarrier()) {
                if (coordinatesOverlap(cooA, cooB)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean coordinatesOverlap(Coordinate p1, Coordinate p2) {
        return p1.getX() == p2.getX() && p1.getY() == p2.getY();
    }
    public Coordinate randomCoordinate(int maxLengthOfWidthOrHeight) {
        // Leave space for Barriers so that room will never exceed the canvas
        int x = 1 + rand.nextInt(WIDTH - maxLengthOfWidthOrHeight - 1);
        int y = 1 + rand.nextInt(HEIGHT - maxLengthOfWidthOrHeight - 1);
        return new Coordinate(x, y);
    }


}
