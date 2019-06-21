import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.logging.Handler;
import java.util.logging.ConsoleHandler;

public class ModifiedMain {

    private static final Logger instrumentationLogger = Logger.getLogger(ModifiedMain.class.getSimpleName());

    private static final Handler instrumentationHandler = new ConsoleHandler();

    public static void main(String[] args) {
        instrumentationHandler.setLevel(Level.ALL);
        instrumentationLogger.addHandler(instrumentationHandler);
        instrumentationLogger.setLevel(Level.ALL);
        int max = 1000;
        {
            instrumentationLogger.log(Level.FINE, "Entering to SwitchStmt block in Main.java class, in * method.");
            try {
                switch(max) {
                    case 1000:
                        max = 0;
                        break;
                    case 0:
                        max = 1000;
                        break;
                }
            } catch (Throwable ex) {
                instrumentationLogger.log(Level.FINE, "Exit with exception from SwitchStmt block in Main.java class, in * method.");
            } finally {
            }
            instrumentationLogger.log(Level.FINE, "Successful exit from SwitchStmt block in Main.java class, in * method.");
        }
    }

    public static void wake() {
        instrumentationHandler.setLevel(Level.ALL);
        instrumentationLogger.addHandler(instrumentationHandler);
        instrumentationLogger.setLevel(Level.ALL);
        int max = 1000;
        {
            instrumentationLogger.log(Level.FINE, "Entering to SwitchStmt block in Main.java class, in * method.");
            try {
                switch(max) {
                    case 1000:
                        max = 0;
                        break;
                    case 0:
                        max = 1000;
                        break;
                }
            } catch (Throwable ex) {
                instrumentationLogger.log(Level.FINE, "Exit with exception from SwitchStmt block in Main.java class, in * method.");
            } finally {
            }
            instrumentationLogger.log(Level.FINE, "Successful exit from SwitchStmt block in Main.java class, in * method.");
        }
    }

    public static String sleep() {
        instrumentationHandler.setLevel(Level.ALL);
        instrumentationLogger.addHandler(instrumentationHandler);
        instrumentationLogger.setLevel(Level.ALL);
        {
            try {
                for (int i = 0; i < 10; i++) {
                    System.out.println(i);
                }
            } finally {
                instrumentationLogger.log(Level.FINE, "Exiting from ForStmt block in Main.java class, in * method.");
            }
        }
        return "sleep";
    }
}
