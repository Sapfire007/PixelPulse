import java.awt.*;
public class ShooterEnemy extends Enemy {
    private long lastShotTime = 0;
    private static final int SHOOT_COOLDOWN = 2000; // ms
    
    public ShooterEnemy(double x, double y, Player player, GamePanel gamePanel) {
        super(x, y, player, gamePanel);
        speed = 1.0; // Shooter enemies are slower
        health = 30; // Less health than regular enemies
    }
    
    @Override
    public void update() {
        super.update();
        
        // Shoot at player periodically
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastShotTime > SHOOT_COOLDOWN) {
            lastShotTime = currentTime;
            
            double angle = Math.atan2(
                player.getBounds().getCenterY() - y,
                player.getBounds().getCenterX() - x
            );
            gamePanel.addBullet(new Bullet(x, y, angle, false));
        }
    }
    
    @Override
    public void draw(Graphics g) {
        g.setColor(Color.ORANGE);
        g.fillRect((int)x - 10, (int)y - 10, 20, 20);
    }
}
