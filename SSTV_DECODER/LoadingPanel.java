import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;


public class LoadingPanel extends JPanel {
    private final String message;
    private int dotCount = 0;
    private Timer animationTimer;
    private JButton cancelButton;

    public LoadingPanel(String message) {
        this.message = message;
        setOpaque(false);
        setLayout(null); 

        
        cancelButton = new JButton("Cancel");
        cancelButton.setFocusPainted(false);
        cancelButton.setBackground(new Color(220, 53, 69));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFont(new Font("Arial", Font.BOLD, 14));
        add(cancelButton);

       
        animationTimer = new Timer(500, e -> {
            dotCount = (dotCount + 1) % 4;
            repaint();
        });
        animationTimer.start();
    }

    

    public void setCancelAction(ActionListener listener) {
        cancelButton.addActionListener(listener);
    }

    @Override
    public void doLayout() {
        super.doLayout();

        
        if (cancelButton != null) {
            Dimension buttonSize = new Dimension(100, 30);
            cancelButton.setSize(buttonSize);
            int x = (getWidth() - buttonSize.width) / 2;
            int y = getHeight() / 2 + 80; 
            cancelButton.setLocation(x, y);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRect(0, 0, getWidth(), getHeight());

        

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 18));

        

        StringBuilder dots = new StringBuilder();
        for (int i = 0; i < dotCount; i++) {
            dots.append(".");
        }

        String displayMessage = message + dots.toString();

        

        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(displayMessage);
        int textHeight = fm.getHeight();
        int x = (getWidth() - textWidth) / 2;
        int y = (getHeight() - textHeight) / 2 + fm.getAscent();

        g2d.drawString(displayMessage, x, y);

        
        int spinnerSize = 40;
        int spinnerX = getWidth() / 2;
        int spinnerY = y + 40;

        g2d.setStroke(new BasicStroke(4));
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        
        int startAngle = (dotCount * 90) % 360;
        g2d.drawArc(spinnerX - spinnerSize/2, spinnerY - spinnerSize/2,
                   spinnerSize, spinnerSize, startAngle, 270);

        g2d.dispose();
    }

    public void stopAnimation() {
        if (animationTimer != null) {
            animationTimer.stop();
        }
    }
}
