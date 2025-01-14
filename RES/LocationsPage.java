import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class LocationsPage extends JFrame {
    private JComboBox<LocationItem> locationComboBox;
    
    public LocationsPage() {
        setTitle("Select Location");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        BackgroundPanel mainPanel = new BackgroundPanel("bg3.jpg");
        mainPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        
        JLabel titleLabel = new JLabel("");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        
        locationComboBox = new JComboBox<>();
        locationComboBox.setPreferredSize(new Dimension(300, 30));
        
        JButton viewButton = new JButton("View Restaurants");
        viewButton.addActionListener(e -> {
            LocationItem selected = (LocationItem) locationComboBox.getSelectedItem();
            if (selected != null) {
                new RestaurantPage(selected.getId()).setVisible(true);
                dispose();
            }
        });
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 20, 0);
        mainPanel.add(titleLabel, gbc);
        
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 10, 0);
        mainPanel.add(locationComboBox, gbc);
        
        gbc.gridy = 2;
        mainPanel.add(viewButton, gbc);
        
        add(mainPanel);
        loadLocations();
    }
    
    private void loadLocations() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT id, address, city, state FROM locations";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            while (rs.next()) {
                LocationItem item = new LocationItem(
                    rs.getString("id"),
                    rs.getString("address"),
                    rs.getString("city"),
                    rs.getString("state")
                );
                locationComboBox.addItem(item);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Error loading locations: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    class BackgroundPanel extends JPanel {
        private Image backgroundImage;
        
        public BackgroundPanel(String imagePath) {
            try {
                backgroundImage = new ImageIcon(imagePath).getImage();
            } catch (Exception e) {
                System.err.println("Error loading background image: " + e.getMessage());
            }
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (backgroundImage != null) {
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            }
        }
    }
}

