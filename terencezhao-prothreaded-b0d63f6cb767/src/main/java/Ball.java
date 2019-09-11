import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeType;

import static java.lang.Math.sqrt;

/**
 * SOURCE: https://gist.github.com/james-d/8327842#file-animationtimertest-java
 * <p>
 * The Ball class represents a single bouncing ball. It has several properties including velocities in the horizontal
 * and vertical directions. A speed property that measures how fast the ball travels. A mass which represents how heavy
 * a ball is. A radius which measures the distance from the center of a ball to its edge. Lastly, a Circle shape which
 * represents the visualization of the ball object in the JavaFx environment.
 * Various properties such as velocity, speed, mass, and radius play a part in determining how a ball reacts when it
 * collides with another ball.
 */
public class Ball {
    private final DoubleProperty xVelocity;
    private final DoubleProperty yVelocity;
    private final ReadOnlyDoubleWrapper speed;
    private final double mass;
    private final double radius;
    private final Circle view;

    /**
     * The Ball constructor simply takes the given parameters and assigns the corresponding member variables.
     * Notice the members are private and final meaning they are encapulated by the ball and cannot change once instantiated.
     *
     * @param centerX   is the initial x coordinate position of the ball when it is first created
     * @param centerY   is the initial y coordinate position of the ball when it is first created
     * @param radius    is the length from the edge of the ball to its center. This ultimately determines the size of the ball.
     * @param xVelocity is the speed in the horizontal direction the ball is traveling when it is first created.
     * @param yVelocity is the speed in the vertical direction in which the ball is traveling when it is first created
     * @param mass      is the weight of the ball which influences collisions with other balls
     * @param color     is the color of the ball when it is first created
     */
    public Ball(double centerX, double centerY, double radius, double xVelocity, double yVelocity, double mass, Color color) {
        this.xVelocity = new SimpleDoubleProperty(this, "xVelocity", xVelocity);
        this.yVelocity = new SimpleDoubleProperty(this, "yVelocity", yVelocity);
        this.speed = new ReadOnlyDoubleWrapper(this, "speed");
        speed.bind(Bindings.createDoubleBinding(() -> {
            final double xVel = getXVelocity();
            final double yVel = getYVelocity();
            return sqrt(xVel * xVel + yVel * yVel);
        }, this.xVelocity, this.yVelocity));
        this.mass = mass;
        this.radius = radius;
        this.view = new Circle(centerX, centerY, radius);
        view.setRadius(radius);
        view.setFill(color);
        view.setStrokeType(StrokeType.OUTSIDE);
        view.setStroke(Color.BLACK);
        view.setStrokeWidth(2);
    }

    /**
     * Below are simple getters and setters for the various properties of the Ball.
     */

    public double getMass() {
        return mass;
    }

    public double getRadius() {
        return radius;
    }

    public final double getXVelocity() {
        return xVelocity.get();
    }

    public final void setXVelocity(double xVelocity) {
        this.xVelocity.set(xVelocity);
    }

    public final DoubleProperty xVelocityProperty() {
        return xVelocity;
    }

    public final double getYVelocity() {
        return yVelocity.get();
    }

    public final void setYVelocity(double yVelocity) {
        this.yVelocity.set(yVelocity);
    }

    public final DoubleProperty yVelocityProperty() {
        return yVelocity;
    }

    public final double getSpeed() {
        return speed.get();
    }

    public final ReadOnlyDoubleProperty speedProperty() {
        return speed.getReadOnlyProperty();
    }

    public final double getCenterX() {
        return view.getCenterX();
    }

    public final void setCenterX(double centerX) {
        view.setCenterX(centerX);
    }

    public final DoubleProperty centerXProperty() {
        return view.centerXProperty();
    }

    public final double getCenterY() {
        return view.getCenterY();
    }

    public final void setCenterY(double centerY) {
        view.setCenterY(centerY);
    }

    public final DoubleProperty centerYProperty() {
        return view.centerYProperty();
    }

    public Shape getView() {
        return view;
    }

}