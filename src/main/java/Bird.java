import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.control.CharacterControl;
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

    private final CharacterControl jugador = new CharacterControl(
            new CapsuleCollisionShape(1.5f, 6f, 1), 0.05f);
    private final Vector3f direccion = new Vector3f();
    private boolean izquierda = false, derecha = false, arriba = false, abajo = false;
    private final Vector3f tmpDireccion = new Vector3f();
    private final Vector3f tmpIzquierda = new Vector3f();
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
        switch (accion) {
            case "Izquierda":
                izquierda = presionado;
                break;
            case "Derecha":
                derecha = presionado;
                break;
            case "Arriba":
                arriba = presionado;
                break;
            case "Abajo":
                abajo = presionado;
                break;
            case "Brincar":
                if (presionado) jugador.jump();
                break;
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
        cam.getDirection().mult(0.6f, tmpDireccion);
        cam.getLeft().mult(0.4f, tmpIzquierda);
        direccion.zero();
        if (izquierda)
            direccion.addLocal(tmpIzquierda);
        if (derecha)
            direccion.addLocal(tmpIzquierda.negate());
        if (arriba)
            direccion.addLocal(tmpDireccion);
        if (abajo)
            direccion.addLocal(tmpDireccion.negate());
        jugador.setWalkDirection(direccion);
        cam.setLocation(jugador.getPhysicsLocation());
    }

    private void initEntorno() {
        stateManager.attach(fisicas);
        flyCam.setZoomSpeed(0);
        flyCam.setMoveSpeed(30);
        viewPort.setBackgroundColor(ColorRGBA.White);
        rootNode.attachChild(movibles);
        jugador.setJumpSpeed(20);
        jugador.setFallSpeed(30);
        jugador.setGravity(50f); //it must be set before MOVING the physics location.
        jugador.setPhysicsLocation(new Vector3f(0, 10, 0));
        fisicas.getPhysicsSpace().add(jugador);
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
        inputManager.addMapping("Izquierda", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("Derecha", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("Arriba", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("Abajo", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("Brincar", new KeyTrigger(KeyInput.KEY_LSHIFT));
        inputManager.addMapping("Brincar", new KeyTrigger(KeyInput.KEY_RSHIFT));
        inputManager.addListener(listenerAccion, "Agarrar");
        inputManager.addListener(listenerAnalogo, "Acercar");
        inputManager.addListener(listenerAnalogo, "Alejar");
        inputManager.addListener(listenerAccion, "Izquierda");
        inputManager.addListener(listenerAccion, "Derecha");
        inputManager.addListener(listenerAccion, "Arriba");
        inputManager.addListener(listenerAccion, "Abajo");
        inputManager.addListener(listenerAccion, "Brincar");
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
