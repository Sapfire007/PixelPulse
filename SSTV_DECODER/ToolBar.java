import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;


public class ToolBar extends JToolBar {
    private JButton openButton;
    private JButton decodeButton;
    private JButton viewButton;
    private JButton settingsButton;
    private JButton saveAsButton;
    
    public ToolBar() {
        setFloatable(false);
        setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        
        
        openButton = createToolbarButton("Open", "Open WAV File", "/icons/open.png");
        decodeButton = createToolbarButton("Decode", "Decode Selected File", "/icons/decode.png");
        viewButton = createToolbarButton("View", "View Decoded Image", "/icons/view.png");
        settingsButton = createToolbarButton("Settings", "Open Settings Dialog", "/icons/settings.png");
        saveAsButton = createToolbarButton("Save As", "Save Image As...", "/icons/save.png");
        
        
        add(openButton);
        addSeparator();
        add(decodeButton);
        add(viewButton);
        addSeparator();
        add(settingsButton);
        add(saveAsButton);
        
        

        decodeButton.setEnabled(false);
        viewButton.setEnabled(false);
        saveAsButton.setEnabled(false);
    }
    
    
    private JButton createToolbarButton(String text, String tooltip, String iconPath) {
        JButton button = new JButton(text);
        button.setToolTipText(tooltip);
        
       
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource(iconPath));
            if (icon.getIconWidth() > 0) {
                button.setIcon(icon);
                button.setText("");
            }
        } catch (Exception e) {
            System.out.println("Icon not found: " + iconPath);
        }
        
        return button;
    }
    
   
    public void setOpenAction(ActionListener listener) {
        openButton.addActionListener(listener);
    }
    
    public void setDecodeAction(ActionListener listener) {
        decodeButton.addActionListener(listener);
    }
    
    public void setViewAction(ActionListener listener) {
        viewButton.addActionListener(listener);
    }
    
    public void setSettingsAction(ActionListener listener) {
        settingsButton.addActionListener(listener);
    }
    
    public void setSaveAsAction(ActionListener listener) {
        saveAsButton.addActionListener(listener);
    }
    
    
    public void setFileSelected(boolean selected) {
        decodeButton.setEnabled(selected);
    }
    
    public void setImageAvailable(boolean available) {
        viewButton.setEnabled(available);
        saveAsButton.setEnabled(available);
    }
}
