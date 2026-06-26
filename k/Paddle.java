import java.awt.Graphics2D;
import java.awt.Color;

/**
 * Paddle.java
 *
 * 玩家挡板：继承自 Entity，处理输入标志并在 update 中移动。
 * 展示继承与多态：GamePanel 将把 Paddle 放入 Entity 列表并以统一接口更新/渲染。
 */
public class Paddle extends Entity {

    private boolean movingLeft = false;
    private boolean movingRight = false;
    private float speed = 400f; // pixels per second
    private Color color = Color.ORANGE;

    public Paddle(float x, float y, int width, int height) {
        super(x, y, width, height);
    }

    @Override
    public void update(float dt) {
        float dx = 0f;
        if (movingLeft) dx -= speed * dt;
        if (movingRight) dx += speed * dt;
        x += dx;

        // 简单边界限制：不允许移出左侧或过度移出右侧（使用常量）
        if (x < 0) x = 0;
        // 右侧最多到窗口宽度减去挡板宽度
        if (x + width > GameConstants.WINDOW_WIDTH) x = GameConstants.WINDOW_WIDTH - width;
    }

    @Override
    public void render(Graphics2D g) {
        g.setColor(color);
        g.fillRoundRect((int) x, (int) y, width, height, 8, 8);
        g.setColor(Color.BLACK);
        g.drawRoundRect((int) x, (int) y, width, height, 8, 8);
    }

    // 控制方法由 GamePanel 的 KeyListener 调用
    public void setMovingLeft(boolean movingLeft) { this.movingLeft = movingLeft; }
    public void setMovingRight(boolean movingRight) { this.movingRight = movingRight; }

}
