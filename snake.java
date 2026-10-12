import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Простая игра "Змейка" на Java Swing.
 *
 * Управление: стрелки (или WASD).
 * После проигрыша — пробел для рестарта.
 *
 * Компиляция:  javac SnakeGame.java
 * Запуск:      java SnakeGame
 */
class SnakeGame extends JPanel implements ActionListener {

    private static final int TILE_SIZE = 25;
    private static final int GRID_WIDTH = 24;
    private static final int GRID_HEIGHT = 20;
    private static final int WINDOW_WIDTH = TILE_SIZE * GRID_WIDTH;
    private static final int WINDOW_HEIGHT = TILE_SIZE * GRID_HEIGHT;

    private static final int INITIAL_DELAY_MS = 140;
    private static final int MIN_DELAY_MS = 60;
    private static final int SPEEDUP_EVERY_N_FOODS = 5;
    private static final int SPEEDUP_STEP_MS = 8;

    private enum Direction { UP, DOWN, LEFT, RIGHT }

    private final List<Point> snake = new ArrayList<>();
    private Direction direction = Direction.RIGHT;
    private Direction nextDirection = Direction.RIGHT;

    private Point food;
    private final Random random = new Random();

    private Timer timer;
    private int currentDelay = INITIAL_DELAY_MS;

    private int score = 0;
    private int highScore = 0;
    private boolean gameOver = false;
    private boolean started = false;

    public SnakeGame() {
        setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        setBackground(new Color(20, 20, 24));
        setFocusable(true);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKey(e.getKeyCode());
            }
        });

        initGame();
        timer = new Timer(currentDelay, this);
        timer.start();
    }

    private void initGame() {
        snake.clear();
        int startX = GRID_WIDTH / 2;
        int startY = GRID_HEIGHT / 2;
        snake.add(new Point(startX, startY));
        snake.add(new Point(startX - 1, startY));
        snake.add(new Point(startX - 2, startY));

        direction = Direction.RIGHT;
        nextDirection = Direction.RIGHT;
        score = 0;
        currentDelay = INITIAL_DELAY_MS;
        gameOver = false;
        started = false;

        spawnFood();
    }

    private void spawnFood() {
        List<Point> free = new ArrayList<>();
        for (int x = 0; x < GRID_WIDTH; x++) {
            for (int y = 0; y < GRID_HEIGHT; y++) {
                Point p = new Point(x, y);
                if (!snake.contains(p)) {
                    free.add(p);
                }
            }
        }
        if (free.isEmpty()) {
            food = null; // игрок заполнил всё поле — победа
            return;
        }
        food = free.get(random.nextInt(free.size()));
    }

    private void handleKey(int keyCode) {
        switch (keyCode) {
            case KeyEvent.VK_UP:
            case KeyEvent.VK_W:
                if (direction != Direction.DOWN) nextDirection = Direction.UP;
                started = true;
                break;
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_S:
                if (direction != Direction.UP) nextDirection = Direction.DOWN;
                started = true;
                break;
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_A:
                if (direction != Direction.RIGHT) nextDirection = Direction.LEFT;
                started = true;
                break;
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_D:
                if (direction != Direction.LEFT) nextDirection = Direction.RIGHT;
                started = true;
                break;
            case KeyEvent.VK_SPACE:
                if (gameOver) {
                    highScore = Math.max(highScore, score);
                    initGame();
                    timer.setDelay(currentDelay);
                }
                break;
            case KeyEvent.VK_P:
                if (!gameOver) {
                    if (timer.isRunning()) timer.stop(); else timer.start();
                }
                break;
        }
    }

    private void move() {
        direction = nextDirection;
        Point head = snake.get(0);
        Point newHead = new Point(head.x, head.y);

        switch (direction) {
            case UP: newHead.y--; break;
            case DOWN: newHead.y++; break;
            case LEFT: newHead.x--; break;
            case RIGHT: newHead.x++; break;
        }

        // проверка столкновения со стеной
        if (newHead.x < 0 || newHead.x >= GRID_WIDTH || newHead.y < 0 || newHead.y >= GRID_HEIGHT) {
            endGame();
            return;
        }

        // проверка столкновения с собой (без учёта хвоста, который сейчас уйдёт)
        boolean willEat = food != null && newHead.equals(food);
        int checkLimit = willEat ? snake.size() : snake.size() - 1;
        for (int i = 0; i < checkLimit; i++) {
            if (snake.get(i).equals(newHead)) {
                endGame();
                return;
            }
        }

        snake.add(0, newHead);

        if (willEat) {
            score++;
            if (score % SPEEDUP_EVERY_N_FOODS == 0 && currentDelay > MIN_DELAY_MS) {
                currentDelay = Math.max(MIN_DELAY_MS, currentDelay - SPEEDUP_STEP_MS);
                timer.setDelay(currentDelay);
            }
            spawnFood();
        } else {
            snake.remove(snake.size() - 1);
        }
    }

    private void endGame() {
        gameOver = true;
        highScore = Math.max(highScore, score);
        timer.stop();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameOver && started) {
            move();
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawGrid(g2);

        // еда
        if (food != null) {
            g2.setColor(new Color(230, 70, 70));
            g2.fillRoundRect(food.x * TILE_SIZE + 3, food.y * TILE_SIZE + 3,
                    TILE_SIZE - 6, TILE_SIZE - 6, 8, 8);
        }

        // змейка
        for (int i = 0; i < snake.size(); i++) {
            Point p = snake.get(i);
            if (i == 0) {
                g2.setColor(new Color(120, 220, 130));
            } else {
                int shade = Math.max(60, 180 - i * 3);
                g2.setColor(new Color(60, shade, 90));
            }
            g2.fillRoundRect(p.x * TILE_SIZE + 1, p.y * TILE_SIZE + 1,
                    TILE_SIZE - 2, TILE_SIZE - 2, 6, 6);
        }

        // интерфейс
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 16));
        g2.drawString("Счёт: " + score, 10, 20);
        g2.drawString("Рекорд: " + highScore, 10, 40);

        if (!started && !gameOver) {
            drawCenteredMessage(g2, "Нажмите стрелку, чтобы начать", -10);
            drawCenteredMessage(g2, "P — пауза", 15);
        }

        if (gameOver) {
            g2.setColor(new Color(0, 0, 0, 160));
            g2.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, 28));
            drawCenteredMessage(g2, "Игра окончена!", -20);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 18));
            drawCenteredMessage(g2, "Счёт: " + score, 15);
            drawCenteredMessage(g2, "Нажмите ПРОБЕЛ для рестарта", 45);
        }
    }

    private void drawCenteredMessage(Graphics2D g2, String text, int yOffset) {
        FontMetrics fm = g2.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        g2.drawString(text, (WINDOW_WIDTH - textWidth) / 2, WINDOW_HEIGHT / 2 + yOffset);
    }

    private void drawGrid(Graphics2D g2) {
        g2.setColor(new Color(32, 32, 38));
        for (int x = 0; x <= GRID_WIDTH; x++) {
            g2.drawLine(x * TILE_SIZE, 0, x * TILE_SIZE, WINDOW_HEIGHT);
        }
        for (int y = 0; y <= GRID_HEIGHT; y++) {
            g2.drawLine(0, y * TILE_SIZE, WINDOW_WIDTH, y * TILE_SIZE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Змейка (Snake)");
            SnakeGame game = new SnakeGame();

            frame.add(game);
            frame.pack();
            frame.setResizable(false);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            game.requestFocusInWindow();
        });
    }
}