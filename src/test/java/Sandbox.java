import javafx.scene.input.KeyCode;
import xyz.jeremynoesen.pseudo3d.Pseudo3D;
import xyz.jeremynoesen.pseudo3d.input.Keyboard;
import xyz.jeremynoesen.pseudo3d.scene.Scene;
import xyz.jeremynoesen.pseudo3d.scene.entity.Entity;
import xyz.jeremynoesen.pseudo3d.scene.entity.Sprite;
import xyz.jeremynoesen.pseudo3d.scene.render.Camera;
import xyz.jeremynoesen.pseudo3d.scene.util.Vector;

import java.io.FileNotFoundException;
import java.util.Random;

/**
 * sandbox-style testing class for testing various bits of this project
 *
 * @author Jeremy Noesen
 */
public class Sandbox {
    
    /**
     * run the sandbox test scene
     *
     * @param args program arguments
     */
    public static void main(String[] args) throws FileNotFoundException {
        Sprite playerFront = new Sprite(0.85f, 2, "src/test/resources/images/player/front.png");
        Sprite playerBack = new Sprite(0.85f, 2, "src/test/resources/images/player/back.png");
        Sprite playerLeft = new Sprite(0.6f, 2, "src/test/resources/images/player/left.png");
        Sprite playerRight = new Sprite(0.6f, 2, "src/test/resources/images/player/right.png");
        Sprite floor = new Sprite(1, 1, "src/test/resources/images/floor.png");
        Sprite background = new Sprite(16, 16, "src/test/resources/images/background.png");
        //load all sprites
        
        Pseudo3D.launch(500, 500, 60, 120, true, "Sandbox");
        //launch program
        
        Entity player = (Entity) new Entity()
                .setUpdateOffScreen(true)
                .setSprite(playerFront)
                .setDimensions(0.8f, 2, 0.8f);
        //create player entity
        
        Entity dummy = new Entity(player).setUpdateOffScreen(false);
        //create dummy entity
        
        Camera camera = new Camera().setFieldOfView(49);
        //create camera
        
        Scene scene = new Scene()
                .setGridScale(new Vector(48, 48, 48))
                .setBackground(background)
                .addEntity(player)
                .addEntity(dummy)
                .setCamera(camera);
        //init scene
        
        for (int j = -8; j <= 8; j++) {
            for (int i = -3; i <= 0; i++) {
                Entity block = (Entity) new Entity()
                        .setSprite(floor)
                        .setPosition(new Vector(j, -4.75f, i))
                        .setKinematic(false)
                        .setDimensions(1, 1, 1);
                scene.addEntity(block);
            }
        }
        //generate floor
        
        Pseudo3D.setActiveScene(scene);
        //set scene
        
        //the following adds controls to the scene
        scene.addLoopInjection(() -> {
            
            if (Keyboard.isPressed(KeyCode.W) && camera.getFieldOfView() > 0) {
                player.setVelocity(player.getVelocity().setZ(-2));
                player.setSprite(playerBack);
            }
            
            if (Keyboard.isPressed(KeyCode.S) && camera.getFieldOfView() > 0) {
                player.setVelocity(player.getVelocity().setZ(2));
                player.setSprite(playerFront);
            }
            
            if ((Keyboard.isPressed(KeyCode.W) && Keyboard.isPressed(KeyCode.S)) ||
                    camera.getFieldOfView() == 0) {
                player.setVelocity(player.getVelocity().setZ(0));
            }
            
            if (Keyboard.isPressed(KeyCode.A)) {
                player.setVelocity(player.getVelocity().setX(-2));
                player.setSprite(playerLeft);
            }
            
            if (Keyboard.isPressed(KeyCode.D)) {
                player.setVelocity(player.getVelocity().setX(2));
                player.setSprite(playerRight);
            }
            
            if (Keyboard.isPressed(KeyCode.A) && Keyboard.isPressed(KeyCode.D)) {
                player.setVelocity(player.getVelocity().setX(0));
            }
            
            if (Keyboard.isPressed(KeyCode.SPACE)) {
                player.setVelocity(player.getVelocity().setY(2));
            }
            
            if (Keyboard.isPressed(KeyCode.SHIFT)) {
                player.setVelocity(player.getVelocity().setY(-2));
            }
            
            if (Keyboard.isPressed(KeyCode.SPACE) && Keyboard.isPressed(KeyCode.SHIFT)) {
                player.setVelocity(player.getVelocity().setY(0));
            }
            
            if (Keyboard.isPressed(KeyCode.UP)) {
                camera.setFieldOfView(camera.getFieldOfView() + 0.5f);
            }
            
            if (Keyboard.isPressed(KeyCode.DOWN)) {
                camera.setFieldOfView(Math.max(camera.getFieldOfView() - 0.5f, 0));
            }
            
            if (Keyboard.isPressed(KeyCode.LEFT)) {
                camera.setRotation(camera.getRotation() + 0.5f);
            }
            
            if (Keyboard.isPressed(KeyCode.RIGHT)) {
                camera.setRotation(camera.getRotation() - 0.5f);
            }
            
            if (Keyboard.isPressed(KeyCode.F)) {
                Random random = new Random();
                camera.setOffset(new Vector(random.nextInt() % 4 - 2, random.nextInt() % 4 - 2));
                camera.setRotation(((random.nextInt() % 2) / 2.0f));
            }
            
            if (Keyboard.isPressed(KeyCode.R)) {
                camera.setOffset(new Vector());
                camera.setRotation(0);
                player.setPosition(new Vector());
                player.setVelocity(new Vector());
                player.setAcceleration(new Vector());
                dummy.setPosition(new Vector());
                dummy.setVelocity(new Vector());
                dummy.setAcceleration(new Vector());
                camera.setFieldOfView(49);
            }
        });
    }
}
