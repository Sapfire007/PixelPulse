import java.awt.*;

public class Bullet {
    private double x, y;
    private double angle;
    private double speed = 7.0;
    private boolean isPlayerBullet;
    private Rectangle bounds;
    
    public Bullet(double x, double y, double angle, boolean isPlayerBullet) {
        this.x = x;
        this.y = y;
        this.angle = angle;
        this.isPlayerBullet = isPlayerBullet;
        this.bounds = new Rectangle((int)x - 2, (int)y - 2, 4, 4);
    }
    
    public void update() {
        x += Math.cos(angle) * speed;
        y += Math.sin(angle) * speed;
        bounds.setLocation((int)x - 2, (int)y - 2);
    }
    
    public Rectangle getBounds() {
        return bounds;
    }
    
    public boolean isPlayerBullet() {
        return isPlayerBullet;
    }
    
    public double getX() {
        return x;
    }
    
    public double getY() {
        return y;
    }
    
    public void draw(Graphics g) {
        g.setColor(isPlayerBullet ? Color.CYAN : Color.YELLOW);
        g.fillRect((int)x - 2, (int)y - 2, 4, 4);
    }
    
    public void drawAtPosition(Graphics g, int x, int y) {
        g.setColor(isPlayerBullet ? Color.CYAN : Color.YELLOW);
        g.fillRect(x, y, 4, 4);
    }
}