import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.ArrayList;
import javax.swing.Timer;

public class GamePanel extends JPanel implements KeyListener, MouseListener, MouseMotionListener {
    private GameStateManager gsm;
    private Player player;
    private List<Enemy> enemies;
    private List<Bullet> bullets;
    private Boss boss;
    private int currentWave = 1;
    private boolean[] keys = new boolean[256];
    private Point mousePosition = new Point(0, 0);
    private boolean gameOver = false;
    private boolean victory = false;
    
    public GamePanel(GameStateManager gsm) {
        this.gsm = gsm;
        setPreferredSize(new Dimension(800, 600));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
        
        resetGame();
        
        Timer gameTimer = new Timer(16, e -> {
            if (!gameOver && !victory) {
                update();
            }
            repaint();
        });
        gameTimer.start();
    }
    
    public void resetGame() {
        player = new Player(400, 300);
        enemies = new ArrayList<>();
        bullets = new ArrayList<>();
        boss = null;
        currentWave = 1;
        gameOver = false;
        victory = false;
        spawnWave(currentWave);
    }
    
    private void spawnWave(int wave) {
        int enemyCount = 3 + wave * 2;
        
        for (int i = 0; i < enemyCount; i++) {
            int side = (int)(Math.random() * 4);
            int x = 0, y = 0;
            
            switch (side) {
                case 0: x = 0; y = (int)(Math.random() * getHeight()); break;
                case 1: x = getWidth(); y = (int)(Math.random() * getHeight()); break;
                case 2: x = (int)(Math.random() * getWidth()); y = 0; break;
                case 3: x = (int)(Math.random() * getWidth()); y = getHeight(); break;
            }
            
            if (wave >= 4 && Math.random() < (wave == 5 ? 0.05 : 0.01)) {
                enemies.add(new ShooterEnemy(x, y, player, this));
            } else {
                enemies.add(new Enemy(x, y, player, this));
            }
        }
        
        if (wave == 5) {
            boss = new Boss(getWidth() / 2, 100, player, this);
        }
    }
    
    private void update() {
        // Player movement
        int dx = 0, dy = 0;
        if (keys[KeyEvent.VK_W]) dy -= 5;
        if (keys[KeyEvent.VK_S]) dy += 5;
        if (keys[KeyEvent.VK_A]) dx -= 5;
        if (keys[KeyEvent.VK_D]) dx += 5;
        
        player.move(dx, dy, getWidth(), getHeight());
        
        // Update enemies
        for (Enemy enemy : new ArrayList<>(enemies)) {
            enemy.update();
            
            if (enemy.getBounds().intersects(player.getBounds())) {
                player.takeDamage(10);
                enemies.remove(enemy);
            }
        }
        
        // Update boss
        if (boss != null) {
            boss.update();
            
            if (boss.getBounds().intersects(player.getBounds())) {
                player.takeDamage(20);
            }
            
            if (boss.isDead()) {
                victory = true;
                gsm.changeState(GameStateManager.GameState.VICTORY);
                return;
            }
        }
        
        // Update bullets
        for (Bullet bullet : new ArrayList<>(bullets)) {
            bullet.update();
            
            if (bullet.getX() < 0 || bullet.getX() > getWidth() || 
                bullet.getY() < 0 || bullet.getY() > getHeight()) {
                bullets.remove(bullet);
                continue;
            }
            
            Rectangle bulletBounds = bullet.getBounds();
            
            if (bullet.isPlayerBullet()) {
                for (Enemy enemy : new ArrayList<>(enemies)) {
                    if (enemy.getBounds().intersects(bulletBounds)) {
                        enemy.takeDamage(25);
                        bullets.remove(bullet);
                        if (enemy.isDead()) {
                            enemies.remove(enemy);
                        }
                        break;
                    }
                }
                
                if (boss != null && boss.getBounds().intersects(bulletBounds)) {
                    boss.takeDamage(25);
                    bullets.remove(bullet);
                }
            } else {
                if (player.getBounds().intersects(bulletBounds)) {
                    player.takeDamage(10);
                    bullets.remove(bullet);
                }
            }
        }
        
        if (enemies.isEmpty() && boss == null && currentWave < 5) {
            currentWave++;
            spawnWave(currentWave);
        }
        
        if (player.isDead()) {
            gameOver = true;
            gsm.changeState(GameStateManager.GameState.GAME_OVER);
        }
    }
    
    public void addBullet(Bullet bullet) {
        bullets.add(bullet);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // Draw background
        // Draw player centered
        player.drawAtPosition(g, getWidth()/2, getHeight()/2);
        
        // Calculate camera offset
        int cameraX = (int)player.getX() - getWidth()/2;
        int cameraY = (int)player.getY() - getHeight()/2;

        // Draw player centered

        // Draw enemies with camera offset
        for (Enemy enemy : enemies) {
            int drawX = (int)enemy.x - cameraX - 10;
            int drawY = (int)enemy.y - cameraY - 10;
            enemy.drawAtPosition(g, drawX, drawY);
        }

        // Draw boss with camera offset
        if (boss != null) {
            int drawX = (int)boss.x - cameraX - 25;
            int drawY = (int)boss.y - cameraY - 25;
            boss.drawAtPosition(g, drawX, drawY);
        }

        // Draw bullets with camera offset
        for (Bullet bullet : bullets) {
            int drawX = (int)bullet.getX() - cameraX - 2;
            int drawY = (int)bullet.getY() - cameraY - 2;
            bullet.drawAtPosition(g, drawX, drawY);
        }

        // Draw UI (unchanged position)
        drawUI(g);
    }
    
    private void drawUI(Graphics g) {
        // Health bar
        g.setColor(Color.RED);
        g.fillRect(20, 20, 200, 20);
        g.setColor(Color.GREEN);
        g.fillRect(20, 20, (int)(200 * (player.getHealth() / 100.0)), 20);
        g.setColor(Color.WHITE);
        g.drawRect(20, 20, 200, 20);
        g.drawString("Health: " + (int)player.getHealth(), 25, 35);
        
        // Wave counter
        g.drawString("Wave: " + currentWave + "/5", 20, 60);
        
        // Enemy counter
        g.drawString("Enemies: " + enemies.size(), 20, 80);
        
        // Boss health
        if (boss != null) {
            g.setColor(Color.RED);
            g.fillRect(getWidth() / 2 - 100, 10, 200, 15);
            g.setColor(Color.ORANGE);
            g.fillRect(getWidth() / 2 - 100, 10, (int)(200 * (boss.getHealth() / (double)Boss.MAX_HEALTH)), 15);
            g.setColor(Color.WHITE);
            g.drawRect(getWidth() / 2 - 100, 10, 200, 15);
            g.drawString("BOSS", getWidth() / 2 - 15, 22);
        }
        
        // Crosshair
        g.setColor(Color.RED);
        int crosshairSize = 20;
        g.drawLine(mousePosition.x - crosshairSize, mousePosition.y, mousePosition.x + crosshairSize, mousePosition.y);
        g.drawLine(mousePosition.x, mousePosition.y - crosshairSize, mousePosition.x, mousePosition.y + crosshairSize);
    }
    
    // Input handling methods
    @Override
    public void keyPressed(KeyEvent e) {
        keys[e.getKeyCode()] = true;
    }
    
    @Override
    public void keyReleased(KeyEvent e) {
        keys[e.getKeyCode()] = false;
    }
    
    @Override
    public void keyTyped(KeyEvent e) {}
    
    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            player.shoot(mousePosition.x + (int)player.getX() - getWidth()/2, 
                        mousePosition.y + (int)player.getY() - getHeight()/2, 
                        this);
        }
    }
    
    @Override
    public void mousePressed(MouseEvent e) {}
    
    @Override
    public void mouseReleased(MouseEvent e) {}
    
    @Override
    public void mouseEntered(MouseEvent e) {}
    
    @Override
    public void mouseExited(MouseEvent e) {}
    
    @Override
    public void mouseMoved(MouseEvent e) {
        mousePosition = e.getPoint();
    }
    
    @Override
    public void mouseDragged(MouseEvent e) {
        mousePosition = e.getPoint();
    }
}