import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.*;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
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

    private Vector3f w = null;
    private ColorRGBA color = null;
    private CollisionResult objetoActual = null;
    private final BulletAppState fisicas = new BulletAppState();
    private final Node movibles = new Node("movibles");
    private final ActionListener listenerAccion = (accion, presionado, tpf) -> {
        if (accion.equals("Agarrar")) {
            var resultados = new CollisionResults();
            movibles.collideWith(new Ray(cam.getLocation(), cam.getDirection()), resultados);
            if (resultados.size() > 0)
                if (presionado) {
                    objetoActual = resultados.getClosestCollision();
                    w = objetoActual.getGeometry().
                            getLocalTranslation().subtract(objetoActual.getContactPoint());
                    color = objetoActual.getGeometry().getMaterial().getParamValue("Color");
                    var d = 1 - Math.max(color.r, Math.max(color.g, color.b));
                    objetoActual.getGeometry().getMaterial().setColor("Color",
                            new ColorRGBA(color.r + d, color.g + d, color.b + d, 1));
                } else {
                    objetoActual.getGeometry().getMaterial().setColor("Color", color);
                    ((RigidBodyControl) objetoActual.getGeometry().getControl(0)).activate();
                    objetoActual = null;
                }
        }
    };
    private final AnalogListener listenerAnalogo = (accion, valor, tpf) -> {
        if (objetoActual != null) {
            if (accion.equals("Acercar"))
                objetoActual.setDistance(objetoActual.getDistance() - valor);
            if (accion.equals("Alejar"))
                objetoActual.setDistance(objetoActual.getDistance() + valor);
        }
    };

    @Override
    public void simpleInitApp() {
        initEntorno();
        initApuntador();
        initTeclas();
    }

    @Override
    public void simpleUpdate(float tpf) {
        if (objetoActual != null) {
            var rigidez = (RigidBodyControl) objetoActual.getGeometry().getControl(0);
            rigidez.setPhysicsLocation(
                    cam.getLocation().
                            add(cam.getDirection().normalize().mult(objetoActual.getDistance())).
                            add(w));
        }

    }

    private void initEntorno() {
        stateManager.attach(fisicas);
        flyCam.setZoomSpeed(0);
        flyCam.setMoveSpeed(30);
        viewPort.setBackgroundColor(ColorRGBA.White);
        rootNode.attachChild(movibles);
        hacerPiso();
        hacerCubo("tres", -2f, 3f, 1f);
        hacerCubo("dos", 1f, 2f, 0f);
        hacerCubo("uno", 0f, 1f, -2f);
        hacerCubo("cero", 1f, 0f, -4f);
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

    private void initTeclas() {
        inputManager.addMapping("Agarrar",
                new KeyTrigger(KeyInput.KEY_SPACE),
                new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addMapping("Acercar",
                new MouseAxisTrigger(MouseInput.AXIS_WHEEL, true));
        inputManager.addMapping("Alejar",
                new MouseAxisTrigger(MouseInput.AXIS_WHEEL, false));
        inputManager.addListener(listenerAccion, "Agarrar");
        inputManager.addListener(listenerAnalogo, "Acercar");
        inputManager.addListener(listenerAnalogo, "Alejar");
    }

    private void hacerCubo(String nombre, float x, float y, float z) {
        var cubo = new Geometry(nombre, new Box(1, 1, 1));
        cubo.setLocalTranslation(x, y, z);
        var rigidezCubo = new RigidBodyControl(2f);
        cubo.addControl(rigidezCubo);
        cubo.setMaterial(new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md") {{
            setColor("Color", ColorRGBA.randomColor());
        }});
        movibles.attachChild(cubo);
        fisicas.getPhysicsSpace().add(rigidezCubo);
    }

    private void hacerPiso() {
        var piso = new Geometry("piso", new Box(40, .1f, 40));
        piso.setLocalTranslation(0, -4, -5);
        var rigidezPiso = new RigidBodyControl(0);
        piso.addControl(rigidezPiso);
        piso.setMaterial(new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md") {{
            setColor("Color", ColorRGBA.Gray);
        }});
        rootNode.attachChild(piso);
        fisicas.getPhysicsSpace().add(rigidezPiso);
    }
}
