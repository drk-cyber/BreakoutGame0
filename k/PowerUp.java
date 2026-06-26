import java.awt.Graphics2D;
import java.awt.Color;

/**
 * PowerUp.java
 *
 * 道具基类及示例实现：被砖块掉落后可以被挡板拾取。
 * 演示继承与多态：PowerUp 基类可以有不同效果的子类。
 */
public abstract class PowerUp extends Entity {

    protected boolean active = true;
    protected float speed = 80f; // 下落速度
    protected Color color = Color.MAGENTA;

    public PowerUp(float x, float y, int width, int height) {
        super(x, y, width, height);
    }

    @Override
    public void update(float dt) {
        y += speed * dt;
        // 如果超出屏幕则标记移除（由 GamePanel 调用时传入界面高度）
    }

    @Override
    public void render(Graphics2D g) {
        g.setColor(color);
        g.fillOval((int) x, (int) y, width, height);
        g.setColor(Color.BLACK);
        g.drawOval((int) x, (int) y, width, height);
    }

    // 当被挡板拾取时执行效果
    public abstract void apply(Paddle paddle, ScoreManager scoreManager);

    // 示例：加速挡板
    public static class PaddleExpand extends PowerUp {
        public PaddleExpand(float x, float y) { super(x, y, 20, 20); this.color = Color.GREEN; }

        @Override
        public void apply(Paddle paddle, ScoreManager scoreManager) {
            // 扩大挡板宽度（简单实现）
            paddle.width = Math.min(paddle.width + 40, 400);
            remove();
        }
    }

    // 示例：额外生命
    public static class ExtraLife extends PowerUp {
        public ExtraLife(float x, float y) { super(x, y, 20, 20); this.color = Color.PINK; }

        @Override
        public void apply(Paddle paddle, ScoreManager scoreManager) {
            scoreManager.increaseLife();
            remove();
        }
    }

    // 示例：加速球
    public static class SpeedBall extends PowerUp {
        public SpeedBall(float x, float y) { super(x, y, 20, 20); this.color = Color.ORANGE; }

        @Override
        public void apply(Paddle paddle, ScoreManager scoreManager) {
            // 简单：找到场景中的球并加速（GamePanel 可扩展调用）
            // 这里作为占位符，仅增加分数
            scoreManager.addScore(50);
            remove();
        }
    }

}
