import javafx.application.Platform;
import javafx.scene.effect.BoxBlur;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Shape;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

/**
 * Created by teren on 8/9/2016.
 * The BallRunnable class implements Runnable, meaning it can be run in parallel on its own Thread.
 * The BallRunnable object wraps a Ball object along with the ballPane.
 * On its over-ride run method, it can detect and react to collisions with walls and other balls.
 * Each separate ball can be computer in its own thread to increase performance.
 * NOTE: After experimenting with the thread based implementation vs the JavaFX AnimationTimer, I opted
 * for the second solution as it results in smoother animation.
 */
public class BallRunnable implements Runnable {

    private final Ball ball;
    private Pane ballPane;

    /**
     * The BallRunnable constructor takes a single Ball and the Ball Panel as input.
     *
     * @param ball     is the Ball that will be used during the run
     * @param ballPane is the container in which all balls will bounce
     */
    BallRunnable(Ball ball, Pane ballPane) {
        this.ball = ball;
        this.ballPane = ballPane;
    }

    /**
     * The run method is constantly invoked every millisecond and can detect and react to collisions with balls and walls.
     */
    public void run() {
        while (true) {
            blurOnWallCollision();
            handleCollisions();
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This collision effect causes the ball to become blurred when it first collides with a wall.
     * It allows for an interesting visualization as eventually all the balls will be blurred, indicating
     * they have all at some point reached the outer limit of the ball panel.
     */
    private void blurOnWallCollision() {
        double leftWall = 0;
        double rightWall = ballPane.getWidth();
        double bottomWall = 0;
        double topWall = ballPane.getHeight();
        double leftSideOfBall = ball.getCenterX() - ball.getRadius();
        double rightSideOfBall = ball.getCenterX() + ball.getRadius();
        double bottomSideOfBall = ball.getCenterY() - ball.getRadius();
        double topSideOfBall = ball.getCenterY() + ball.getRadius();
        boolean isLeftWallCollision = leftSideOfBall <= leftWall;
        boolean isRightWallCollision = rightSideOfBall >= rightWall;
        boolean isBottomWallCollision = bottomSideOfBall <= bottomWall;
        boolean isTopWallCollision = topSideOfBall >= topWall;
        Shape circle = ball.getView();
        if (isLeftWallCollision || isRightWallCollision || isTopWallCollision || isBottomWallCollision) {
            Platform.runLater(() -> circle.setEffect(new BoxBlur(10, 10, 3)));
        }
    }

    /**
     * Iterates through the ball list using parallel streams and handles ball and wall collisions
     */
    private void handleCollisions() {
        BouncingBallApplication.balls.parallelStream().forEach(ball1 -> {
            wallCollision(ball1, ballPane);
            BouncingBallApplication.balls.parallelStream().forEach(ball2 -> ballCollision(ball1, ball2));
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
     *
     * @param ball     is the Ball that we want to check for wall collisions
     * @param ballPane is the Pane whose edges represent the walls
     */
    private void wallCollision(Ball ball, Pane ballPane) {
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
            Platform.runLater(() -> ball.setXVelocity(-horizontalVelocity));
        }
        if ((isBallMovingDown && isBottomWallCollision) || (isBallMovingUp && isTopWallCollision)) {
            Platform.runLater(() -> ball.setYVelocity(-verticalVelocity));
        }
    }

    /**
     * A ball collision is a relatively complex phenomenon which involves detecting whether the two balls in question
     * are overlapping. This is determined based on finding the distance between their radii. We must also confirm that
     * the two balls were actually moving towards each other prior to the collision by ensuring that their distance is decreasing.
     * Once we have determined that a pair of balls has collided, we must calculate the deflection angles and velocities
     * in which the balls will bounce, given their size, mass, velocity, etc.
     *
     * @param ball1
     * @param ball2
     */
    private void ballCollision(Ball ball1, Ball ball2) {
        final double deltaX = ball2.getCenterX() - ball1.getCenterX();
        final double deltaY = ball2.getCenterY() - ball1.getCenterY();
        final double distanceBetweenBalls = ball1.getRadius() + ball2.getRadius();
        boolean isOverlapping = (pow(deltaX, 2) + pow(deltaY, 2) <= pow(distanceBetweenBalls, 2));
        boolean isDistanceDecreasing = (deltaX * (ball2.getXVelocity() - ball1.getXVelocity()) + deltaY * (ball2.getYVelocity() - ball1.getYVelocity()) < 0);
        boolean isBallCollision = isOverlapping && isDistanceDecreasing;
        if (isBallCollision) {
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

            Platform.runLater(() -> ball1.setXVelocity(v1 * unitContactX + u1PerpX));
            Platform.runLater(() -> ball1.setYVelocity(v1 * unitContactY + u1PerpY));
            Platform.runLater(() -> ball2.setXVelocity(v2 * unitContactX + u2PerpX));
            Platform.runLater(() -> ball2.setYVelocity(v2 * unitContactY + u2PerpY));
        }
    }
}
