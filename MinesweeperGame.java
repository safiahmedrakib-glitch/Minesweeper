import java.util.Scanner;
import java.util.Random;
import java.util.HashMap;
import java.util.Map;

/**
 * COMPLETE MINESWEEPER GAME IN SINGLE FILE
 * Includes all 4 required design patterns:
 * 1. Singleton Pattern - GameManager
 * 2. Builder Pattern - BoardBuilder
 * 3. Abstract Factory Pattern - CellFactory
 * 4. Prototype Pattern - CellPrototype
 */

// Enums
enum GameDifficulty {
    BEGINNER,
    INTERMEDIATE,
    EXPERT,
    CUSTOM
}

enum GameState {
    NOT_STARTED,
    PLAYING,
    WON,
    LOST
}

// Cell class
class Cell {
    private boolean isMine;
    private boolean isRevealed;
    private boolean isFlagged;
    private int adjacentMines;
    
    public Cell(boolean isMine) {
        this.isMine = isMine;
        this.isRevealed = false;
        this.isFlagged = false;
        this.adjacentMines = 0;
    }
    
    public boolean isMine() { return isMine; }
    public boolean isRevealed() { return isRevealed; }
    public boolean isFlagged() { return isFlagged; }
    public int getAdjacentMines() { return adjacentMines; }
    
    public void setRevealed(boolean revealed) { this.isRevealed = revealed; }
    public void setFlagged(boolean flagged) { this.isFlagged = flagged; }
    public void setAdjacentMines(int count) { this.adjacentMines = count; }
    
    public void reveal() {
        if (!isFlagged) {
            isRevealed = true;
        }
    }
    
    public void toggleFlag() {
        if (!isRevealed) {
            isFlagged = !isFlagged;
        }
    }
}

// Prototype Pattern
class CellPrototype {
    private Map<String, Cell> prototypes;
    
    public CellPrototype() {
        prototypes = new HashMap<>();
        initializePrototypes();
    }
    
    private void initializePrototypes() {
        prototypes.put("safe", new Cell(false));
        prototypes.put("mine", new Cell(true));
    }
    
    public Cell createCell(boolean isMine) {
        String key = isMine ? "mine" : "safe";
        Cell prototype = prototypes.get(key);
        
        if (prototype != null) {
            return new Cell(isMine);
        }
        
        throw new IllegalArgumentException("Unknown cell type: " + key);
    }
}

// Abstract Factory Pattern
interface CellFactory {
    Cell createCell(boolean isMine);
    int getRows();
    int getCols();
    int getMines();
}

class GameFactory implements CellFactory {
    private int rows;
    private int cols;
    private int mines;
    private CellPrototype cellPrototype;
    
    public GameFactory(int rows, int cols, int mines) {
        this.rows = rows;
        this.cols = cols;
        this.mines = mines;
        this.cellPrototype = new CellPrototype();
    }
    
    @Override
    public Cell createCell(boolean isMine) {
        return cellPrototype.createCell(isMine);
    }
    
    @Override
    public int getRows() {
        return rows;
    }
    
    @Override
    public int getCols() {
        return cols;
    }
    
    @Override
    public int getMines() {
        return mines;
    }
}

class DifficultyFactory {
    public static CellFactory getFactory(GameDifficulty difficulty) {
        switch (difficulty) {
            case BEGINNER:
                return new GameFactory(8, 8, 10);
            case INTERMEDIATE:
                return new GameFactory(16, 16, 40);
            case EXPERT:
                return new GameFactory(16, 30, 99);
            case CUSTOM:
                return new GameFactory(10, 10, 15);
            default:
                throw new IllegalArgumentException("Unknown difficulty: " + difficulty);
        }
    }
}

// Board class
class Board {
    private Cell[][] grid;
    private int rows;
    private int cols;
    private int totalMines;
    private int revealedCells;
    private int flaggedCells;
    private boolean hitMine;
    
    public Board(int rows, int cols, int mines) {
        this.rows = rows;
        this.cols = cols;
        this.totalMines = mines;
        this.revealedCells = 0;
        this.flaggedCells = 0;
        this.hitMine = false;
        initializeBoard();
    }
    
    private void initializeBoard() {
        createGrid();
        placeMines();
        calculateAdjacentMines();
    }
    
    private void createGrid() {
        grid = new Cell[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                grid[i][j] = new Cell(false);
            }
        }
    }
    
    private void placeMines() {
        Random random = new Random();
        int minesPlaced = 0;
        
        while (minesPlaced < totalMines) {
            int row = random.nextInt(rows);
            int col = random.nextInt(cols);
            
            if (!grid[row][col].isMine()) {
                grid[row][col] = new Cell(true);
                minesPlaced++;
            }
        }
    }
    
    private void calculateAdjacentMines() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (!grid[i][j].isMine()) {
                    int count = countAdjacentMines(i, j);
                    grid[i][j].setAdjacentMines(count);
                }
            }
        }
    }
    
    private int countAdjacentMines(int row, int col) {
        int count = 0;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) continue;
                
                int newRow = row + i;
                int newCol = col + j;
                
                if (isValidPosition(newRow, newCol) && grid[newRow][newCol].isMine()) {
                    count++;
                }
            }
        }
        return count;
    }
    
    public void revealCell(int row, int col) {
        if (!isValidPosition(row, col) || grid[row][col].isRevealed() || grid[row][col].isFlagged()) {
            return;
        }
        
        grid[row][col].reveal();
        revealedCells++;
        
        if (grid[row][col].isMine()) {
            hitMine = true;
            return;
        }
        
        if (grid[row][col].getAdjacentMines() == 0) {
            revealAdjacentCells(row, col);
        }
    }
    
    private void revealAdjacentCells(int row, int col) {
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) continue;
                
                int newRow = row + i;
                int newCol = col + j;
                
                if (isValidPosition(newRow, newCol) && 
                    !grid[newRow][newCol].isRevealed() && 
                    !grid[newRow][newCol].isFlagged()) {
                    revealCell(newRow, newCol);
                }
            }
        }
    }
    
    public void toggleFlag(int row, int col) {
        if (isValidPosition(row, col) && !grid[row][col].isRevealed()) {
            grid[row][col].toggleFlag();
            if (grid[row][col].isFlagged()) {
                flaggedCells++;
            } else {
                flaggedCells--;
            }
        }
    }
    
    public void revealAll() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                grid[i][j].setRevealed(true);
            }
        }
    }
    
    public boolean hasHitMine() {
        return hitMine;
    }
    
    public boolean isAllSafeCellsRevealed() {
        int safeCells = (rows * cols) - totalMines;
        return revealedCells == safeCells;
    }
    
    public int getRemainingMines() {
        return totalMines - flaggedCells;
    }
    
    private boolean isValidPosition(int row, int col) {
        return row >= 0 && row < rows && col >= 0 && col < cols;
    }
    
    public String display() {
        StringBuilder sb = new StringBuilder();
        
        // Column numbers
        sb.append("   ");
        for (int j = 0; j < cols; j++) {
            sb.append(String.format("%2d ", j));
        }
        sb.append("\n");
        
        for (int i = 0; i < rows; i++) {
            sb.append(String.format("%2d ", i)); // Row numbers
            
            for (int j = 0; j < cols; j++) {
                Cell cell = grid[i][j];
                
                if (cell.isFlagged()) {
                    sb.append(" F ");
                } else if (!cell.isRevealed()) {
                    sb.append(" ■ ");
                } else if (cell.isMine()) {
                    sb.append(" * ");
                } else if (cell.getAdjacentMines() > 0) {
                    sb.append(String.format(" %d ", cell.getAdjacentMines()));
                } else {
                    sb.append(" . ");
                }
            }
            sb.append("\n");
        }
        
        return sb.toString();
    }
}

// Builder Pattern
class BoardBuilder {
    private int rows;
    private int cols;
    private int mines;
    private GameDifficulty difficulty;
    
    public BoardBuilder() {
        this.rows = 8;
        this.cols = 8;
        this.mines = 10;
        this.difficulty = GameDifficulty.BEGINNER;
    }
    
    public BoardBuilder setRows(int rows) {
        this.rows = rows;
        return this;
    }
    
    public BoardBuilder setCols(int cols) {
        this.cols = cols;
        return this;
    }
    
    public BoardBuilder setMines(int mines) {
        this.mines = mines;
        return this;
    }
    
    public BoardBuilder setDifficulty(GameDifficulty difficulty) {
        this.difficulty = difficulty;
        // Set appropriate dimensions based on difficulty
        switch (difficulty) {
            case BEGINNER:
                this.rows = 8; this.cols = 8; this.mines = 10;
                break;
            case INTERMEDIATE:
                this.rows = 16; this.cols = 16; this.mines = 40;
                break;
            case EXPERT:
                this.rows = 16; this.cols = 30; this.mines = 99;
                break;
        }
        return this;
    }
    
    public Board build() {
        validateParameters();
        return new Board(rows, cols, mines);
    }
    
    private void validateParameters() {
        if (rows <= 0 || cols <= 0) {
            throw new IllegalArgumentException("Rows and columns must be positive");
        }
        if (mines <= 0 || mines >= rows * cols) {
            throw new IllegalArgumentException("Invalid number of mines");
        }
    }
}

// Singleton Pattern
class GameManager {
    private static GameManager instance;
    private Board board;
    private GameState gameState;
    private Scanner scanner;
    
    private GameManager() {
        this.scanner = new Scanner(System.in);
        this.gameState = GameState.NOT_STARTED;
    }
    
    public static GameManager getInstance() {
        if (instance == null) {
            instance = new GameManager();
        }
        return instance;
    }
    
    public void startGame() {
        System.out.println("🎮 Welcome to Minesweeper!");
        System.out.println("DESIGN PATTERNS USED:");
        System.out.println("1. Singleton - GameManager");
        System.out.println("2. Builder - BoardBuilder");
        System.out.println("3. Abstract Factory - CellFactory");
        System.out.println("4. Prototype - CellPrototype");
        
        GameDifficulty difficulty = chooseDifficulty();
        
        this.board = new BoardBuilder()
                .setDifficulty(difficulty)
                .build();
        
        this.gameState = GameState.PLAYING;
        playGame();
    }
    
    private GameDifficulty chooseDifficulty() {
        System.out.println("\nChoose Difficulty:");
        System.out.println("1. BEGINNER (8x8, 10 mines)");
        System.out.println("2. INTERMEDIATE (16x16, 40 mines)");
        System.out.println("3. EXPERT (16x30, 99 mines)");
        System.out.print("Enter choice (1-3): ");
        
        int choice = scanner.nextInt();
        switch (choice) {
            case 1: return GameDifficulty.BEGINNER;
            case 2: return GameDifficulty.INTERMEDIATE;
            case 3: return GameDifficulty.EXPERT;
            default: return GameDifficulty.BEGINNER;
        }
    }
    
    private void playGame() {
        while (gameState == GameState.PLAYING) {
            displayBoard();
            processMove();
            checkGameState();
        }
        
        displayFinalResult();
        scanner.close();
    }
    
    private void displayBoard() {
        System.out.println("\n" + board.display());
        System.out.println("Mines left: " + board.getRemainingMines());
    }
    
    private void processMove() {
        System.out.print("Enter row, column and action (r for reveal, f for flag): ");
        int row = scanner.nextInt();
        int col = scanner.nextInt();
        char action = scanner.next().charAt(0);
        
        if (action == 'r') {
            board.revealCell(row, col);
        } else if (action == 'f') {
            board.toggleFlag(row, col);
        } else {
            System.out.println("Invalid action! Use 'r' to reveal or 'f' to flag.");
        }
    }
    
    private void checkGameState() {
        if (board.hasHitMine()) {
            gameState = GameState.LOST;
        } else if (board.isAllSafeCellsRevealed()) {
            gameState = GameState.WON;
        }
    }
    
    private void displayFinalResult() {
        board.revealAll();
        System.out.println("\n" + board.display());
        
        if (gameState == GameState.WON) {
            System.out.println("🎉 Congratulations! You won!");
        } else {
            System.out.println("💥 Game Over! You hit a mine!");
        }
    }
}

// Main class
public class MinesweeperGame {
    public static void main(String[] args) {
        System.out.println("🚀 Starting Minesweeper Game...");
        GameManager game = GameManager.getInstance();
        game.startGame();
    }
}