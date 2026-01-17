import game.DemoGame;

/**
 * Application Entry Point.
 * 
 * <p>
 * This class serves as the bootstrapper for the application. It keeps the global scope clean
 * by immediately delegating logic to the {@link DemoGame} instance.
 * </p>
 */
public class Main {
    /**
     * The main method.
     * @param args Command line arguments (not used).
     */
    public static void main(String[] args) {
        // Create and start the game loop
        new DemoGame().start();
    }
}