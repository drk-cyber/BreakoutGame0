import java.util.List;
import java.util.ArrayList;
import java.awt.Color;

/**
 * LevelManager.java
 *
 * 负责生成关卡砖块布局并管理关卡进度。
 * 演示如何将关卡数据封装在类中，并返回实体列表供 GamePanel 使用。
 */
public class LevelManager {

    private int panelWidth;
    private int panelHeight;
    private ScoreManager scoreManager;
    private int currentLevel = 1;

    public LevelManager(int panelWidth, int panelHeight, ScoreManager scoreManager) {
        this.panelWidth = panelWidth;
        this.panelHeight = panelHeight;
        this.scoreManager = scoreManager;
    }

    // 生成指定关卡的砖块布局（简单示例：行列布局）
    public List<Brick> createLevel(int level) {
        this.currentLevel = level;
        List<Brick> bricks = new ArrayList<>();

        // 关卡参数随 level 递增
        int rows = Math.min(6 + level, 12);
        int cols = Math.min(8 + (level / 2), 14);

        int padding = 8;
        int totalWidth = panelWidth - padding * 2;
        int brickWidth = Math.max(32, (totalWidth - (cols - 1) * padding) / cols);
        int brickHeight = 20;

        int startX = padding;
        int startY = 60; // 从顶部偏移

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int x = startX + c * (brickWidth + padding);
                int y = startY + r * (brickHeight + padding);

                // 不同行使用不同类型砖块
                Brick b;
                if (r % 5 == 0 && level >= 2) {
                    b = Brick.createStrong(x, y, brickWidth, brickHeight);
                } else {
                    b = Brick.createNormal(x, y, brickWidth, brickHeight);
                }
                bricks.add(b);
            }
        }

        return bricks;
    }

    // 简单检查：场景中没有 Brick 或所有都被销毁则认为关卡通过
    public boolean isLevelCleared(List<Entity> entities) {
        for (Entity e : entities) {
            if (e instanceof Brick) {
                Brick b = (Brick) e;
                if (!b.isDestroyed()) return false;
            }
        }
        return true;
    }

    // 进入下一关（返回下一关编号）
    public int nextLevel() {
        currentLevel++;
        scoreManager.onLevelUp(currentLevel);
        return currentLevel;
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

}
