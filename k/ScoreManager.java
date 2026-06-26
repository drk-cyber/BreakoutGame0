/**
 * ScoreManager.java
 *
 * 管理分数、生命与一些游戏全局统计。
 */
public class ScoreManager {

    private int score = 0;
    private int lives = 3;

    public ScoreManager() {}

    public int getScore() { return score; }
    public int getLives() { return lives; }

    public void addScore(int v) { score += v; }
    public void decreaseLife() { lives -= 1; }
    public void increaseLife() { lives += 1; }

    public void reset() {
        score = 0;
        lives = 3;
    }

    public void onLevelUp(int level) {
        // 奖励生命或分数为示例
        score += level * 50;
        if (level % 3 == 0) {
            increaseLife();
        }
    }

}
