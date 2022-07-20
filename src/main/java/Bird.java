import com.jme3.app.SimpleApplication;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;

public class Bird extends SimpleApplication {
    public static void main(String[] args) {
        var app = new Bird();
        app.setDisplayFps(false);
        app.setShowSettings(false);
        app.setDisplayStatView(false);
        app.start();
    }

    private Node movibles = new Node("movibles");

    @Override
    public void simpleInitApp() {
        initEntorno();
        initApuntador();
    }

    private void initEntorno() {
        flyCam.setZoomSpeed(0);
        flyCam.setMoveSpeed(30);
        viewPort.setBackgroundColor(ColorRGBA.White);
        movibles.attachChild(hacerCubo("tres", -2f, 3f, 1f));
        movibles.attachChild(hacerCubo("dos", 1f, 2f, 0f));
        movibles.attachChild(hacerCubo("uno", 0f, 1f, -2f));
        movibles.attachChild(hacerCubo("cero", 1f, 0f, -4f));
        rootNode.attachChild(movibles);
        rootNode.attachChild(hacerPiso());
    }

    private void initApuntador() {
        guiNode.attachChild(new BitmapText(guiFont) {{
            setText("+");
            setSize(40);
            setColor(ColorRGBA.Black);
            setLocalTranslation(settings.getWidth() / 2f - getLineWidth() / 2f,
                    settings.getHeight() / 2f + getLineHeight() / 2f, 0);
        }});
    }

    private Geometry hacerCubo(String nombre, float x, float y, float z) {
        var cubo = new Geometry(nombre, new Box(1, 1, 1));
        cubo.setLocalTranslation(x, y, z);
        cubo.setMaterial(new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md") {{
            setColor("Color", ColorRGBA.randomColor());
        }});
        return cubo;
    }

    private Geometry hacerPiso() {
        var piso = new Geometry("piso", new Box(40, .1f, 40));
        piso.setLocalTranslation(0, -4, -5);
        piso.setMaterial(new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md") {{
            setColor("Color", ColorRGBA.Gray);
        }});
        return piso;
    }
}
