import java.awt.Color;
import java.awt.Graphics2D;

/**
 * AdvancedBricks.java
 *
 * 一些额外类型的砖块，用以展示多态扩展：
 *  - ExplodingBrick: 被击中后爆炸并影响周围砖块（示例逻辑）
 *  - RegeneratingBrick: 被击碎后经过时间会重新生成
 */
public class AdvancedBricks {

    public static class ExplodingBrick extends Brick {

        public ExplodingBrick(float x, float y, int width, int height) {
            super(x, y, width, height, 1, 200, Color.MAGENTA);
        }

        @Override
        protected void onDestroyed() {
            // 在真实游戏中，这里会通知 LevelManager 或 GamePanel 处理周围砖块被破坏
            // 作为示例，输出日志
            System.out.println("ExplodingBrick destroyed at (" + x + "," + y + ")");
        }
    }

    public static class RegeneratingBrick extends Brick {

        private float regenTimer = 0f;
        private float regenDelay = 5f; // seconds

        public RegeneratingBrick(float x, float y, int width, int height) {
            super(x, y, width, height, 2, 250, Color.BLUE);
        }

        @Override
        public void update(float dt) {
            if (destroyed) {
                regenTimer += dt;
                if (regenTimer >= regenDelay) {
                    destroyed = false;
                    hp = maxHp;
                    regenTimer = 0f;
                    // 重新生成时不需要再添加回实体列表：此处仅演示概念
                }
            }
        }

        @Override
        protected void onDestroyed() {
            System.out.println("RegeneratingBrick destroyed, will regen in " + regenDelay + "s");
        }
    }

}
