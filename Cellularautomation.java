import java.io.*;
import java.util.Random;
import java.util.Scanner;
import java.math.BigInteger;


public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
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

        String finalStateBinary = automaton.toBinaryString();
        System.out.println("Final state in binary: " + finalStateBinary);
        BigInteger finalStateDecimal = new BigInteger(finalStateBinary, 2);
        System.out.println("Final state in decimal: " + finalStateDecimal.toString());
    }

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
}
