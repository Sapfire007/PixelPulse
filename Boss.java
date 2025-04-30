import java.awt.*;
public class Boss extends Enemy {
    public static final double MAX_HEALTH = 500;
    private long lastShotTime = 0;
    private static final int SHOOT_COOLDOWN = 2000; // ms
    
    public Boss(double x, double y, Player player, GamePanel gamePanel) {
        super(x, y, player, gamePanel);
        this.health = MAX_HEALTH;
        this.speed = 0.7;
        this.bounds = new Rectangle((int)x - 25, (int)y - 25, 50, 50);
    }
    
    @Override
    public void update() {
        // Move toward player but slower
        double dx = player.getBounds().getCenterX() - x;
        double dy = player.getBounds().getCenterY() - y;
        double distance = Math.sqrt(dx * dx + dy * dy);
        
        if (distance > 0) {
            x += (dx / distance) * speed;
            y += (dy / distance) * speed;
        }
        
        bounds.setLocation((int)x - 25, (int)y - 25);
        
        // Shoot at player periodically with spread
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastShotTime > SHOOT_COOLDOWN) {
            lastShotTime = currentTime;
            
            double baseAngle = Math.atan2(
                player.getBounds().getCenterY() - y,
                player.getBounds().getCenterX() - x
            );
            
            // Shoot multiple bullets in a spread pattern
            for (int i = -2; i <= 2; i++) {
                double angle = baseAngle + (i * 0.2);
                gamePanel.addBullet(new Bullet(x, y, angle, false));
            }
        }
    }
    
    @Override
    public void draw(Graphics g) {
        g.setColor(Color.MAGENTA);
        g.fillRect((int)x - 25, (int)y - 25, 50, 50);
        
        // Draw health bar above boss
        g.setColor(Color.RED);
        g.fillRect((int)x - 25, (int)y - 40, 50, 5);
        g.setColor(Color.GREEN);
        g.fillRect((int)x - 25, (int)y - 40, (int)(50 * (health / MAX_HEALTH)), 5);
    }
    
    @Override
    public void drawAtPosition(Graphics g, int x, int y) {
        g.setColor(Color.MAGENTA);
        g.fillRect(x, y, 50, 50);
        
        // Draw health bar above boss
        g.setColor(Color.RED);
        g.fillRect(x, y - 15, 50, 5);
        g.setColor(Color.GREEN);
        g.fillRect(x, y - 15, (int)(50 * (health / MAX_HEALTH)), 5);
    }
}