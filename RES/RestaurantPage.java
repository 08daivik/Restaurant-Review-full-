import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.sql.*;

public class RestaurantPage extends JFrame {
    private String locationId;
    private JPanel restaurantsPanel;
    private JScrollPane scrollPane;
    
    public RestaurantPage(String locationId) {
        this.locationId = locationId;
        setTitle("Restaurants");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Create main container panel
        JPanel mainPanel = new JPanel(new BorderLayout(0, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create top panel for the back button
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton backButton = new JButton("Back to Locations");
        backButton.setFont(new Font("Arial", Font.BOLD, 12));
        backButton.setPreferredSize(new Dimension(150, 30));
        backButton.addActionListener(e -> {
            new LocationsPage().setVisible(true);
            dispose();
        });
        topPanel.add(backButton);
        
        // Restaurant listings panel
        restaurantsPanel = new JPanel();
        restaurantsPanel.setLayout(new BoxLayout(restaurantsPanel, BoxLayout.Y_AXIS));
        
        // Scrollable container
        scrollPane = new JScrollPane(restaurantsPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        // Add components to main panel
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Add main panel to frame
        add(mainPanel);
        
        loadRestaurants();
    }
    
    private void loadRestaurants() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = """
                SELECT r.id, r.name, c.name as cuisine, dt.type as dining_type, ft.type as food_type 
                FROM restaurants r 
                LEFT JOIN cuisines c ON r.cuisineId = c.id 
                LEFT JOIN dining_types dt ON r.diningTypeId = dt.id 
                LEFT JOIN food_types ft ON r.foodTypeId = ft.id 
                WHERE r.locationId = ?
            """;
            
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, locationId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                JPanel restaurantPanel = createRestaurantPanel(
                    rs.getString("id"),
                    rs.getString("name"),
                    rs.getString("cuisine"),
                    rs.getString("dining_type"),
                    rs.getString("food_type")
                );
                restaurantsPanel.add(restaurantPanel);
                restaurantsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Error loading restaurants: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createRestaurantPanel(String id, String name, String cuisine, 
                                     String diningType, String foodType) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 5));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // Image Panel
        JLabel imageLabel = new JLabel();
        String imagePath = "images/restaurant_" + id + ".jpg";
        File imageFile = new File(imagePath);
        if (!imageFile.exists()) {
            imagePath = "images/default.jpg";
        }
        ImageIcon imageIcon = new ImageIcon(imagePath);
        Image image = imageIcon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
        imageLabel.setIcon(new ImageIcon(image));

        // Information Panel
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.add(new JLabel("Name: " + name));
        infoPanel.add(new JLabel("Cuisine: " + cuisine));
        infoPanel.add(new JLabel("Dining Type: " + diningType));
        infoPanel.add(new JLabel("Food Type: " + foodType));

        // Reviews Panel
        JPanel reviewsPanel = new JPanel(new BorderLayout());
        JTextArea reviewsArea = new JTextArea(5, 30);
        reviewsArea.setEditable(false);
        loadReviews(id, reviewsArea);

        JButton addReviewBtn = new JButton("Add Review");
        addReviewBtn.addActionListener(e -> showAddReviewDialog(id));
        reviewsPanel.add(new JScrollPane(reviewsArea), BorderLayout.CENTER);
        reviewsPanel.add(addReviewBtn, BorderLayout.SOUTH);

        panel.add(imageLabel, BorderLayout.WEST);
        panel.add(infoPanel, BorderLayout.CENTER);
        panel.add(reviewsPanel, BorderLayout.EAST);

        return panel;
    }
    
    private void loadReviews(String restaurantId, JTextArea reviewsArea) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = """
                SELECT rating, comments, dates 
                FROM reviews 
                WHERE restaurantid = ? 
                ORDER BY dates DESC
            """;
            
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, restaurantId);
            ResultSet rs = pstmt.executeQuery();
            
            StringBuilder reviews = new StringBuilder();
            while (rs.next()) {
                reviews.append("Rating: ").append(rs.getInt("rating"))
                      .append("\nComment: ").append(rs.getString("comments"))
                      .append("\nDate: ").append(rs.getTimestamp("dates"))
                      .append("\n\n");
            }
            reviewsArea.setText(reviews.toString());
        } catch (SQLException ex) {
            reviewsArea.setText("Error loading reviews: " + ex.getMessage());
        }
    }
    
    private void showAddReviewDialog(String restaurantId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String verifyQuery = "SELECT id FROM restaurants WHERE id = ?";
            
            try (PreparedStatement verifyStmt = conn.prepareStatement(verifyQuery)) {
                verifyStmt.setString(1, restaurantId);
                
                try (ResultSet rs = verifyStmt.executeQuery()) {
                    if (!rs.next()) {
                        JOptionPane.showMessageDialog(this,
                            "Error: Restaurant ID does not exist in the database.",
                            "Database Error",
                            JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    String confirmedId = rs.getString("id");
                    showReviewInputDialog(confirmedId);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error verifying restaurant: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void showReviewInputDialog(String confirmedRestaurantId) {
        JDialog dialog = new JDialog(this, "Add Review", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
    
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
    
        JComboBox<Integer> ratingBox = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5});
        JTextArea commentArea = new JTextArea(5, 30);
        JButton submitBtn = new JButton("Submit Review");
    
        submitBtn.addActionListener(e -> {
            if (commentArea.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog,
                    "Please enter a comment",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
    
            try (Connection conn = DatabaseConnection.getConnection()) {
                String query = """
                    INSERT INTO reviews (id, userid, restaurantid, rating, comments, dates) 
                    VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                """;
    
                String reviewId = "RV" + System.currentTimeMillis() % 100000;
    
                try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                    pstmt.setString(1, reviewId);
                    pstmt.setString(2, "GUEST");
                    pstmt.setString(3, confirmedRestaurantId);
                    pstmt.setInt(4, (Integer) ratingBox.getSelectedItem());
                    pstmt.setString(5, commentArea.getText().trim());
    
                    pstmt.executeUpdate();
                    
                    JOptionPane.showMessageDialog(dialog,
                        "Review added successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadRestaurants();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                String errorMsg = ex.getMessage();
                if (errorMsg.contains("ORA-02291")) {
                    JOptionPane.showMessageDialog(dialog,
                        "Error: Invalid restaurant reference. Please try again.",
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(dialog,
                        "Error adding review: " + errorMsg,
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Rating:"), gbc);
        gbc.gridx = 1;
        panel.add(ratingBox, gbc);
    
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Comment:"), gbc);
        gbc.gridx = 1;
        panel.add(new JScrollPane(commentArea), gbc);
    
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        panel.add(submitBtn, gbc);

    
        dialog.add(panel);
        dialog.setVisible(true);
    }
}
