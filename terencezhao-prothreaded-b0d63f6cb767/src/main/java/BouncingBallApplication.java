import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.ToolBar;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static java.lang.Math.*;
import static javafx.scene.paint.Color.*;

/**
 * SOURCE: https://gist.github.com/james-d/8327842#file-animationtimertest-java
 * This BouncingBallApplication is inspired by the source code found at the above github.
 * For this project, I added additional JavaFX user interface sliders which can control various properties related
 * to the balls. These include increasing or decreasing the number of balls bouncing in the window, changing the range
 * of sizes of the balls that get created, and determining the range of speeds the balls will travel.
 * <p>
 * NOTE: Since I went with the AnimationTimer implementation, adjusting the time between move/draw calls
 * is not applicable since every individual frame triggers a recalculation of the balls.
 */
public class BouncingBallApplication extends Application {

    private BorderPane root = new BorderPane();
    public final Pane ballPane = new Pane();
    private ToolBar toolBar = new ToolBar();

    private Label ballCountLabel = new Label("Ball Count");
    private Slider ballCountSlider = new Slider(0, 1000, 4);
    private Label ballCountValue = new Label();

    private Label ballRadiusLabel = new Label("Ball Size");
    private Slider ballRadiusSlider = new Slider(5, 50, 20);
    private Label ballRadiusValue = new Label();

    private Label ballSpeedLabel = new Label("Ball Speed");
    private Slider ballSpeedSlider = new Slider(50, 500, 250);
    private Label ballSpeedValue = new Label();

    private Label refreshRateLabel = new Label("Thread Count");
    private Slider refreshRateSlider = new Slider(1, 100, 1);
    private Label refreshRateValue = new Label();

    public static ObservableList<Ball> balls = FXCollections.observableArrayList();

    private static final double BALL_DENSITY = 0.01;
    private static final Color[] COLORS = new Color[]{RED, ORANGE, YELLOW, GREEN, BLUE, INDIGO, VIOLET};

    private ExecutorService executorService = Executors.newFixedThreadPool(refreshRateSlider.valueProperty().intValue(), runnable -> {
        Thread t = new Thread(runnable);
        t.setDaemon(true);
        return t;
    });

    /**
     * Starting the application performs the following:
     * Apply listeners to the ball list so that the circles can be redrawn each time the balls change position.
     * Set up the ball pane so that when it is re-sized, the balls behave according to the new alloted space.
     * Set up the various sliders which control the count, size and speed of the bouncing balls.
     * Set up the user interface including the border pane sections and the window dimensions.
     * Finally, being the bouncing ball animation.
     *
     * @param primaryStage is the main stage
     */
    @Override
    public void start(Stage primaryStage) {
        balls.addListener(new ListChangeListener<Ball>() {
            public void onChanged(Change<? extends Ball> change) {
                while (change.next()) {
                    change.getAddedSubList().forEach(ball -> ballPane.getChildren().add(ball.getView()));
                    change.getRemoved().forEach(ball -> ballPane.getChildren().remove(ball.getView()));
                }
            }
        });
        setUpBallPane();
        setUpSliders();
        setUpToolBar();
        root.setCenter(ballPane);
        root.setBottom(toolBar);
        final Scene scene = new Scene(root, 1000, 1000);
        primaryStage.setScene(scene);
        primaryStage.show();
        animate(ballPane);
        primaryStage.setOnCloseRequest(we -> primaryStage.close());

    }

    /**
     * Setting up the ball pane involves adding a click listener which triggers the balls to be created.
     * The number, size, and speed of the balls will be determined by the current values set on the sliders.
     * The position at which the balls will spawn is determined by the location in which the mouse is clicked.
     * Additionally, we set listeners on the width and height dimensions of the Pane, allowing the balls to adjust
     * their wall collision based on the newly sized container.
     */
    private void setUpBallPane() {
        ballPane.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> createBalls(event.getX(), event.getY()));
        ballPane.widthProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.doubleValue() < oldValue.doubleValue()) {
                balls.parallelStream().forEach(ball -> {
                    double max = newValue.doubleValue() - ball.getRadius();
                    if (ball.getCenterX() > max) {
                        ball.setCenterX(max);
                    }
                });
            }
        });
        ballPane.heightProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.doubleValue() < oldValue.doubleValue()) {
                balls.parallelStream().forEach(ball -> {
                    double max = newValue.doubleValue() - ball.getRadius();
                    if (ball.getCenterY() > max) {
                        ball.setCenterY(max);
                    }
                });
            }
        });
    }

    /**
     * Setting up the sliders is simple since they are all very similar in functionality.
     * Each slider is configured so that the major tick unit increases and decreases by 1.
     * The minimum, maximum and current slider values are defined in the Slider creation.
     */
    private void setUpSliders() {
        List<Slider> sliders = new ArrayList<>();
        sliders.add(ballCountSlider);
        sliders.add(ballRadiusSlider);
        sliders.add(ballSpeedSlider);
        sliders.add(refreshRateSlider);
        sliders.forEach(slider -> {
            slider.setMajorTickUnit(1);
            slider.setMinorTickCount(0);
            slider.setBlockIncrement(1);
            slider.setSnapToTicks(true);
        });
    }

    /**
     * Setting up the tool bar involves placing all the sliders adjacent to one another with their respective
     * labels and values displaying as expected. We format the value of the slider to be an integer value and
     * also put a bar separator between each slider/label grouping.
     */
    private void setUpToolBar() {
        ballRadiusValue.textProperty().bind(Bindings.format("%.0f", ballRadiusSlider.valueProperty()));
        ballCountValue.textProperty().bind(Bindings.format("%.0f", ballCountSlider.valueProperty()));
        ballSpeedValue.textProperty().bind(Bindings.format("%.0f", ballSpeedSlider.valueProperty()));
        refreshRateValue.textProperty().bind(Bindings.format("%.0f", refreshRateSlider.valueProperty()));
        toolBar.getItems().addAll(
                ballRadiusLabel, ballRadiusSlider, ballRadiusValue, new Separator(),
                ballCountLabel, ballCountSlider, ballCountValue, new Separator(),
                ballSpeedLabel, ballSpeedSlider, ballSpeedValue, new Separator()
//                ,refreshRateLabel, refreshRateSlider, refreshRateValue
        );
    }

    /**
     * Animating the balls involves using the JavaFX AnimationTimer. On each frame, the handle method is called with
     * the current timestamp. At these intervals, we perform the collision detection and handling while also updating the
     * position of the balls based on the amount of time that has elapsed between each frame.
     *
     * @param ballContainer is the ball container
     */
    private void animate(final Pane ballContainer) {
        final LongProperty lastUpdateTime = new SimpleLongProperty(0);
        final AnimationTimer animationTimer = new AnimationTimer() {
            @Override
            public void handle(long timestamp) {
                if (lastUpdateTime.get() > 0) {
                    long elapsedTime = timestamp - lastUpdateTime.get();
                    CollisionHandler.handleCollisions(balls, ballContainer);
                    double elapsedSeconds = elapsedTime / 1000000000.0;
                    balls.forEach(ball -> {
                        ball.setCenterX(ball.getCenterX() + elapsedSeconds * ball.getXVelocity());
                        ball.setCenterY(ball.getCenterY() + elapsedSeconds * ball.getYVelocity());
                    });
                }
                lastUpdateTime.set(timestamp);
            }
        };
        animationTimer.start();
    }

    /**
     * The createBalls helper method takes in the intial X and Y mouse click position in order to determine where
     * the balls will be spawned. Based on the current value of the ball count slider, it creates balls ranging in size
     * and speed based on the current values of the respective sliders. Finally, a new Thread is created to wrap each
     * ball and the thread is started.
     *
     * @param initialX is the initial x click position
     * @param initialY is the initial y click position
     */
    private void createBalls(double initialX, double initialY) {
        balls.clear();
        refreshRateSlider.valueProperty().intValue();
        int ballCount = ballCountSlider.valueProperty().intValue();
        double minRadius = ballRadiusSlider.getMin();
        double maxRadius = ballRadiusSlider.valueProperty().intValue();
        double minSpeed = ballSpeedSlider.getMin();
        double maxSpeed = ballSpeedSlider.valueProperty().intValue();
        final Random random = new Random();
        IntStream.range(0, ballCount).forEach(i -> {
            double radius = minRadius + (maxRadius - minRadius) * random.nextDouble();
            double volume = Math.pow((4 / 3) * PI * radius, 3);
            double mass = BALL_DENSITY * volume;
            final double speed = minSpeed + (maxSpeed - minSpeed) * random.nextDouble();
            final double angle = 2 * PI * random.nextDouble();
            final Color color = COLORS[i % COLORS.length];
            Ball ball = new Ball(initialX, initialY, radius, speed * cos(angle), speed * sin(angle), mass, color);
            balls.add(ball);
//            Thread thread = new Thread(new BallRunnable(ball, ballPane));
//            thread.setDaemon(true);
//            thread.start();
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
