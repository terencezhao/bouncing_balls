import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.effect.BoxBlur;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Shape;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

/**
 * Created by teren on 8/9/2016.
 * SOURCE: https://gist.github.com/james-d/8327842#file-animationtimertest-java
 * The CollisionHandler detects and handles collisions of balls against walls and other balls.
 */
public class CollisionHandler {

    /**
     * Iterates through the ball list using parallel streams and handles ball and wall collisions
     */
    public static void handleCollisions(ObservableList<Ball> balls, Pane ballPane) {
        balls.parallelStream().forEach(ball1 -> {
            wallCollision(ball1, ballPane);
            balls.parallelStream().forEach(ball2 -> ballCollision(ball1, ball2));
        });
    }

    /**
     * Handles the logic for detecting whether a ball has collided with on of the four Pane edges.
     * A collision with a wall can be simply thought of as a combination of two conditions.
     * The first being, whether or not the respective edge of the ball is touching the wall.
     * For example, the left side of the ball touches the left wall, etc.
     * Second, the ball must be traveling in the direction towards the wall it collides with.
     * If these two conditions are true for any given direction, the ball will collide and reflect off the wall
     * by inverting its respective horizontal or vertical velocity.
     * @param ball is the Ball that we want to check for wall collisions
     * @param ballPane is the Pane whose edges represent the walls
     */
    private static void wallCollision(Ball ball, Pane ballPane) {
        double leftWall = 0;
        double rightWall = ballPane.getWidth();
        double bottomWall = 0;
        double topWall = ballPane.getHeight();
        double horizontalVelocity = ball.getXVelocity();
        double verticalVelocity = ball.getYVelocity();
        double leftSideOfBall = ball.getCenterX() - ball.getRadius();
        double rightSideOfBall = ball.getCenterX() + ball.getRadius();
        double bottomSideOfBall = ball.getCenterY() - ball.getRadius();
        double topSideOfBall = ball.getCenterY() + ball.getRadius();
        boolean isLeftWallCollision = leftSideOfBall <= leftWall;
        boolean isRightWallCollision = rightSideOfBall >= rightWall;
        boolean isBottomWallCollision = bottomSideOfBall <= bottomWall;
        boolean isTopWallCollision = topSideOfBall >= topWall;
        boolean isBallMovingLeft = horizontalVelocity < 0;
        boolean isBallMovingRight = horizontalVelocity > 0;
        boolean isBallMovingDown = verticalVelocity < 0;
        boolean isBallMovingUp = verticalVelocity > 0;
        if ((isBallMovingLeft && isLeftWallCollision) || (isBallMovingRight && isRightWallCollision)) {
            ball.setXVelocity(-horizontalVelocity);
            Platform.runLater(() -> ball.getView().setEffect(new BoxBlur(10, 10, 3)));
        }
        if ((isBallMovingDown && isBottomWallCollision) || (isBallMovingUp && isTopWallCollision)) {
            ball.setYVelocity(-verticalVelocity);
            Platform.runLater(() -> ball.getView().setEffect(new BoxBlur(10, 10, 3)));
        }
    }

    /**
     * A ball collision is a relatively complex phenomenon which involves detecting whether the two balls in question
     * are overlapping. This is determined based on finding the distance between their radii. We must also confirm that
     * the two balls were actually moving towards each other prior to the collision by ensuring that their distance is decreasing.
     * Once we have determined that a pair of balls has collided, we must calculate the deflection angles and velocities
     * in which the balls will bounce, given their size, mass, velocity, etc.
     * @param ball1 is the first ball
     * @param ball2 is the second ball
     */
    private static void ballCollision(Ball ball1, Ball ball2) {
        final double deltaX = ball2.getCenterX() - ball1.getCenterX();
        final double deltaY = ball2.getCenterY() - ball1.getCenterY();
        final double distanceBetweenBalls = ball1.getRadius() + ball2.getRadius();
        boolean isOverlapping = (pow(deltaX, 2) + pow(deltaY, 2) <= pow(distanceBetweenBalls, 2));
        boolean isDistanceDecreasing = (deltaX * (ball2.getXVelocity() - ball1.getXVelocity()) + deltaY * (ball2.getYVelocity() - ball1.getYVelocity()) < 0);
        boolean isBallCollision = isOverlapping && isDistanceDecreasing;
        if (isBallCollision) {
//            Platform.runLater(() -> ball1.getView().setEffect(new Bloom()));
//            Platform.runLater(() -> ball2.getView().setEffect(new Bloom()));
            Shape circle1 = ball1.getView();
            Shape circle2 = ball2.getView();
            if (ball1.getRadius() > ball2.getRadius()) {
                Platform.runLater(() -> circle2.setFill(circle1.getFill()));
            } else {
                Platform.runLater(() -> circle1.setFill(circle2.getFill()));
            }
            final double distance = sqrt(deltaX * deltaX + deltaY * deltaY);
            final double unitContactX = deltaX / distance;
            final double unitContactY = deltaY / distance;

            final double xVelocity1 = ball1.getXVelocity();
            final double yVelocity1 = ball1.getYVelocity();
            final double xVelocity2 = ball2.getXVelocity();
            final double yVelocity2 = ball2.getYVelocity();

            final double u1 = xVelocity1 * unitContactX + yVelocity1 * unitContactY;
            final double u2 = xVelocity2 * unitContactX + yVelocity2 * unitContactY;

            final double massSum = ball1.getMass() + ball2.getMass();
            final double massDiff = ball1.getMass() - ball2.getMass();

            final double v1 = (2 * ball2.getMass() * u2 + u1 * massDiff) / massSum;
            final double v2 = (2 * ball1.getMass() * u1 - u2 * massDiff) / massSum;
            final double u1PerpX = xVelocity1 - u1 * unitContactX;
            final double u1PerpY = yVelocity1 - u1 * unitContactY;
            final double u2PerpX = xVelocity2 - u2 * unitContactX;
            final double u2PerpY = yVelocity2 - u2 * unitContactY;

            ball1.setXVelocity(v1 * unitContactX + u1PerpX);
            ball1.setYVelocity(v1 * unitContactY + u1PerpY);
            ball2.setXVelocity(v2 * unitContactX + u2PerpX);
            ball2.setYVelocity(v2 * unitContactY + u2PerpY);
        }
    }
}
