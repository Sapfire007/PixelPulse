import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GameStateManager extends JPanel {
    public enum GameState { LOADING, MENU, GAME, GAME_OVER, VICTORY }
    
    private GameState currentState;
    private JFrame parentFrame;
    private LoadingScreen loadingScreen;
    private MainMenu mainMenu;
    private GamePanel gamePanel;
    private JPanel endScreen;
    
    public GameStateManager(JFrame frame) {
        this.parentFrame = frame;
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(800, 600));
        
        loadingScreen = new LoadingScreen(this);
        mainMenu = new MainMenu(this);
        gamePanel = new GamePanel(this);
        
        currentState = GameState.LOADING;
        add(loadingScreen, BorderLayout.CENTER);
    }
    
    public void changeState(GameState newState) {
        removeAll();
        currentState = newState;
        
        switch (newState) {
            case LOADING:
                add(loadingScreen, BorderLayout.CENTER);
                break;
            case MENU:
                add(mainMenu, BorderLayout.CENTER);
                break;
            case GAME:
                gamePanel.resetGame();
                add(gamePanel, BorderLayout.CENTER);
                gamePanel.requestFocusInWindow();
                break;
            case GAME_OVER:
                createEndScreen(false);
                add(endScreen, BorderLayout.CENTER);
                break;
            case VICTORY:
                createEndScreen(true);
                add(endScreen, BorderLayout.CENTER);
                break;
        }
        
        revalidate();
        repaint();
    }
    
    private void createEndScreen(boolean victory) {
        endScreen = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(0, 0, 0, 200));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.insets = new Insets(10, 0, 30, 0);
        
        JLabel title = new JLabel(victory ? "YOU WON!" : "WASTED");
        title.setFont(new Font("Arial", Font.BOLD, 72));
        title.setForeground(victory ? Color.GREEN : Color.RED);
        endScreen.add(title, gbc);
        
        JButton replayButton = new JButton("Replay");
        replayButton.addActionListener(e -> changeState(GameState.GAME));
        endScreen.add(replayButton, gbc);
        
        JButton menuButton = new JButton("Main Menu");
        menuButton.addActionListener(e -> changeState(GameState.MENU));
        endScreen.add(menuButton, gbc);
    }
    
    public void startGame() {
        changeState(GameState.LOADING);
        
        Timer loadingTimer = new Timer(3000, e -> {
            changeState(GameState.MENU);
        });
        loadingTimer.setRepeats(false);
        loadingTimer.start();
    }
}