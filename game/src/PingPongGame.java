import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Random;

public class PingPongGame extends JPanel implements ActionListener, KeyListener {
    private Timer timer;
    private ArrayList<Ball> balls;
    private int paddleX, paddleY, paddleWidth = 100, paddleHeight = 10;
    private int score;
    private boolean isPaused;
    private PowerUp ballPowerUp;
    private PowerUp paddlePowerUp; // Power-up for increasing paddle size
    private Rectangle[] obstacles;
    private Random random;
    private Rectangle bomb;

    public PingPongGame() {
        timer = new Timer(5, this);
        random = new Random();
        balls = new ArrayList<>();
        initializeGame();
        setFocusable(true);
        addKeyListener(this);
        setBackground(Color.BLACK);
    }

    private void initializeGame() {
        balls.clear();
        balls.add(new Ball(300, 200, 2, 2)); // Add the initial ball
        paddleX = 250;
        paddleY = 450;
        paddleWidth = 100; // Reset paddle width
        score = 0;
        isPaused = true; // Start in paused state
        ballPowerUp = null; // Initialize ball power-up as null
        paddlePowerUp = null; // Initialize paddle size power-up as null
        obstacles = new Rectangle[3]; // Create obstacles
        createObstacles();
        createBomb(); // Create the initial bomb
    }

    private void createObstacles() {
        for (int i = 0; i < obstacles.length; i++) {
            int width = 50;
            int height = 10;
            int obstacleX = random.nextInt(Math.max(getWidth() - width, 1));
            int obstacleY = random.nextInt(Math.max(getHeight() - 200, 1));
            obstacles[i] = new Rectangle(obstacleX, obstacleY, width, height);
        }
    }

    private void createBomb() {
        int bombX = random.nextInt(Math.max(getWidth() - 20, 1)); // Ensure the width is at least 20
        bomb = new Rectangle(bombX, 0, 20, 20); // Bomb size 20x20
    }

    private void spawnPowerUps() {
        if (ballPowerUp == null && random.nextInt(100) < 3) {
            ballPowerUp = new PowerUp(random.nextInt(getWidth() - 20), 0, "ball");
        }

        if (paddlePowerUp == null && random.nextInt(100) < 3) {
            paddlePowerUp = new PowerUp(random.nextInt(getWidth() - 20), 0, "paddle");
        }
    }

    private void increaseBallCount() {
        int newBallX = paddleX + paddleWidth / 2; // Start new balls from the center of the paddle
        int newBallY = paddleY - 20; // Position just above the paddle
        balls.add(new Ball(newBallX, newBallY, 2, -2)); // Keep speed consistent
    }

    private void increasePaddleSize() {
        paddleWidth += 40; // Increase paddle size temporarily
        Timer timer = new Timer(5000, e -> paddleWidth -= 40); // Reset paddle size after 5 seconds
        timer.setRepeats(false);
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (Ball ball : balls) {
            ball.draw(g);
        }
        g.setColor(Color.BLUE);
        g.fillRect(paddleX, paddleY, paddleWidth, paddleHeight); // Draw the paddle

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Score: " + score, 10, 20);

        if (ballPowerUp != null) {
            ballPowerUp.draw(g);
        }

        if (paddlePowerUp != null) {
            paddlePowerUp.draw(g);
        }

        g.setColor(Color.ORANGE);
        for (Rectangle obstacle : obstacles) {
            g.fillRect(obstacle.x, obstacle.y, obstacle.width, obstacle.height);
        }

        if (bomb != null) {
            g.setColor(Color.RED);
            g.fillRect(bomb.x, bomb.y, bomb.width, bomb.height); // Draw the bomb
        }

        if (isPaused) {
            g.setColor(Color.YELLOW);
            g.drawString("PAUSED", getWidth() / 2 - 30, getHeight() / 2);
        }

        // Achievement Animation
        if (score >= 25 && score < 50) {
            g.setColor(Color.GREEN);
            g.setFont(new Font("Arial", Font.BOLD, 30));
            g.drawString("Keep It Going!", getWidth() / 2 - 100, getHeight() / 2);
        } else if (score >= 50 && score < 100) {
            g.setColor(Color.GREEN);
            g.setFont(new Font("Arial", Font.BOLD, 30));
            g.drawString("Achievement Unlocked!", getWidth() / 2 - 100, getHeight() / 2);
        } else if (score >= 100 && score < 500) {
            g.setColor(Color.CYAN);
            g.setFont(new Font("Arial", Font.BOLD, 30));
            g.drawString("You're on Fire!", getWidth() / 2 - 80, getHeight() / 2);
        } else if (score >= 500 && score < 1000) {
            g.setColor(Color.MAGENTA);
            g.setFont(new Font("Arial", Font.BOLD, 30));
            g.drawString("Half a Grand!", getWidth() / 2 - 90, getHeight() / 2);
        } else if (score >= 1000) {
            g.setColor(Color.YELLOW);
            g.setFont(new Font("Arial", Font.BOLD, 30));
            g.drawString("Legendary Player!", getWidth() / 2 - 100, getHeight() / 2);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!isPaused) {
            for (int i = 0; i < balls.size(); i++) {
                Ball ball = balls.get(i);
                ball.move(getWidth(), getHeight(), paddleX, paddleY, paddleWidth);

                // Check if the ball misses the paddle
                if (ball.y > getHeight()) {
                    balls.remove(i); // Remove the ball if it falls
                    i--; // Adjust index after removal
                }

                // Check for collision with obstacles
                for (Rectangle obstacle : obstacles) {
                    if (ball.getBounds().intersects(obstacle)) {
                        ball.ySpeed = -ball.ySpeed; // Bounce back
                        ball.y = obstacle.y - 20; // Position above the obstacle
                        score++; // Increase score for hitting an obstacle
                        break; // Exit loop after collision
                    }
                }
            }

            // Check if all balls are gone
            if (balls.isEmpty()) {
                endGame();
            }

            // Update power-up positions
            if (ballPowerUp != null) {
                ballPowerUp.move(); // Move the ball power-up down
                if (ballPowerUp.rect.y > getHeight()) {
                    ballPowerUp = null; // Remove if it falls off the screen
                } else if (ballPowerUp.rect.intersects(new Rectangle(paddleX, paddleY, paddleWidth, paddleHeight))) {
                    score += 5;
                    increaseBallCount(); // Increase the number of balls
                    ballPowerUp = null; // Remove ball power-up
                }
            }

            if (paddlePowerUp != null) {
                paddlePowerUp.move(); // Move the paddle power-up down
                if (paddlePowerUp.rect.y > getHeight()) {
                    paddlePowerUp = null; // Remove paddle power-up
                } else if (paddlePowerUp.rect.intersects(new Rectangle(paddleX, paddleY, paddleWidth, paddleHeight))) {
                    increasePaddleSize(); // Increase paddle size
                    paddlePowerUp = null; // Remove paddle power-up
                }
            }

            // Bomb movement
            if (bomb != null) {
                bomb.y += 2; // Move bomb downwards
                if (bomb.y > getHeight()) {
                    createBomb(); // Create a new bomb after it falls off
                }
                // Check if the bomb hits the paddle
                if (bomb.intersects(new Rectangle(paddleX, paddleY, paddleWidth, paddleHeight))) {
                    endGame(); // End game if bomb hits the paddle
                }
            }

            // Spawn new power-ups
            spawnPowerUps();
        }

        repaint(); // Redraw the game
    }

    private void endGame() {
        timer.stop();
        int restart = JOptionPane.showConfirmDialog(this, "Game Over! Final Score: " + score + "\nMade by Naresh and Aayush 😊\nWould you like to restart?", "Game Over", JOptionPane.YES_NO_OPTION);
        if (restart == JOptionPane.YES_OPTION) {
            initializeGame(); // Restart the game
            timer.start(); // Start the timer
        } else {
            System.exit(0); // Exit the game
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_LEFT && paddleX > 0) {
            paddleX -= 30; // Move paddle left
        }
        if (e.getKeyCode() == KeyEvent.VK_RIGHT && paddleX < getWidth() - paddleWidth) {
            paddleX += 30; // Move paddle right
        }
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            isPaused = !isPaused; // Toggle pause
        }
        if (e.getKeyCode() == KeyEvent.VK_R) {
            // Restart the game when 'R' is pressed
            initializeGame();
            timer.start(); // Start the timer
            isPaused = false; // Ensure the game is not paused
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame frame = new JFrame("Ping Pong Game");
        PingPongGame game = new PingPongGame();
        frame.add(game);
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        game.timer.start();
    }

    // Inner classes for the ball and power-ups
    class Ball {
        int x, y, xSpeed, ySpeed;
        Color color;

        public Ball(int x, int y, int xSpeed, int ySpeed) {
            this.x = x;
            this.y = y;
            this.xSpeed = xSpeed;
            this.ySpeed = ySpeed;
            this.color = getRandomColor(); // Set random initial color
        }

        public void draw(Graphics g) {
            g.setColor(color);
            g.fillOval(x, y, 20, 20); // Draw the ball
        }

        public void move(int width, int height, int paddleX, int paddleY, int paddleWidth) {
            x += xSpeed;
            y += ySpeed;

            // Change color randomly
            if (random.nextInt(100) < 5) {
                color = getRandomColor();
            }

            // Ball collision with walls
            if (x <= 0 || x >= width - 20) {
                xSpeed = -xSpeed; // Reverse direction
            }
            if (y <= 0) {
                ySpeed = -ySpeed; // Reverse direction
            }

            // Ball collision with paddle
            if (y >= paddleY - 20 && x >= paddleX && x <= paddleX + paddleWidth) {
                ySpeed = -ySpeed; // Reverse direction
                score++; // Increase score
            }

            // Ball misses paddle
            if (y > height) {
                // Do not end the game immediately, handle in actionPerformed
            }
        }

        public Rectangle getBounds() {
            return new Rectangle(x, y, 20, 20); // Assuming the ball is 20x20 pixels
        }

        private Color getRandomColor() {
            return new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
        }
    }

    class PowerUp {
        Rectangle rect;
        String type;

        public PowerUp(int x, int y, String type) {
            this.rect = new Rectangle(x, y, 20, 20);
            this.type = type;
        }

        public void draw(Graphics g) {
            if (type.equals("ball")) {
                g.setColor(Color.GREEN);
            } else if (type.equals("paddle")) {
                g.setColor(Color.MAGENTA);

            }
            g.fillRect(rect.x, rect.y, rect.width, rect.height);
        }

        public void move() {
            rect.y += 2; // Move downwards
        }
    }
}
