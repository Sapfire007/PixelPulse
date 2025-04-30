import java.awt.*;

public class Enemy {
    protected double x, y;
    protected double speed = 1.5;
    protected double health = 50;
    protected Player player;
    protected GamePanel gamePanel;
    protected Rectangle bounds;
    
    public Enemy(double x, double y, Player player, GamePanel gamePanel) {
        this.x = x;
        this.y = y;
        this.player = player;
        this.gamePanel = gamePanel;
        this.bounds = new Rectangle((int)x - 10, (int)y - 10, 20, 20);
    }
    
    public void update() {
        // Move toward player
        double dx = player.getBounds().getCenterX() - x;
        double dy = player.getBounds().getCenterY() - y;
        double distance = Math.sqrt(dx * dx + dy * dy);
        
        if (distance > 0) {
            x += (dx / distance) * speed;
            y += (dy / distance) * speed;
        }
        
        bounds.setLocation((int)x - 10, (int)y - 10);
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
    
    public void draw(Graphics g) {
        g.setColor(Color.RED);
        g.fillRect((int)x - 10, (int)y - 10, 20, 20);
    }
    
    public void drawAtPosition(Graphics g, int x, int y) {
        g.setColor(Color.RED);
        g.fillRect(x, y, 20, 20);
    }
}


