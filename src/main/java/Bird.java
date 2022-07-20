import com.jme3.app.SimpleApplication;

public class Bird extends SimpleApplication {
    public static void main(String[] args) {
        var app = new Bird();
        app.setDisplayFps(false);
        app.setShowSettings(false);
        app.setDisplayStatView(false);
        app.start();
    }

    @Override
    public void simpleInitApp() {

    }
}
