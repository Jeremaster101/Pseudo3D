import jndev.pseudo3d.application.Game;
import jndev.pseudo3d.listener.Keyboard;
import jndev.pseudo3d.loader.Sprite;
import jndev.pseudo3d.object.PhysicsObject;
import jndev.pseudo3d.object.SpriteObject;
import jndev.pseudo3d.scene.Camera;
import jndev.pseudo3d.scene.Scene;
import jndev.pseudo3d.util.Vector;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;

/**
 * testing class for testing various bits of this project
 *
 * @author JNDev (Jeremaster101)
 */
public class Testing {
    
    /**
     * run the test scene
     *
     * @param args program arguments
     */
    public static void main(String[] args) {
        Sprite.load(new File("res/sprites/"));
        Scene scene = new Scene();
        PhysicsObject physicsObject = new PhysicsObject();
        physicsObject.setSprite(Sprite.get("res/sprites/player/front.png"));
        physicsObject.getBoundingBox().setWidth(physicsObject.getSprite().getWidth(null));
        physicsObject.getBoundingBox().setHeight(physicsObject.getSprite().getHeight(null));
        physicsObject.getBoundingBox().setDepth(physicsObject.getSprite().getWidth(null));
        physicsObject.setPosition(new Vector(physicsObject.getBoundingBox().getWidth() * 10 - 450, 0, -physicsObject.getBoundingBox().getWidth()));
        scene.addObject(physicsObject);
        
        for (int j = 1; j < 22; j++) {
            for (int i = 1; i <= 6; i++) {
                PhysicsObject copy = new PhysicsObject(physicsObject);
                copy.setSprite(Sprite.get("res/sprites/floor.png"));
                copy.getBoundingBox().setWidth(copy.getSprite().getWidth(null));
                copy.getBoundingBox().setHeight(copy.getSprite().getHeight(null));
                copy.getBoundingBox().setDepth(copy.getSprite().getWidth(null));
                copy.setPosition(new Vector(copy.getBoundingBox().getWidth() * j - 525, -465, -copy.getBoundingBox().getWidth() * i));
                copy.setGravity(new Vector());
                scene.addObject(copy);
            }
        }
        
        SpriteObject spriteObject = new SpriteObject();
        spriteObject.setSprite(Sprite.get("res/sprites/background.png"));
        spriteObject.setPosition(new Vector(0, 0, -325));
        scene.addObject(spriteObject);
        
        Camera camera = new Camera();
        camera.setFieldOfView(72);
        camera.setScenePosition(new Vector(0, 0, -100));
        camera.setSensorSize(1000);
        
        scene.setCamera(camera);
        scene.setBackground(Color.WHITE);
        
        Game.launch(1000, 1000, false);
        Game.getLoop().setActiveScene(scene);
        Game.getLoop().start();
        
        Game.getLoop().inject(() -> {
            if (Game.getLoop().getActiveScene().equals(scene)) {
                if (Keyboard.isPressed(KeyEvent.VK_W) && camera.getFieldOfView() > 0) {
                    physicsObject.setVelocity(physicsObject.getVelocity().setZ(-1));
                    physicsObject.setSprite(Sprite.get("res/sprites/player/front.png"));
                }
                
                if (Keyboard.isPressed(KeyEvent.VK_S) && camera.getFieldOfView() > 0) {
                    physicsObject.setVelocity(physicsObject.getVelocity().setZ(1));
                    physicsObject.setSprite(Sprite.get("res/sprites/player/front.png"));
                }

                if ((Keyboard.isPressed(KeyEvent.VK_W) && Keyboard.isPressed(KeyEvent.VK_S)) ||
                        camera.getFieldOfView() == 0) {
                    physicsObject.setVelocity(physicsObject.getVelocity().setZ(0));
                }
                
                if (Keyboard.isPressed(KeyEvent.VK_A)) {
                    physicsObject.setVelocity(physicsObject.getVelocity().setX(-1));
                    physicsObject.setSprite(Sprite.get("res/sprites/player/left.png"));
                }
                
                if (Keyboard.isPressed(KeyEvent.VK_D)) {
                    physicsObject.setVelocity(physicsObject.getVelocity().setX(1));
                    physicsObject.setSprite(Sprite.get("res/sprites/player/right.png"));
                }

                if (Keyboard.isPressed(KeyEvent.VK_A) && Keyboard.isPressed(KeyEvent.VK_D)) {
                    physicsObject.setVelocity(physicsObject.getVelocity().setX(0));
                }
                
                if (Keyboard.isPressed(KeyEvent.VK_SPACE)) {
                    physicsObject.setVelocity(physicsObject.getVelocity().setY(1));
                }
                
                if (Keyboard.isPressed(KeyEvent.VK_SHIFT)) {
                    physicsObject.setVelocity(physicsObject.getVelocity().setY(-1));
                }

                if (Keyboard.isPressed(KeyEvent.VK_SPACE) && Keyboard.isPressed(KeyEvent.VK_SHIFT)) {
                    physicsObject.setVelocity(physicsObject.getVelocity().setY(0));
                }
                
                if (Keyboard.isPressed(KeyEvent.VK_UP)) {
                    camera.setFieldOfView(Math.min(camera.getFieldOfView() + 1, 72));
                }
                
                if (Keyboard.isPressed(KeyEvent.VK_DOWN)) {
                    camera.setFieldOfView(Math.max(camera.getFieldOfView() - 1, 0));
                }
            }
        });
    }
}
