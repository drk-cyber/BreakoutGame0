import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Rectangle;

/**
 * Brick.java
 *
 * 砖块抽象类与几种具体实现：演示继承与多态。
 *  - Brick: 抽象基类，定义生命值、分数价值、渲染与被击中的通用接口
 *  - NormalBrick: 一击即碎
 *  - StrongBrick: 需多次击中
 *
 * GamePanel 在处理碰撞时通过 "instanceof Brick" 识别砖块并调用 onHit。
 */
public abstract class Brick extends Entity {

    protected int hp;
    protected int maxHp;
    protected int pointValue;
    protected Color color;
    protected boolean destroyed = false;

    public Brick(float x, float y, int width, int height, int hp, int pointValue, Color color) {
        super(x, y, width, height);
        this.hp = hp;
        this.maxHp = hp;
        this.pointValue = pointValue;
        this.color = color;
    }

    // 被球击中时调用，默认减少生命并在 hp<=0 时执行销毁
    public void onHit(Ball ball) {
        if (destroyed) return;
        hp -= 1;
        onHitEffect(ball);
        if (hp <= 0) {
            destroyed = true;
            remove();
            onDestroyed();
        }
    }

    // 子类可重写添加被击中特效
    protected void onHitEffect(Ball ball) {
        // 默认无动作
    }

    // 子类可重写来实现破坏效果
    protected void onDestroyed() {
        // 默认无动作
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    public int getPointValue() {
        return pointValue;
    }

    @Override
    public void update(float dt) {
        // 砖块通常不需要每帧更新，但是保留扩展点
    }

    @Override
    public void render(Graphics2D g) {
        // 根据剩余生命显示不同颜色深度
        float ratio = maxHp > 0 ? (float) hp / (float) maxHp : 0f;
        int r = (int) (color.getRed() * ratio + 50 * (1 - ratio));
        int gr = (int) (color.getGreen() * ratio + 50 * (1 - ratio));
        int b = (int) (color.getBlue() * ratio + 50 * (1 - ratio));
        Color drawColor = new Color(Math.max(0, Math.min(255, r)), Math.max(0, Math.min(255, gr)), Math.max(0, Math.min(255, b)));
        g.setColor(drawColor);
        g.fillRect((int) x, (int) y, width, height);
        g.setColor(Color.BLACK);
        g.drawRect((int) x, (int) y, width, height);
    }

    // 简单示例：子类工厂方法
    public static Brick createNormal(float x, float y, int width, int height) {
        return new NormalBrick(x, y, width, height);
    }

    public static Brick createStrong(float x, float y, int width, int height) {
        return new StrongBrick(x, y, width, height);
    }

    // 普通砖：1 次击中即破
    public static class NormalBrick extends Brick {
        public NormalBrick(float x, float y, int width, int height) {
            super(x, y, width, height, 1, 100, Color.YELLOW);
        }

        @Override
        protected void onDestroyed() {
            // 可能产生小粒子效果或音效（此处为占位）
        }
    }

    // 坚固砖：需要多次击中
    public static class StrongBrick extends Brick {
        public StrongBrick(float x, float y, int width, int height) {
            super(x, y, width, height, 3, 300, Color.RED);
        }

        @Override
        protected void onHitEffect(Ball ball) {
            // 被击中时略微改变球速作为示例（使用 Ball 的公共方法）
            ball.multiplyVelocity(1.02f, 0.98f);
        }

        @Override
        protected void onDestroyed() {
            // 可能掉落道具（在 LevelManager 中实现）
        }
    }

}
