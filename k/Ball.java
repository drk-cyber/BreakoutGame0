import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Rectangle;
import java.util.Random;

/**
 * Ball.java
 *
 * 球体实体：负责移动、与挡板/砖块/墙的基本碰撞反应。
 * 展示继承与多态：作为 Entity 的具体实现，提供 onPaddleCollision/onBrickCollision 等方法。
 */
public class Ball extends Entity {

    private float vx = 0f;
    private float vy = 0f;
    private float speed = 370f; // 基础速度（像素/秒）
    private boolean launched = false;

    private Paddle paddle;
    private Color color = Color.CYAN;

    private final Random rnd = new Random();

    public Ball(float x, float y, int width, int height) {
        super(x, y, width, height);
        // 默认未发射，位于挡板上方
    }

    @Override
    public void update(float dt) {
        if (!launched) {
            // 如果未发射且有挡板，则保持在挡板上方
            if (paddle != null) {
                x = paddle.getX() + paddle.getWidth() / 2 - width / 2;
                y = paddle.getY() - height - 2;
            }
            return;
        }

        x += vx * dt;
        y += vy * dt;
    }

    @Override
    public void render(Graphics2D g) {
        g.setColor(color);
        g.fillOval((int) x, (int) y, width, height);
        g.setColor(Color.BLACK);
        g.drawOval((int) x, (int) y, width, height);
    }

    public void setPaddle(Paddle p) {
        this.paddle = p;
    }

    // 被挡板碰撞时的反应：反向 y 速度，并根据击中位置调整 x 速度
    public void onPaddleCollision(Paddle p) {
        // 位置差越大，x 速度越大
        float paddleCenter = p.getX() + p.getWidth() / 2f;
        float ballCenter = x + width / 2f;
        float rel = (ballCenter - paddleCenter) / (p.getWidth() / 2f);
        // 限制相对值
        if (rel < -1f) rel = -1f;
        if (rel > 1f) rel = 1f;

        // 更新速度向量
        vy = -Math.abs(vy == 0 ? -speed : vy);
        vx = rel * speed;

        // 确保球离开挡板，避免重复碰撞
        y = p.getY() - height - 1;
    }

    // 与砖块碰撞后的简单反应
    public void onBrickCollision(Brick b) {
        // 通过简单规则决定反弹方向：如果从左右碰撞则反向 vx，否则反向 vy
        Rectangle br = b.getBounds();
        Rectangle ba = getBounds();

        // 计算交叉深度
        float overlapX = Math.min(br.x + br.width, ba.x + ba.width) - Math.max(br.x, ba.x);
        float overlapY = Math.min(br.y + br.height, ba.y + ba.height) - Math.max(br.y, ba.y);

        if (overlapX < overlapY) {
            vx = -vx;
        } else {
            vy = -vy;
        }
    }

    // 处理墙面碰撞（左右和上）
    public void handleWallCollision(int left, int top, int right, int bottom) {
        if (x <= left) {
            x = left;
            vx = Math.abs(vx);
        } else if (x + width >= right) {
            x = right - width;
            vx = -Math.abs(vx);
        }

        if (y <= top) {
            y = top;
            vy = Math.abs(vy);
        }
    }

    // 是否丢失（掉到底部）
    public boolean isLost(int bottom) {
        return y > bottom;
    }

    // 重置位置并停止移动
    public void resetPosition(float nx, float ny) {
        x = nx;
        y = ny;
        vx = 0f;
        vy = 0f;
        launched = false;
    }

    // 发射球
    public void launch() {
        if (launched) return;
        launched = true;
        // 初始角度略有随机，以避免完全垂直
        float angle = (float) ((rnd.nextFloat() * 0.8f + 0.1f) * Math.PI);
        vx = (float) (Math.cos(angle) * speed * (rnd.nextBoolean() ? 1 : -1));
        vy = (float) (-Math.abs(Math.sin(angle) * speed));
    }

    // 公共方法：获取/设置速度以及按比例修改速度，避免直接访问私有字段
    public float getVx() { return vx; }
    public float getVy() { return vy; }
    public void setVx(float newVx) { vx = newVx; }
    public void setVy(float newVy) { vy = newVy; }

    public void multiplyVelocity(float mx, float my) {
        vx *= mx;
        vy *= my;
    }

    public boolean isLaunched() { return launched; }

}
