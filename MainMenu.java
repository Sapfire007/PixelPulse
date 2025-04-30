import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MainMenu extends JPanel {
    private GameStateManager gsm;
    
    public MainMenu(GameStateManager gsm) {
        this.gsm = gsm;
        setLayout(new GridBagLayout());
        setBackground(Color.BLACK);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.insets = new Insets(10, 0, 30, 0);
        
        JLabel title = new JLabel("2D FPS GAME");
        title.setFont(new Font("Arial", Font.BOLD, 48));
        title.setForeground(Color.RED);
        add(title, gbc);
        
        JButton playButton = new JButton("PLAY");
        styleButton(playButton);
        playButton.addActionListener(e -> gsm.changeState(GameStateManager.GameState.GAME));
        add(playButton, gbc);
        
        JButton exitButton = new JButton("EXIT");
        styleButton(exitButton);
        exitButton.addActionListener(e -> System.exit(0));
        add(exitButton, gbc);
    }
    
    private void styleButton(JButton button) {
        button.setPreferredSize(new Dimension(200, 50));
        button.setFont(new Font("Arial", Font.BOLD, 24));
        button.setBackground(Color.DARK_GRAY);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
    }
}