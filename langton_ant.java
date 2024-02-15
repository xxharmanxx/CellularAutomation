import java.io.*;
import java.util.Random;
import java.util.Scanner;
import java.math.BigInteger;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Choose the automaton to run:");
        System.out.println("1. Cellular Automaton");
        System.out.println("2. Conway's Game of Life");
        System.out.println("3. Langton's Ant");
        int choice = scanner.nextInt();

        switch (choice) {
            case 1:
                runCellularAutomaton(scanner);
                break;
            case 2:
                runGameOfLife(scanner);
                break;
            case 3:
                runLangtonsAnt(scanner);
                break;
            default:
                System.out.println("Invalid option.");
                break;
        }
    }

    private static void runCellularAutomaton(Scanner scanner) {
        System.out.println("Enter the size of the automaton:");
        int size = scanner.nextInt();

        System.out.println("Enter the rule number (0-255) or -1 for a random rule:");
        int ruleInput = scanner.nextInt();
        int ruleNumber = (ruleInput == -1) ? new Random().nextInt(256) : ruleInput;
        System.out.println("Using rule number: " + ruleNumber);

        CellularAutomaton automaton = new CellularAutomaton(size, ruleNumber);

        System.out.println("Would you like to load the initial state from a file? (yes/no)");
        String answer = scanner.next();
        if ("yes".equalsIgnoreCase(answer)) {
            System.out.println("Enter filename:");
            String filename = scanner.next();
            try {
                automaton.loadFromFile(filename);
            } catch (IOException e) {
                System.out.println("Could not load initial state from file. Starting with default.");
            }
        } else {
            System.out.println("Would you like to set a custom initial state? (yes/no)");
            answer = scanner.next();
            if ("yes".equalsIgnoreCase(answer)) {
                System.out.println("Enter the initial state as a binary string:");
                scanner.nextLine(); // Consume the newline
                String initialState = scanner.nextLine();
                automaton.fromBinaryString(initialState);
            }
        }

        System.out.println("Initial Generation:");
        automaton.displayGeneration();

        System.out.println("Enter the number of generations:");
        int generations = scanner.nextInt();

        for (int i = 0; i < generations; i++) {
            automaton.generateNext();
            System.out.println("Generation " + (i + 1) + ":");
            automaton.displayGeneration();
        }

        try {
            automaton.saveToFile("finalState.txt");
        } catch (IOException e) {
            System.out.println("Could not save final state.");
        }
    }

    private static void runGameOfLife(Scanner scanner) {
        System.out.println("Enter the size of the grid (N x N):");
        int size = scanner.nextInt(); // Assuming a square grid for simplicity.

        GameOfLife game = new GameOfLife(size);

        // Optionally, initialize the grid with a specific pattern here or let the user input it.
        // For simplicity, starting with a random configuration or a known pattern could be an option.

        System.out.println("Initial Configuration:");
        game.displayGrid();

        System.out.println("Enter the number of generations:");
        int generations = scanner.nextInt();

        for (int i = 0; i < generations; i++) {
            game.nextGeneration();
            System.out.println("Generation " + (i + 1) + ":");
            game.displayGrid();
        }
    }

    // CellularAutomaton class as previously defined.
   public static class CellularAutomaton {
        private int[] parentGeneration;
        private int[] childGeneration;
        private int generationSize;
        private int ruleNumber;
        private boolean[] ruleSet;

        public CellularAutomaton(int size, int ruleNumber) {
            this.generationSize = size;
            this.ruleNumber = ruleNumber;
            this.parentGeneration = new int[size];
            this.childGeneration = new int[size];
            this.ruleSet = new boolean[8];
            setRuleSet(ruleNumber);
            for (int i = 0; i < size; i++) {
                this.parentGeneration[i] = 0;
            }
            this.parentGeneration[size / 2] = 1;
        }

        private void setRuleSet(int ruleNumber) {
            for (int i = 0; i < 8; i++) {
                this.ruleSet[i] = ((ruleNumber >> i) & 1) == 1;
            }
        }

        public void fromBinaryString(String binary) {
            parentGeneration = new int[generationSize];
            for (int i = 0; i < Math.min(binary.length(), generationSize); i++) {
                parentGeneration[i] = binary.charAt(i) == '1' ? 1 : 0;
            }
        }

        public String toBinaryString() {
            StringBuilder sb = new StringBuilder();
            for (int cell : parentGeneration) {
                sb.append(cell);
            }
            return sb.toString();
        }

        public void saveToFile(String filename) throws IOException {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
                writer.write(toBinaryString());
            }
        }

        public void loadFromFile(String filename) throws IOException {
            try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
                String line = reader.readLine();
                if (line != null) {
                    fromBinaryString(line);
                }
            }
        }

        private void applyRule() {
            for (int i = 0; i < generationSize; i++) {
                int left = (i == 0) ? parentGeneration[generationSize - 1] : parentGeneration[i - 1];
                int self = parentGeneration[i];
                int right = (i == generationSize - 1) ? parentGeneration[0] : parentGeneration[i + 1];
                int index = (left << 2) | (self << 1) | right;
                childGeneration[i] = ruleSet[index] ? 1 : 0;
            }
            parentGeneration = childGeneration.clone();
        }

        public void generateNext() {
            applyRule();
        }

        public void displayGeneration() {
            for (int i = 0; i < generationSize; i++) {
                System.out.print(parentGeneration[i] == 1 ? "1" : "0");
            }
            System.out.println();
        }
    }

    // GameOfLife class implementation.
    public static class GameOfLife {
        private boolean[][] grid;
        private int size;

        public GameOfLife(int size) {
            this.size = size;
            this.grid = new boolean[size][size];
            // Optional: Initialize with a random configuration for demonstration.
            Random rand = new Random();
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    grid[i][j] = rand.nextBoolean();
                }
            }
        }

        public void nextGeneration() {
            boolean[][] newGrid = new boolean[size][size];
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    int liveNeighbors = countLiveNeighbors(i, j);
                    if (grid[i][j]) {
                        newGrid[i][j] = liveNeighbors == 2 || liveNeighbors == 3;
                    } else {
                        newGrid[i][j] = liveNeighbors == 3;
                    }
                }
            }
            grid = newGrid;
        }

        private int countLiveNeighbors(int row, int col) {
            int count = 0;
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    if (i == 0 && j == 0) continue; // Skip the cell itself
                    int newRow = (row + i + size) % size;
                    int newCol = (col + j + size) % size;
                    if (grid[newRow][newCol]) {
                        count++;
                    }
                }
            }
            return count;
        }

        public void displayGrid() {
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    System.out.print(grid[i][j] ? "1" : "0");
                }
                System.out.println();
            }
        }
 private static void runLangtonsAnt(Scanner scanner) {
        System.out.println("Enter the size of the grid (N x N):");
        int size = scanner.nextInt(); // Assuming a square grid for simplicity.

        LangtonsAnt langtonsAnt = new LangtonsAnt(size);

        System.out.println("Initial Configuration:");
        langtonsAnt.displayGrid();

        System.out.println("Enter the number of steps:");
        int steps = scanner.nextInt();

        for (int i = 0; i < steps; i++) {
            langtonsAnt.move();
            System.out.println("Step " + (i + 1) + ":");
            langtonsAnt.displayGrid();
        }
    }

    // CellularAutomaton and GameOfLife classes remain unchanged

    public static class LangtonsAnt {
        private boolean[][] grid;
        private int size;
        private int antX, antY; // Ant's position
        private int dir = 0; // 0: up, 1: right, 2: down, 3: left

        public LangtonsAnt(int size) {
            this.size = size;
            this.grid = new boolean[size][size];
            // Place the ant in the middle of the grid
            antX = size / 2;
            antY = size / 2;
        }

        public void move() {
            // Flip the color of the current cell
            grid[antX][antY] = !grid[antX][antY];

            // Turn the ant
            if (grid[antX][antY]) { // If the cell becomes white, turn right
                dir = (dir + 1) % 4;
            } else { // If the cell becomes black, turn left
                dir = (dir + 3) % 4;
            }

            // Move the ant forward
            switch (dir) {
                case 0: // up
                    antX = (antX - 1 + size) % size;
                    break;
                case 1: // right
                    antY = (antY + 1) % size;
                    break;
                case 2: // down
                    antX = (antX + 1) % size;
                    break;
                case 3: // left
                    antY = (antY - 1 + size) % size;
                    break;
            }
        }

        public void displayGrid() {
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    if (i == antX && j == antY) {
                        System.out.print("A");
                    } else {
                        System.out.print(grid[i][j] ? "1" : "0");
                    }
                }
                System.out.println();
            }
        }
    }
}
