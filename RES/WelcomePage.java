import javax.swing.*;
import java.awt.*;

// Custom panel class that handles background image scaling
class BackgroundPanel extends JPanel {
    private Image backgroundImage;

    public BackgroundPanel(String imagePath) {
        backgroundImage = new ImageIcon(imagePath).getImage();
        setLayout(new GridBagLayout()); // Use GridBagLayout for components
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Draw the background image to fit the current panel size
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }
}

public class WelcomePage extends JFrame {
    public WelcomePage() {
        setTitle("Welcome to Restaurant Review System");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Create the background panel instead of a label
        BackgroundPanel backgroundPanel = new BackgroundPanel("bg1.jpg");
        
        GridBagConstraints gbc = new GridBagConstraints();
        
        JLabel welcomeLabel = new JLabel("");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        welcomeLabel.setForeground(Color.WHITE); // Make text visible against background
        
        JButton nextButton = new JButton("Next");
        nextButton.addActionListener(e -> {
            new LocationsPage().setVisible(true);
            dispose();
        });
        
        // Style the button
        nextButton.setPreferredSize(new Dimension(100, 30));
        nextButton.setBackground(new Color(51, 122, 183));
        nextButton.setForeground(Color.WHITE);
        nextButton.setFocusPainted(false);
        
        // Add components to the background panel
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 20, 0);
        backgroundPanel.add(welcomeLabel, gbc);
        
        gbc.gridy = 1;
        backgroundPanel.add(nextButton, gbc);
        
        // Add the background panel to the frame
        add(backgroundPanel);
        
        // Make the frame resizable
        setResizable(true);
    }

  /*   public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new WelcomePage().setVisible(true));
    } */
public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> 
        new WelcomePage().setVisible(true));
}
}

