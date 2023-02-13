package irlclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Parser {

    private Map<Integer, Color> constAgentColor = new HashMap<>();

    private Map<Character, Color> constBoxColor = new HashMap<>();

    private char[][] goals;

    public static State parseLevel(BufferedReader serverMessages) throws IOException {
        // We can assume that the level file is conforming to specification, since the server verifies this.
        // Read domain
        serverMessages.readLine(); // #domain
        serverMessages.readLine(); // hospital

        // Read Level name
        serverMessages.readLine(); // #levelname
        serverMessages.readLine(); // <name>

        // Read colors
        serverMessages.readLine(); // #colors
        Map<Integer, irlclient.Color> agentColors = new HashMap<>();
        Map<Character, irlclient.Color> boxColors = new HashMap<>();
        String line = serverMessages.readLine();
        while (!line.startsWith("#")) {
            String[] split = line.split(":");
            irlclient.Color color = Color.fromString(split[0].strip());
            String[] entities = split[1].split(",");
            for (String entity : entities) {
                char c = entity.strip().charAt(0);
                if ('0' <= c && c <= '9') {
                    agentColors.put(c - '0', color);
                } else if ('A' <= c && c <= 'Z') {
                    boxColors.put(c, color);
                }
            }
            line = serverMessages.readLine();
        }

        // Read initial state
        // line is currently "#initial"
        int numRows = 0;
        int numCols = 0;
        ArrayList<String> levelLines = new ArrayList<>(64);
        line = serverMessages.readLine();
        while (!line.startsWith("#")) {
            levelLines.add(line);
            numCols = Math.max(numCols, line.length());
            ++numRows;
            line = serverMessages.readLine();
        }
        int numAgents = 0;
        Map<Integer, Integer> agentRows = new HashMap<>();
        Map<Integer, Integer> agentCols = new HashMap<>();
        boolean[][] walls = new boolean[numRows][numCols];
        char[][] boxes = new char[numRows][numCols];
        for (int row = 0; row < numRows; ++row) {
            line = levelLines.get(row);
            for (int col = 0; col < line.length(); ++col) {
                char c = line.charAt(col);

                if ('0' <= c && c <= '9') {
                    agentRows.put(c - '0', row);
                    agentCols.put(c - '0', col);
                    ++numAgents;
                } else if ('A' <= c && c <= 'Z') {
                    boxes[row][col] = c;
                } else if (c == '+') {
                    walls[row][col] = true;
                }
            }
        }
        agentRows = new HashMap<>(agentRows);
        agentCols = new HashMap<>(agentCols);

        // Read goal state
        // line is currently "#goal"
        char[][] goals = new char[numRows][numCols];
        line = serverMessages.readLine();
        int row = 0;
        while (!line.startsWith("#")) {
            for (int col = 0; col < line.length(); ++col) {
                char c = line.charAt(col);

                if (('0' <= c && c <= '9') || ('A' <= c && c <= 'Z')) {
                    goals[row][col] = c;
                }
            }

            ++row;
            line = serverMessages.readLine();
        }

        // End
        // line is currently "#end"
        return new State(agentRows, agentCols, agentColors, walls, boxes, boxColors, goals);
    }

    public void readColorHeader(List<String> lines) {
        int i = 0;
        String line;
        for (int j = 1; j < lines.size(); j++) {
            line = lines.get(j);
            if (line.startsWith("#")) {
                break;
            }
            String[] split = line.split(":");
            irlclient.Color color = Color.fromString(split[0].strip());
            String[] entities = split[1].split(",");
            for (String entity : entities) {
                char c = entity.strip().charAt(0);
                if ('0' <= c && c <= '9') {
                    this.constAgentColor.put(c - '0', color);
                } else if ('A' <= c && c <= 'Z') {
                    this.constBoxColor.put(c, color);
                }
            }
            i = j;
        }


        i = i + 2;
        int numRows = 0;
        int numCols = 0;
        for (int j = i; j < lines.size(); j++) {
            line = lines.get(j);
            if (lines.get(j).contains("#end")) {
                break;
            }
            numCols = Math.max(numCols, line.length());
            numRows++;
        }

        this.goals = new char[numRows][numCols];
        int row = 0;
        for (int j = i; j < lines.size(); j++) {
            line = lines.get(j);
            if (lines.get(j).contains("#end")) {
                break;
            }
            System.err.println(line);
            for (int col = 0; col < line.length(); ++col) {
                char c = line.charAt(col);

                if (('0' <= c && c <= '9') || ('A' <= c && c <= 'Z')) {
                    goals[row][col] = c;
                }
            }
            ++row;

        }
    }

    public State parseState(StringBuilder stringBuilder) {

        int numAgents = 0;
        String[] levelLines = stringBuilder.toString().split("\n");
        Map<Integer, Integer> agentRows = new HashMap<>();
        Map<Integer, Integer> agentCols = new HashMap<>();
        boolean[][] walls = new boolean[goals.length][goals[0].length];
        char[][] boxes = new char[goals.length][goals[0].length];
        for (int row = 0; row < goals.length; row++) {
            String line = levelLines[row];
            if (line.isEmpty()) {
                continue;
            }
            for (int col = 0; col < line.length(); ++col) {
                char c = line.charAt(col);

                if ('0' <= c && c <= '9') {
                    agentRows.put(c - '0', row);
                    agentCols.put(c - '0', col);
                    ++numAgents;
                } else if ('A' <= c && c <= 'Z') {
                    boxes[row][col] = c;
                } else if (c == '+') {
                    walls[row][col] = true;
                }
            }
        }
        agentRows = new HashMap<>(agentRows);
        agentCols = new HashMap<>(agentCols);

        return new State(agentRows, agentCols, this.constAgentColor, walls, boxes, this.constBoxColor, this.goals);
    }

    public State parseState(List<String> levelLines) {
        // We can assume that the level file is conforming to specification, since the server verifies this.
        // Read domain
        int i = 5;
        String line = levelLines.get(i);

        Map<Integer, Color> agentColors = new HashMap<>();
        Map<Character, Color> boxColors = new HashMap<>();

        while (!line.startsWith("#")) {
            String[] split = line.split(":");
            Color color = Color.fromString(split[0].strip());
            String[] entities = split[1].split(",");
            for (String entity : entities) {
                char c = entity.strip().charAt(0);
                if ('0' <= c && c <= '9') {
                    agentColors.put(c - '0', color);
                } else if ('A' <= c && c <= 'Z') {
                    boxColors.put(c, color);
                }
            }
            i++;
            line = levelLines.get(i);
        }

        // Read initial state
        // line is currently "#initial"
        int numRows = 0;
        int numCols = 0;
        ArrayList<String> initialLevelLines = new ArrayList<>(64);
        i++;
        line = levelLines.get(i);
        while (!line.startsWith("#")) {
            initialLevelLines.add(line);
            numCols = Math.max(numCols, line.length());
            ++numRows;
            i++;
            line = levelLines.get(i);
        }
        int numAgents = 0;
        Map<Integer, Integer> agentRows = new HashMap<>();
        Map<Integer, Integer> agentCols = new HashMap<>();
        boolean[][] walls = new boolean[numRows][numCols];
        char[][] boxes = new char[numRows][numCols];
        for (int row = 0; row < numRows; ++row) {
            line = initialLevelLines.get(row);
            for (int col = 0; col < line.length(); ++col) {
                char c = line.charAt(col);

                if ('0' <= c && c <= '9') {
                    agentRows.put(c - '0', row);
                    agentCols.put(c - '0', col);
                    ++numAgents;
                } else if ('A' <= c && c <= 'Z') {
                    boxes[row][col] = c;
                } else if (c == '+') {
                    walls[row][col] = true;
                }
            }
        }
        agentRows = new HashMap<>(agentRows);
        agentCols = new HashMap<>(agentCols);

        // Read goal state
        // line is currently "#goal"
        char[][] goals = new char[numRows][numCols];
        i++;
        line = levelLines.get(i);
        int row = 0;
        while (!line.startsWith("#")) {
            for (int col = 0; col < line.length(); ++col) {
                char c = line.charAt(col);

                if (('0' <= c && c <= '9') || ('A' <= c && c <= 'Z')) {
                    goals[row][col] = c;
                }
            }

            ++row;
            i++;
            line = levelLines.get(i);
        }

        // End
        // line is currently "#end"
        return new State(agentRows, agentCols, agentColors, walls, boxes, boxColors, goals);
    }
}

