import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.io.*;

/**
 * HighScoreManager.java
 *
 * 支持磁盘持久化的高分管理：将分数保存为每行一个整数的文本文件。
 */
public class HighScoreManager {

    private final List<Integer> topScores = new ArrayList<>();
    private final int capacity;
    private final File file;

    public HighScoreManager(int capacity, String filePath) {
        this.capacity = capacity;
        this.file = new File(filePath);
        load();
    }

    // 加分并持久化
    public synchronized void addScore(int score) {
        topScores.add(score);
        Collections.sort(topScores, Collections.reverseOrder());
        while (topScores.size() > capacity) topScores.remove(topScores.size() - 1);
        save();
    }

    public synchronized List<Integer> getTopScores() {
        return new ArrayList<>(topScores);
    }

    private void load() {
        topScores.clear();
        if (!file.exists()) return;
        try (BufferedReader r = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = r.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                try {
                    topScores.add(Integer.parseInt(line));
                } catch (NumberFormatException ex) {
                    // ignore malformed line
                }
            }
            Collections.sort(topScores, Collections.reverseOrder());
            while (topScores.size() > capacity) topScores.remove(topScores.size() - 1);
        } catch (IOException e) {
            System.err.println("Failed to load high scores: " + e.getMessage());
        }
    }

    private void save() {
        try (BufferedWriter w = new BufferedWriter(new FileWriter(file))) {
            for (Integer s : topScores) {
                w.write(String.valueOf(s));
                w.newLine();
            }
        } catch (IOException e) {
            System.err.println("Failed to save high scores: " + e.getMessage());
        }
    }

}
