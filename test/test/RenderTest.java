package test;

import jndev.pseudo3d.listener.Keyboard;
import jndev.pseudo3d.loader.Sprites;
import jndev.pseudo3d.object.Object;
import jndev.pseudo3d.renderer.Camera;
import jndev.pseudo3d.renderer.SimpleRenderer;
import jndev.pseudo3d.scene.Scene;
import jndev.pseudo3d.util.Vector;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

public class RenderTest extends JPanel implements ActionListener {
    
    private Scene scene;
    private Object object;
    
    public RenderTest() {
        scene = new Scene();
        object = new Object();
        object.setSprite(Sprites.get("front"));
        object.setGravity(0);
        object.setPosition(new Vector(0, 0, -40));
        scene.addObject(object);
        
        Camera camera = new Camera();
        camera.setFieldOfView(90);
        
        scene.setCamera(camera);
        scene.setBackground(Color.BLACK);
        
        setSize(new Dimension(500, 500));
        
        Timer timer = new Timer(10, this);
        timer.start();
        
        setVisible(true);
        requestFocus();
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        SimpleRenderer.render(scene, this, g);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (Keyboard.isPressed(KeyEvent.VK_W)) {
            object.setPosition(object.getPosition().setZ(object.getPosition().getZ() - 1));
            object.setSprite(Sprites.get("front"));
        }
        if (Keyboard.isPressed(KeyEvent.VK_S)) {
            object.setPosition(object.getPosition().setZ(object.getPosition().getZ() + 1));
            object.setSprite(Sprites.get("front"));
        }
        if (Keyboard.isPressed(KeyEvent.VK_A)) {
            object.setPosition(object.getPosition().setX(object.getPosition().getX() - 1));
            object.setSprite(Sprites.get("left"));
        }
        if (Keyboard.isPressed(KeyEvent.VK_D)) {
            object.setPosition(object.getPosition().setX(object.getPosition().getX() + 1));
            object.setSprite(Sprites.get("right"));
        }
        if (Keyboard.isPressed(KeyEvent.VK_UP))
            object.setPosition(object.getPosition().setY(object.getPosition().getY() + 1));
        if (Keyboard.isPressed(KeyEvent.VK_DOWN))
            object.setPosition(object.getPosition().setY(object.getPosition().getY() - 1));
        scene.tick();
    }
}