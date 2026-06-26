import javax.swing.SwingUtilities;

/**
 * BreakoutMain.java
 *
 * 启动类：在事件调度线程上创建窗口并开始游戏。
 *
 * 使用说明：
 *  - 编译所有 .java 文件后，运行：java BreakoutMain
 *
 * 这个文件演示类与方法的组织方式，包含主方法和一点启动日志。
 *
 * 该文件与其它类协作：GameWindow, GamePanel, 等。
 */
public class BreakoutMain {

    // 程序入口点
    public static void main(String[] args) {
        // 在 EDT（事件分发线程）上启动 Swing UI，遵循 Swing 线程约束
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                startGame();
            }
        });
    }

    // 启动游戏窗口并初始化环境
    private static void startGame() {
        // 创建窗口（GameWindow 的实现将在 GameWindow.java）
        GameWindow window = new GameWindow("Java 打砖块 - 演示版", 800, 600);
        window.setVisible(true);

        // 提示信息（简短）
        System.out.println("Breakout game started. Use left/right arrow keys or A/D to move paddle.");
        System.out.println("Press SPACE to pause/resume. Close window to exit.");
    }
                    
    }