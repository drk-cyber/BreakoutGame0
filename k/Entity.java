import java.awt.Graphics2D;
import java.awt.Rectangle;

/**
 * Entity.java
 *
 * 抽象实体基类：提供位置、大小、渲染与更新接口。
 * 演示继承与多态：Paddle、Ball、Brick 等都继承该类并实现抽象方法。
 */
public abstract class Entity {

    protected float x;
    protected float y;
    protected int width;
    protected int height;
    protected boolean removed = false;

    public Entity(float x, float y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    // 更新实体状态（由具体子类实现）
    public abstract void update(float dt);

    // 渲染实体（由具体子类实现）
    public abstract void render(Graphics2D g);

    // 获取碰撞边界
    public Rectangle getBounds() {
        return new Rectangle((int) x, (int) y, width, height);
    }

    // 标记为移除（例如砖块被打碎）
    public void remove() {
        removed = true;
    }

    public boolean isRemoved() {
        return removed;
    }

    // 访问器
    public float getX() { return x; }
    public float getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public void setX(float x) { this.x = x; }
}