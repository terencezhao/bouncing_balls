proThreaded:

To run the application, execute the main method within the BouncingBallApplication.java.
You will be presented with a blank JavaFx application screen.
Notice at the bottom of the screen are various sliders that you can use to configure the
count, size, and speed of the bouncing balls.
Once you have set the values to your liking, click anywhere on the pane to spawn the bouncing balls.
You will notice a couple of interesting features;
1. Once a ball collides with a wall, it will become blurred. It is interesting to observe when there are alot of balls
present, which ones have not yet touched the outer limit of the application window.
2. When two balls collide, one of them will change color. This is determined by the size of the balls colliding.
When two balls collide, the smaller ball will change its color to the color of the larger ball. Again, this creates a
very entertaining visualization when there are many balls as there appears to be a battle of colors trying to fight for
existence. If you watch long enough, one of the colors will win out and all the balls on the screen will eventually be the same color.

Enjoy!

NOTE:
I implemented collision handling in 2 separate ways to compare performance.
One way was to assign each ball a separate thread using the BallRunable class (MTBBA.java).
Another was to rely the JavaFX AnimationTimer to redraw the view on each frame (BouncingBallApplication.java).
I found that using the AnimationTimer yielded smoother results, even at larger ball quantities and higher speeds.
I ended up delegating the blurOnWallCollision effect to each individual ball thread while allowing the JavaFX AnimationTimer
to control the collision handling. Since I went with the AnimationTimer implementation, adjusting the time between move/draw calls
is not applicable since every individual frame triggers a recalculation of the balls.