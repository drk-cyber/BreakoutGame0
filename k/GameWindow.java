import javax.swing.JFrame;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Component;

/**
 * GameWindow.java
 *
 * 窗口类：创建主窗口并承载 GamePanel（游戏绘制区）。
 *
 * 构造时可传入标题与大小。GamePanel 在下一批提供，将被添加到窗口中心。
 *
 * 窗口类使用继承自 JFrame 的特性并封装一些窗口相关的配置逻辑。
 */
public class GameWindow extends JFrame {

    private final int width;
    private final int height;
    // 将面板作为字段保存，避免局部变量在匿名类中产生作用域问题
    private Component panel;

    // 构造器：设置标题与大小
    public GameWindow(String title, int width, int height) {
        super(title);
        this.width = width;
        this.height = height;
        initUI();
    }

    // 初始化 UI：配置 JFrame 属性并创建/添加 GamePanel
    private void initUI() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setResizable(false);

        // 创建游戏面板（优先创建 GamePanel，否则回退到普通 Component）
        try {
            panel = new GamePanel(width, height);
        } catch (Throwable t) {
            // 如果 GamePanel 发生异常，使用空白 Component 作为回退
            panel = new Component() {};
        }

        panel.setPreferredSize(new Dimension(width, height));
        add(panel, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null); // 居中显示

        // 确保在 EDT 上启动游戏循环并创建 BufferStrategy 在 canvas 上
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (panel != null) panel.requestFocusInWindow();
                // 如果 panel 是 GamePanel（Canvas），为其创建 BufferStrategy
                try {
                    if (panel instanceof GamePanel) {
                        ((GamePanel) panel).createBufferStrategy(2);
                        ((GamePanel) panel).requestFocus();
                    }
                } catch (Throwable t) {
                    // 有些环境可能不支持，忽略
                }
                // 如果 GamePanel 提供 start 方法，这里可以调用：
                // if (panel instanceof GamePanel) ((GamePanel) panel).start();
            }
        });
    }

    // 公开获取窗口宽高的访问器
    public int getGameWidth() {
        return width;
    }

    public int getGameHeight() {
        return height;
    }

    // 窗口销毁时做清理
    @Override
    public void dispose() {
        // 可以在这里通知 GamePanel 停止线程，释放资源等
        super.dispose();
    }

    /*
     * 备注：
     *  - GameWindow 依赖 GamePanel 的存在。后续批次会提供 GamePanel.java，
     *    里面实现游戏主循环、输入处理、渲染与资源管理。
     *  - 本文件保持简单，展示 JFrame 的封装与窗口级方法（面向对象：继承 + 封装）。
     */

}