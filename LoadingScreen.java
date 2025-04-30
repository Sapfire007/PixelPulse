import javax.swing.*;
import java.awt.*;

public class LoadingScreen extends JPanel {
    private GameStateManager gsm;
    private int dotCount = 0;
    
    public LoadingScreen(GameStateManager gsm) {
        this.gsm = gsm;
        setBackground(Color.BLACK);
        
        Timer dotTimer = new Timer(500, e -> {
            dotCount = (dotCount + 1) % 4;
            repaint();
        });
        dotTimer.start();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.BOLD, 36));
        
        String dots = ".".repeat(dotCount);
        String text = "LOADING" + dots;
        
        FontMetrics fm = g.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(text)) / 2;
        int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
        
        g.drawString(text, x, y);
    }
}