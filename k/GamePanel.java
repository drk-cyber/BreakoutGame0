import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.RenderingHints;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.image.BufferStrategy;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * GamePanel.java
 *
 * 游戏主面板：负责游戏循环（update/render）、输入、实体管理与碰撞检测。
 *
 * 继承关系与多态示例：
 *  - 保存 List<Entity>，在循环中调用 entity.update(dt) 与 entity.render(g)
 *  - Paddle、Ball、Brick 等类继承 Entity，实现各自行为
 *
 * 实现要点：
 *  - 采用独立线程运行游戏循环（implements Runnable）
 *  - 使用键盘监听控制挡板
 *  - 简单的碰撞检测和关卡推进逻辑（LevelManager 在后续文件）
 */
public class GamePanel extends Canvas implements Runnable, KeyListener {

    private static final int TARGET_FPS = 60;
    private static final long   FRAME_TIME_NANO = 1000000000L / TARGET_FPS;

    private final int panelWidth;
    private final int panelHeight;

    private Thread gameThread;
    private volatile boolean running = false;
    private volatile boolean paused = false;
    private volatile boolean gameOver = false; // 游戏结束标志

    // Back buffer for smooth rendering
    private BufferedImage backBuffer;

    // 游戏实体集合（多态：实体共享接口）
    private final List<Entity> entities = new ArrayList<>();

    // 游戏对象引用（便于快速访问）
    private Paddle playerPaddle;
    private Ball gameBall;
    private LevelManager levelManager;
    private ScoreManager scoreManager;
    private HighScoreManager highScoreManager; // 持久化高分管理
    private boolean highscoreSaved = false; // 防止重复保存

    public GamePanel(int width, int height) {
        this.panelWidth = width;
        this.panelHeight = height;
        setFocusable(true);
        // Canvas 默认是轻量/重型组件，设置可聚焦并添加键盘监听
        addKeyListener(this);

        // 使用不带 alpha 的 RGB 图像可减少合成开销，避免部分透明导致的闪烁
        backBuffer = new BufferedImage(panelWidth, panelHeight, BufferedImage.TYPE_INT_RGB);

        // 请求焦点以接收键盘事件
        requestFocus();

        // 初始化高分管理（保存到工作目录下的 highscores.txt）
        highScoreManager = new HighScoreManager(10, "highscores.txt");

        initGame();
        start();
    }



    // 初始化游戏对象
    private void initGame() {
        entities.clear();

        // 重置高分保存标志
        highscoreSaved = false;

        // 初始化分数与关卡管理
        scoreManager = new ScoreManager();
        levelManager = new LevelManager(panelWidth, panelHeight, scoreManager);

        // 创建玩家挡板与球
        playerPaddle = new Paddle(panelWidth / 2 - 60, panelHeight - 50, 120, 16);
        gameBall = new Ball(panelWidth / 2 - 8, panelHeight - 70, 16, 16);

        // 将实体加入列表（展示多态）
        entities.add(playerPaddle);
        entities.add(gameBall);

        // 添加关卡砖块
        List<Brick> bricks = levelManager.createLevel(1);
        entities.addAll(bricks);

        // 绑定球与挡板的引用以便交互
        gameBall.setPaddle(playerPaddle);
    }

    // 启动游戏线程
    public synchronized void start() {
        if (running) return;
        running = true;
        gameThread = new Thread(this, "GameThread");
        gameThread.start();
    }

    // 停止游戏线程
    public synchronized void stop() {
        running = false;
        try {
            if (gameThread != null) gameThread.join(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // 游戏主循环
    @Override
    public void run() {
        long previous = System.nanoTime();
        long lag = 0L;

        while (running) {
            long now = System.nanoTime();
            long elapsed = now - previous;
            previous = now;
            lag += elapsed;

            // 处理更新（固定步长）
            while (lag >= FRAME_TIME_NANO) {
                if (!paused) update(1.0f / TARGET_FPS);
                lag -= FRAME_TIME_NANO;
            }

            render();
            // 简单节流，防止 CPU 占用过高
            try {
                Thread.sleep(2);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    // 更新所有实体与游戏逻辑
    private void update(float dt) {
        // 如果已游戏结束，跳过更新（仅保留渲染）
        if (gameOver) return;

        // 更新实体
        for (Entity e : new ArrayList<>(entities)) {
            e.update(dt);
        }

        // 碰撞检测：球与挡板、砖块、边界
        handleCollisions();

        // 清理已标记为 dead 的实体（例如被破坏的砖）
        Iterator<Entity> it = entities.iterator();
        while (it.hasNext()) {
            Entity e = it.next();
            if (e.isRemoved()) {
                it.remove();
            }
        }

        // 清理超出底部的道具
        for (Entity e : new ArrayList<>(entities)) {
            if (e instanceof PowerUp) {
                if (e.getY() > panelHeight) {
                    e.remove();
                }
            }
        }

        // 检查关卡完成
        if (levelManager.isLevelCleared(entities)) {
            // 进入下一关
            int next = levelManager.nextLevel();
            List<Brick> bricks = levelManager.createLevel(next);
            entities.addAll(bricks);
            // 重置球与挡板位置
            playerPaddle.setX(panelWidth / 2 - playerPaddle.getWidth() / 2);
            gameBall.resetPosition(panelWidth / 2 - 8, panelHeight - 70);
            gameBall.launch();
        }

        // 检查球丢失（生命减少）
        if (gameBall != null && gameBall.isLost(panelHeight)) {
            scoreManager.decreaseLife();
            if (scoreManager.getLives() <= 0) {
                // 游戏结束：显示 Game Over，保留分数直到玩家重启
                gameOver = true;
                paused = true;
                // 停止球并放回初始位置
                gameBall.resetPosition(panelWidth / 2 - 8, panelHeight - 70);
                // 保存高分（确保只保存一次）
                if (!highscoreSaved) {
                    if (highScoreManager != null) {
                        highScoreManager.addScore(scoreManager.getScore());
                    }
                    highscoreSaved = true;
                }
            } else {
                // 重置球与挡板，继续
                playerPaddle.setX(panelWidth / 2 - playerPaddle.getWidth() / 2);
                gameBall.resetPosition(panelWidth / 2 - 8, panelHeight - 70);
            }
        }
    }

    // 简单碰撞处理示例（包含道具拾取）
    private void handleCollisions() {
        if (gameBall != null && playerPaddle != null) {
            // Ball 与 Paddle
            if (gameBall.getBounds().intersects(playerPaddle.getBounds())) {
                gameBall.onPaddleCollision(playerPaddle);
            }
        }

        // Ball 与 Bricks
        for (Entity e : new ArrayList<>(entities)) {
            if (e instanceof Brick) {
                Brick b = (Brick) e;
                if (!b.isDestroyed() && gameBall != null && gameBall.getBounds().intersects(b.getBounds())) {
                    b.onHit(gameBall);
                    scoreManager.addScore(b.getPointValue());
                    gameBall.onBrickCollision(b);

                    // 破坏时有小概率掉落道具（简单示例）
                    if (b.isDestroyed() && Math.random() < 0.05) {
                        PowerUp p = (Math.random() < 0.5) ? new PowerUp.PaddleExpand(b.getX(), b.getY()) : new PowerUp.ExtraLife(b.getX(), b.getY());
                        entities.add(p);
                    }
                }
            }
        }

        // PowerUp 与 Paddle（拾取）
        for (Entity e : new ArrayList<>(entities)) {
            if (e instanceof PowerUp) {
                PowerUp p = (PowerUp) e;
                if (playerPaddle != null && p.getBounds().intersects(playerPaddle.getBounds())) {
                    p.apply(playerPaddle, scoreManager);
                }
            }
        }

        // Ball 与 边界（左右和上）
        if (gameBall != null) gameBall.handleWallCollision(0, 0, panelWidth, panelHeight);
    }

    // 渲染到后备缓冲并调用 repaint 绘制到屏幕（更安全的 Swing 绘制模式）
    private void render() {
        Graphics2D g = backBuffer.createGraphics();
        // 清屏
        g.setColor(Color.DARK_GRAY);
        g.fillRect(0, 0, panelWidth, panelHeight);

        // 渲染实体（多态：不同实体实现各自渲染）
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        for (Entity e : entities) {
            e.render(g);
        }

        // HUD：分数与生命
        renderHUD(g);

        g.dispose();

        // 主动渲染：优先使用本 Canvas 的 BufferStrategy
        boolean drawnToBS = false;
        BufferStrategy bs = getBufferStrategy();
        if (bs != null) {
            java.awt.Graphics gbs = null;
            try {
                gbs = bs.getDrawGraphics();
                if (gbs != null) {
                    gbs.drawImage(backBuffer, 0, 0, null);
                    if (gbs instanceof Graphics2D) {
                        try {
                            Graphics2D g2dBs = (Graphics2D) gbs;
                            renderHUD(g2dBs);
                        } catch (Throwable th) {
                            // ignore
                        }
                    }
                }
            } finally {
                if (gbs != null) gbs.dispose();
            }

            try {
                bs.show();
                Toolkit.getDefaultToolkit().sync();
                drawnToBS = true;
            } catch (IllegalStateException ise) {
                drawnToBS = false;
            }
        }

        if (!drawnToBS) {
            repaint();
        }
    }

    @Override
    public void paint(Graphics g) {
        // 直接绘制 backBuffer
        if (backBuffer != null) {
            g.drawImage(backBuffer, 0, 0, null);
        }
    }

    @Override
    public void update(Graphics g) {
        // 避免清屏闪烁，直接调用 paint
        paint(g);
    }

    private void renderHUD(Graphics2D g) {
        // 顶部信息栏背景（不透明，确保在实体之上可见）
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, panelWidth, 36);

        // 分数/生命/关卡
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 16));
        String scoreStr = "Score: " + scoreManager.getScore();
        g.drawString(scoreStr, 12, 24);

        String levelStr = "Level: " + levelManager.getCurrentLevel();
        int levelW = g.getFontMetrics().stringWidth(levelStr);
        g.drawString(levelStr, panelWidth / 2 - levelW / 2, 24);

        String livesStr = "Lives: " + scoreManager.getLives();
        int livesW = g.getFontMetrics().stringWidth(livesStr);
        g.drawString(livesStr, panelWidth - livesW - 12, 24);

        // 暂停提示（非 gameOver）
        if (paused && !gameOver) {
            g.setFont(new Font("SansSerif", Font.PLAIN, 18));
            String paused = "PAUSED - Press SPACE to resume";
            int pw = g.getFontMetrics().stringWidth(paused);
            g.drawString(paused, panelWidth / 2 - pw / 2, panelHeight / 2);
        }

        // Game Over 覆盖层
        if (gameOver) {
            // 半透明覆盖
            g.setColor(new Color(0, 0, 0, 160));
            g.fillRect(0, 0, panelWidth, panelHeight);
            g.setColor(Color.WHITE);
            g.setFont(new Font("SansSerif", Font.BOLD, 36));
            String gameOverText = "GAME OVER";
            int gw = g.getFontMetrics().stringWidth(gameOverText);
            g.drawString(gameOverText, panelWidth / 2 - gw / 2, panelHeight / 2 - 40);

            g.setFont(new Font("SansSerif", Font.PLAIN, 20));
            String scoreText = "Final Score: " + scoreManager.getScore();
            int sw = g.getFontMetrics().stringWidth(scoreText);
            g.drawString(scoreText, panelWidth / 2 - sw / 2, panelHeight / 2 - 10);

            // 显示排行榜
            g.setFont(new Font("SansSerif", Font.PLAIN, 16));
            int y = panelHeight / 2 + 20;
            g.drawString("Top Scores:", panelWidth / 2 - 60, y);
            java.util.List<Integer> top = (highScoreManager != null) ? highScoreManager.getTopScores() : new java.util.ArrayList<Integer>();
            int i = 0;
            for (Integer s : top) {
                if (i >= 5) break;
                String line = String.format("%d. %d", i + 1, s);
                int lw = g.getFontMetrics().stringWidth(line);
                g.drawString(line, panelWidth / 2 - lw / 2, y + 20 * (i + 1));
                i++;
            }

            String hint = "Press R to Restart";
            int hw = g.getFontMetrics().stringWidth(hint);
            g.drawString(hint, panelWidth / 2 - hw / 2, panelHeight / 2 + 140);
        }
    }

    // 键盘输入处理
    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_LEFT || code == KeyEvent.VK_A) {
            playerPaddle.setMovingLeft(true);
        } else if (code == KeyEvent.VK_RIGHT || code == KeyEvent.VK_D) {
            playerPaddle.setMovingRight(true);
        } else if (code == KeyEvent.VK_SPACE) {
            if (gameBall != null && !gameBall.isLaunched()) {
                gameBall.launch();
            } else {
                paused = !paused;
            }
        } else if (code == KeyEvent.VK_R) {
            if (gameOver) {
                // 玩家请求重启：重置分数/生命并初始化关卡
                scoreManager.reset();
                gameOver = false;
                paused = false;
                initGame();
            } else {
                initGame(); // 重新开始
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_LEFT || code == KeyEvent.VK_A) {
            playerPaddle.setMovingLeft(false);
        } else if (code == KeyEvent.VK_RIGHT || code == KeyEvent.VK_D) {
            playerPaddle.setMovingRight(false);
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // not used
    }

    // 清理资源（在窗口关闭时可调用）
    public void disposeResources() {
        stop();
    }
}