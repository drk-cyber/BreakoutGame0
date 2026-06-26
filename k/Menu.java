import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Font;

/**
 * Menu.java
 *
 * 简易菜单绘制类，显示开始、暂停与结束界面。
 */
public class Menu {

    public enum State { MAIN, PAUSED, GAMEOVER }

    private State state = State.MAIN;

    public Menu() {}

    public void setState(State s) { this.state = s; }
    public State getState() { return state; }

    public void render(Graphics2D g, int w, int h) {
        g.setColor(new Color(0,0,0,160));
        g.fillRect(0,0,w,h);
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 36));
        switch (state) {
            case MAIN:
                g.drawString("Press SPACE to Start", w/2 - 180, h/2);
                break;
            case PAUSED:
                g.drawString("PAUSED", w/2 - 60, h/2);
                break;
            case GAMEOVER:
                g.drawString("GAME OVER", w/2 - 100, h/2);
                break;
        }
    }

}
