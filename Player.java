import java.awt.*;

public class Player {
    private double x, y;
    private double health = 100;
    private Rectangle bounds;
    private long lastShotTime = 0;
    private static final int SHOOT_COOLDOWN = 300; // ms
    
    public Player(double x, double y) {
        this.x = x;
        this.y = y;
        this.bounds = new Rectangle((int)x - 15, (int)y - 15, 30, 30);
    }
    
    public void move(int dx, int dy, int maxWidth, int maxHeight) {
        x = Math.max(15, Math.min(maxWidth - 15, x + dx));
        y = Math.max(15, Math.min(maxHeight - 15, y + dy));
        bounds.setLocation((int)x - 15, (int)y - 15);
    }
    
    public void shoot(int targetX, int targetY, GamePanel gamePanel) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastShotTime < SHOOT_COOLDOWN) return;
        
        lastShotTime = currentTime;
        
        double angle = Math.atan2(targetY - y, targetX - x);
        gamePanel.addBullet(new Bullet(x, y, angle, true));
    }
    
    public void takeDamage(double amount) {
        health -= amount;
    }
    
    public boolean isDead() {
        return health <= 0;
    }
    
    public Rectangle getBounds() {
        return bounds;
    }
    
    public double getHealth() {
        return health;
    }
    
    public double getX() {
        return x;
    }
    
    public double getY() {
        return y;
    }
    
    public void draw(Graphics g) {
        g.setColor(Color.BLUE);
        g.fillRect((int)x - 15, (int)y - 15, 30, 30);
    }
    public void drawAtPosition(Graphics g, int drawX, int drawY) {
        g.setColor(Color.BLUE);
        g.fillRect(drawX - 15, drawY - 15, 30, 30);
        g.setColor(Color.RED);
        g.drawRect(drawX - 15, drawY - 15, 30, 30);
    }
        
}