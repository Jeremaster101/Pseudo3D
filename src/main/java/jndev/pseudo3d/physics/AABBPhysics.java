package jndev.pseudo3d.physics;

import jndev.pseudo3d.scene.Scene;
import jndev.pseudo3d.sceneobject.Renderable;
import jndev.pseudo3d.util.Box;
import jndev.pseudo3d.util.FastMath;
import jndev.pseudo3d.util.Side;
import jndev.pseudo3d.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

/**
 * axis-aligned bounding box object physics
 *
 * @author JNDev (Jeremaster101)
 */
public abstract class AABBPhysics {
    
    /**
     * gravity applied to the object (pixels / tick ^ 2))
     */
    private Vector gravity;
    
    /**
     * position of object (pixels)
     */
    private Vector position;
    
    /**
     * velocity of object, or rate of change of position (pixels / tick)
     */
    private Vector velocity;
    
    /**
     * acceleration of object, or rate of change of velocity (pixels / tick ^ 2)
     */
    private Vector acceleration;
    
    /**
     * jerk of object, or rate of change of acceleration (pixels / tick ^ 3)
     */
    private Vector jerk;
    
    /**
     * +/- terminal velocity of gravity acceleration of object (pixels / tick)
     */
    private Vector terminalVelocity;
    
    /**
     * +/- drag applied to the object, used to slow down per direction. for simplicity, this is an acceleration vector
     * (pixels / tick ^ 2)
     */
    private Vector drag;
    
    /**
     * +/- friction applied to colliding objects. for simplicity, this is an acceleration vector (pixels / tick ^ 2)
     */
    private Vector friction;
    
    /**
     * scene the object is colliding in
     */
    private Scene scene;
    
    /**
     * if an object can collide with others
     */
    private boolean collidable;
    
    /**
     * list of objects this one is colliding with per side
     */
    private final HashMap<Side, ArrayList<AABBPhysics>> collidingObjects;
    
    /**
     * object's collision status
     */
    private boolean colliding;
    
    /**
     * object's overlapping status
     */
    private boolean overlapping;
    
    /**
     * whether this object can move or not and feel motion of other objects. this is best used for ground objects.
     * setting this to false will disallow this object from having any updates to motion or collisions, which can
     * prevent you from getting the data from its collisions. these objects can remain collidable.
     */
    private boolean kinematic;
    
    /**
     * whether this object can be pushed by other objects
     */
    private boolean pushable;
    
    /**
     * mass of an object used for calculations, such as momentum conservation
     */
    private float mass;
    
    /**
     * bounding box for collisions
     */
    private Box box;
    
    /**
     * create a new aabb object with default values
     */
    public AABBPhysics() {
        gravity = new Vector(0, -0.1f, 0);
        position = new Vector();
        velocity = new Vector();
        acceleration = new Vector();
        jerk = new Vector();
        terminalVelocity = new Vector(10, 10, 10);
        drag = new Vector(0.005f, 0.005f, 0.005f);
        friction = new Vector(0.05f, 0.05f, 0.05f);
        scene = null;
        collidable = true;
        colliding = false;
        overlapping = false;
        kinematic = true;
        pushable = false;
        mass = 1.0f;
        box = new Box();
        collidingObjects = new HashMap<>();
        for (Side s : Side.values()) collidingObjects.put(s, new ArrayList<>());
    }
    
    /**
     * copy constructor for aabb objects
     *
     * @param aabbPhysics aabb object to copy
     */
    public AABBPhysics(AABBPhysics aabbPhysics) {
        gravity = aabbPhysics.gravity;
        position = aabbPhysics.position;
        velocity = aabbPhysics.velocity;
        terminalVelocity = aabbPhysics.terminalVelocity;
        acceleration = aabbPhysics.acceleration;
        jerk = aabbPhysics.jerk;
        drag = aabbPhysics.drag;
        friction = aabbPhysics.friction;
        scene = aabbPhysics.scene;
        collidable = aabbPhysics.collidable;
        colliding = aabbPhysics.colliding;
        overlapping = aabbPhysics.overlapping;
        box = new Box(aabbPhysics.box);
        mass = aabbPhysics.mass;
        kinematic = aabbPhysics.kinematic;
        pushable = aabbPhysics.pushable;
        collidingObjects = new HashMap<>();
        for (Side s : Side.values()) collidingObjects.put(s, new ArrayList<>());
    }
    
    /**
     * update the motion of the object in 3D space using jerk, acceleration, velocity, and position
     */
    public void tickMotion() {
        acceleration = acceleration.add(jerk);
        //update acceleration based on jerk
        
        velocity = velocity.add(acceleration);
        //update velocity based on acceleration
        
        float vx = velocity.getX(), vy = velocity.getY(), vz = velocity.getZ();
        
        if (vx > -terminalVelocity.getX() && gravity.getX() < 0)
            vx = FastMath.max(vx + gravity.getX(), -terminalVelocity.getX());
        else if (vx < terminalVelocity.getX() && gravity.getX() > 0)
            vx = FastMath.min(vx + gravity.getX(), terminalVelocity.getX());
        
        if (vy > -terminalVelocity.getY() && gravity.getY() < 0)
            vy = FastMath.max(vy + gravity.getY(), -terminalVelocity.getY());
        else if (vy < terminalVelocity.getY() && gravity.getY() > 0)
            vy = FastMath.min(vy + gravity.getY(), terminalVelocity.getY());
        
        if (vz > -terminalVelocity.getZ() && gravity.getZ() < 0)
            vz = FastMath.max(vz + gravity.getZ(), -terminalVelocity.getZ());
        else if (vz < terminalVelocity.getZ() && gravity.getZ() > 0)
            vz = FastMath.min(vz + gravity.getZ(), terminalVelocity.getZ());
        //apply gravity if not exceeding terminal velocity
        
        float fx = 0, fy = 0, fz = 0;
        if (colliding) {
            int xCount = 0, yCount = 0, zCount = 0;
            for (Side side : Side.values()) {
                for (AABBPhysics aabbPhysics : collidingObjects.get(side)) {
                    if ((side == Side.LEFT && vx < 0) || (side == Side.RIGHT && vx > 0)) {
                        if (aabbPhysics.pushable && aabbPhysics.kinematic) {
                            float sum = mass + aabbPhysics.mass;
                            float diff = mass - aabbPhysics.mass;
                            float v1 = vx;
                            float v2 = aabbPhysics.velocity.getX();
                            vx = ((diff / sum) * v1) + ((2 * aabbPhysics.mass / sum) * v2);
                            aabbPhysics.velocity = aabbPhysics.velocity.setX(((-diff / sum) * v2) + ((2 * mass / sum) * v1));
                        } else vx = 0;
                        fy += aabbPhysics.friction.getY();
                        yCount++;
                        fz += aabbPhysics.friction.getZ();
                        zCount++;
                    } else if ((side == Side.BOTTOM && vy < 0) || (side == Side.TOP && vy > 0)) {
                        if (aabbPhysics.pushable && aabbPhysics.kinematic) {
                            float sum = mass + aabbPhysics.mass;
                            float diff = mass - aabbPhysics.mass;
                            float v1 = vy;
                            float v2 = aabbPhysics.velocity.getY();
                            vy = ((diff / sum) * v1) + ((2 * aabbPhysics.mass / sum) * v2);
                            aabbPhysics.velocity = aabbPhysics.velocity.setY(((-diff / sum) * v2) + ((2 * mass / sum) * v1));
                        } else vy = 0;
                        fx += aabbPhysics.friction.getX();
                        xCount++;
                        fz += aabbPhysics.friction.getZ();
                        zCount++;
                    } else if ((side == Side.BACK && vz < 0) || (side == Side.FRONT && vz > 0)) {
                        if (aabbPhysics.pushable && aabbPhysics.kinematic) {
                            float sum = mass + aabbPhysics.mass;
                            float diff = mass - aabbPhysics.mass;
                            float v1 = vz;
                            float v2 = aabbPhysics.velocity.getZ();
                            vz = ((diff / sum) * v1) + ((2 * aabbPhysics.mass / sum) * v2);
                            aabbPhysics.velocity = aabbPhysics.velocity.setZ(((-diff / sum) * v2) + ((2 * mass / sum) * v1));
                        } else vz = 0;
                        fx += aabbPhysics.friction.getX();
                        xCount++;
                        fy += aabbPhysics.friction.getY();
                        yCount++;
                    }
                }
            }
            //get sum of frictions for each axis
            
            if (xCount > 0) fx = (fx + friction.getX()) / (xCount + 1);
            if (yCount > 0) fy = (fy + friction.getY()) / (yCount + 1);
            if (zCount > 0) fz = (fz + friction.getZ()) / (zCount + 1);
            //calculate average friction per axis that has friction applied
        }
        //apply friction from colliding objects
        
        if (vx < 0) vx = FastMath.min(vx + drag.getX() + fx, 0);
        else if (vx > 0) vx = FastMath.max(vx - drag.getX() - fx, 0);
        if (vy < 0) vy = FastMath.min(vy + drag.getY() + fy, 0);
        else if (vy > 0) vy = FastMath.max(vy - drag.getY() - fy, 0);
        if (vz < 0) vz = FastMath.min(vz + drag.getZ() + fz, 0);
        else if (vz > 0) vz = FastMath.max(vz - drag.getZ() - fz, 0);
        //modify velocity based on friction and drag
        
        velocity = new Vector(vx, vy, vz);
        //set new velocity
        
        setPosition(position.add(velocity));
        //update position based on velocity
    }
    
    /**
     * check if a object has collided with this object
     */
    public void tickCollisions() {
        colliding = false;
        overlapping = false;
        collidingObjects.values().forEach(ArrayList::clear);
        //reset all collision data
        
        for (Renderable object : scene.getObjects()) {
            //loop through all renderable objects in scene
            
            if (object instanceof AABBPhysics aabbPhysics && aabbPhysics != this) {
                //check for AABBPhysics objects
                
                if (box.overlaps(aabbPhysics.getBoundingBox())) {
                    //check for an overlap
                    if (aabbPhysics.isCollidable() && collidable) {
                        //if this and other object can collide
                        collideWith(aabbPhysics);
                        //do the collision calculations
                    } else {
                        overlapping = true;
                        //set overlapping
                    }
                }
            }
        }
    }
    
    /**
     * fix the position of this object to make a collision occur
     *
     * @param aabbPhysics object colliding with this object
     */
    private void collideWith(AABBPhysics aabbPhysics) {
        float[] overlaps = new float[6];
        overlaps[0] = Math.abs(box.getMinimum().getX() - aabbPhysics.getBoundingBox().getMaximum().getX()); //left
        overlaps[1] = Math.abs(box.getMaximum().getX() - aabbPhysics.getBoundingBox().getMinimum().getX()); //right
        overlaps[2] = Math.abs(box.getMinimum().getY() - aabbPhysics.getBoundingBox().getMaximum().getY()); //bottom
        overlaps[3] = Math.abs(box.getMaximum().getY() - aabbPhysics.getBoundingBox().getMinimum().getY()); //top
        overlaps[4] = Math.abs(box.getMinimum().getZ() - aabbPhysics.getBoundingBox().getMaximum().getZ()); //back
        overlaps[5] = Math.abs(box.getMaximum().getZ() - aabbPhysics.getBoundingBox().getMinimum().getZ()); //front
        //get overlap distances
        
        int zeros = 0;
        int axis = 1;
        int dir = -1;
        float distance = overlaps[0];
        for (int i = 0; i < 6; i++) {
            if (overlaps[i] < distance) {
                distance = overlaps[i];
                dir = -FastMath.pow(-1, i);
                if (i <= 1) {
                    axis = 1;
                } else if (i <= 3) {
                    axis = 2;
                } else {
                    axis = 3;
                }
            }
            //find min overlap, direction, and axis of collision
            
            if (overlaps[i] == 0) zeros++;
            //check for 0 distance overlaps
        }
        
        if (zeros > 1) return;
        //if object has more than one 0 overlaps, it is technically not touching, so stop collision
        
        colliding = true;
        //set object to colliding
        
        if (axis == 1) {
            // check if collision is on this axis (1 = x, 2 = y, 3 = z)
            if (velocity.getX() * dir > 0) {
                // check if object is moving in proper direction on the axis
                distance *= Math.abs(velocity.getX()) / (Math.abs(velocity.getX()) + Math.abs(aabbPhysics.velocity.getX()));
                // scale distance based on object velocities to improve collision accuracy
                setPosition(position.setX(position.getX() - (distance * dir)));
                // fix object position so it is not overlapping
            }
            collidingObjects.get(dir == -1 ? Side.LEFT : Side.RIGHT).add(aabbPhysics);
            //add to colliding objects for the colliding side
        } else if (axis == 2) {
            if (velocity.getY() * dir > 0) {
                distance *= Math.abs(velocity.getY()) / (Math.abs(velocity.getY()) + Math.abs(aabbPhysics.velocity.getY()));
                setPosition(position.setY(position.getY() - (distance * dir)));
            }
            collidingObjects.get(dir == -1 ? Side.BOTTOM : Side.TOP).add(aabbPhysics);
        } else {
            if (velocity.getZ() * dir > 0) {
                distance *= Math.abs(velocity.getZ()) / (Math.abs(velocity.getZ()) + Math.abs(aabbPhysics.velocity.getZ()));
                setPosition(position.setZ(position.getZ() - (distance * dir)));
            }
            collidingObjects.get(dir == -1 ? Side.BACK : Side.FRONT).add(aabbPhysics);
        }
    }
    
    /**
     * get the position vector of the object
     *
     * @return position vector of object
     */
    public Vector getPosition() {
        return position;
    }
    
    /**
     * set the position of the object
     *
     * @param position position vector
     */
    public void setPosition(Vector position) {
        this.position = position;
        box.setPosition(position);
    }
    
    /**
     * get the velocity vector of the object
     *
     * @return velocity vector of an object
     */
    public Vector getVelocity() {
        return velocity;
    }
    
    /**
     * set the velocity of the object
     *
     * @param velocity velocity vector
     */
    public void setVelocity(Vector velocity) {
        this.velocity = velocity;
    }
    
    /**
     * get the acceleration vector of the object
     *
     * @return acceleration vector of an object
     */
    public Vector getAcceleration() {
        return acceleration;
    }
    
    /**
     * set the acceleration of the object
     *
     * @param acceleration acceleration vector
     */
    public void setAcceleration(Vector acceleration) {
        this.acceleration = acceleration;
    }
    
    /**
     * get the jerk vector of the object
     *
     * @return jerk of the object
     */
    public Vector getJerk() {
        return jerk;
    }
    
    /**
     * set the jerk of the object
     *
     * @param jerk jerk vector
     */
    public void setJerk(Vector jerk) {
        this.jerk = jerk;
    }
    
    /**
     * get the gravity applied to the object
     *
     * @return gravity vector
     */
    public Vector getGravity() {
        return gravity;
    }
    
    /**
     * set the gravity applied to the object
     *
     * @param gravity gravity vector
     */
    public void setGravity(Vector gravity) {
        this.gravity = gravity;
    }
    
    /**
     * get terminal velocity for this object
     *
     * @return terminal velocity
     */
    public Vector getTerminalVelocity() {
        return terminalVelocity;
    }
    
    /**
     * set the terminal velocity for this object
     *
     * @param terminalVelocity terminal velocity
     */
    public void setTerminalVelocity(Vector terminalVelocity) {
        this.terminalVelocity = terminalVelocity;
    }
    
    /**
     * get the drag of the object
     *
     * @return drag vector of an object
     */
    public Vector getDrag() {
        return drag;
    }
    
    /**
     * set the drag for the object
     *
     * @param drag drag of object
     */
    public void setDrag(Vector drag) {
        this.drag = drag;
    }
    
    /**
     * get the friction of the object
     *
     * @return friction vector
     */
    public Vector getFriction() {
        return friction;
    }
    
    /**
     * set the friction of the object
     *
     * @param friction friction vector
     */
    public void setFriction(Vector friction) {
        this.friction = friction;
    }
    
    /**
     * set the scene this object is in
     *
     * @param scene scene for collisions
     */
    public void setScene(Scene scene) {
        this.scene = scene;
    }
    
    /**
     * get the scene the collisions are set to occur in
     *
     * @return scene for collisions
     */
    public Scene getScene() {
        return scene;
    }
    
    /**
     * check if an object can be collided with
     *
     * @return true if collidable
     */
    public boolean isCollidable() {
        return collidable;
    }
    
    /**
     * enable or disable collisions for the object
     *
     * @param collidable true to allow collisions
     */
    public void setCollidable(boolean collidable) {
        this.collidable = collidable;
    }
    
    /**
     * check if the object is currently colliding with another object
     *
     * @return true if the object is colliding with another object
     */
    public boolean isColliding() {
        return colliding;
    }
    
    /**
     * check if an= object collides with this object
     *
     * @param aabbPhysics object to check if colliding with
     * @return true if this object collides with the other object
     */
    public boolean collidesWith(AABBPhysics aabbPhysics) {
        
        for (ArrayList<AABBPhysics> list : collidingObjects.values()) {
            if (list.contains(aabbPhysics)) return true;
        }
        
        return false;
    }
    
    /**
     * check if this object is colliding on the specified side
     *
     * @param side side of the object
     * @return true if the object is colliding on the side
     */
    public boolean collidesOn(Side side) {
        return !collidingObjects.get(side).isEmpty();
    }
    
    /**
     * check if an object collides with this one on a specific side
     *
     * @param aabbPhysics object to check if colliding with
     * @param side        side of object
     * @return true if the object is colliding with the other object on the soecified side
     */
    public boolean collidesWithOn(AABBPhysics aabbPhysics, Side side) {
        return collidingObjects.get(side).contains(aabbPhysics);
    }
    
    /**
     * see if this object is overlapping any object
     *
     * @return true if overlapping
     */
    public boolean isOverlapping() {
        return overlapping;
    }
    
    /**
     * set the bounding box for this object
     *
     * @return object's bounding box
     */
    public Box getBoundingBox() {
        return box;
    }
    
    /**
     * set the object's bounding box centered at the object's current position
     *
     * @param box bounding box to set
     */
    public void setBoundingBox(Box box) {
        Box newBox = new Box(box);
        newBox.setPosition(position);
        this.box = newBox;
    }
    
    /**
     * check if the object is kinematic
     *
     * @return true if object is kinematic
     */
    public boolean isKinematic() {
        return kinematic;
    }
    
    /**
     * set an object to be kinematic or not
     *
     * @param kinematic true to allow object motion self collision checks
     */
    public void setKinematic(boolean kinematic) {
        this.kinematic = kinematic;
    }
    
    /**
     * check if the object is pushable
     *
     * @return true if an object is pushable
     */
    public boolean isPushable() {
        return pushable;
    }
    
    /**
     * set the pushablility status of the object
     *
     * @param pushable true to allow pushing
     */
    public void setPushable(boolean pushable) {
        this.pushable = pushable;
    }
    
    /**
     * get the mass of the object
     *
     * @return mass of the object
     */
    public float getMass() {
        return mass;
    }
    
    /**
     * set the mass of the object
     *
     * @param mass new mass for object
     */
    public void setMass(float mass) {
        this.mass = mass;
    }
    
    /**
     * check if another set of aabbphysics data is equal to this one
     *
     * @param o object to check for equality
     * @return aabbphysics data is equivalent to this
     */
    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AABBPhysics that = (AABBPhysics) o;
        return collidable == that.collidable &&
                colliding == that.colliding &&
                overlapping == that.overlapping &&
                kinematic == that.kinematic &&
                Objects.equals(gravity, that.gravity) &&
                Objects.equals(position, that.position) &&
                Objects.equals(velocity, that.velocity) &&
                Objects.equals(acceleration, that.acceleration) &&
                Objects.equals(jerk, that.jerk) &&
                Objects.equals(drag, that.drag) &&
                Objects.equals(friction, that.friction) &&
                Objects.equals(scene, that.scene) &&
                Objects.equals(collidingObjects, that.collidingObjects) &&
                Objects.equals(box, that.box) &&
                Objects.equals(terminalVelocity, that.terminalVelocity);
    }
}