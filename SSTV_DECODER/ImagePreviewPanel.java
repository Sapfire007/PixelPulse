import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;


public class ImagePreviewPanel extends JPanel {
    private BufferedImage image;
    private boolean showGrid;
    private int currentLine;
    
    public ImagePreviewPanel() {
        setBackground(Color.BLACK);
        setBorder(BorderFactory.createTitledBorder("Image Preview"));
        showGrid = true;
        currentLine = 0;
    }
    
    
    public void setImage(BufferedImage image) {
        this.image = image;
        repaint();
    }
    
    
    public void setCurrentLine(int line) {
        this.currentLine = line;
        repaint();
    }
    
    
    public void setShowGrid(boolean show) {
        this.showGrid = show;
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        if (image == null) {
            g.setColor(Color.DARK_GRAY);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(Color.LIGHT_GRAY);
            g.drawString("No image preview available", getWidth()/2 - 80, getHeight()/2);
            return;
        }
        
        
        double scale = Math.min(
            (double) getWidth() / image.getWidth(),
            (double) getHeight() / image.getHeight()
        );
        
        int scaledWidth = (int) (image.getWidth() * scale);
        int scaledHeight = (int) (image.getHeight() * scale);
        
        
        int x = (getWidth() - scaledWidth) / 2;
        int y = (getHeight() - scaledHeight) / 2;
        
        
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(image, x, y, scaledWidth, scaledHeight, null);
        
        // Draw grid if enabled
        if (showGrid) {
            g2d.setColor(new Color(50, 50, 50, 100));
            
            
            for (int i = 0; i <= image.getWidth(); i += 50) {
                int gridX = x + (int)(i * scale);
                g2d.drawLine(gridX, y, gridX, y + scaledHeight);
            }
            
            
            for (int i = 0; i <= image.getHeight(); i += 50) {
                int gridY = y + (int)(i * scale);
                g2d.drawLine(x, gridY, x + scaledWidth, gridY);
            }
        }
        
        
        if (currentLine > 0 && currentLine < image.getHeight()) {
            int lineY = y + (int)(currentLine * scale);
            g2d.setColor(new Color(255, 255, 0, 150));
            g2d.drawLine(x, lineY, x + scaledWidth, lineY);
        }
        
        g2d.dispose();
    }
}
