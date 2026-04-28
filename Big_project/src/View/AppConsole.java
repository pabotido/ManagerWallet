package View;

public final class AppConsole {
    private static final Object LOCK = new Object();

    public static void println(String text) {
        synchronized (LOCK) {
            System.out.println(text);
        }
    }
}
