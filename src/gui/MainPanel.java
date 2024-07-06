package gui;

import gui.pages.Home;
import gui.pages.Login;
import server.*;

import javax.swing.*;
import java.awt.*;

public class MainPanel extends JPanel {

    private boolean isCollapsed = true;

    public MainPanel(JFrame parentFrame, Data data, JPanel displayPanel) {
        setLayout(new BorderLayout());
        User user = data.getUser();
        // Create the top-left container panel
        JPanel topLeftContainerPanel = new JPanel(new BorderLayout());
        topLeftContainerPanel.setPreferredSize(new Dimension(parentFrame.getWidth(), 30));
        topLeftContainerPanel.setBackground(Color.GRAY);

        // Create the top-left dark gray panel
        JPanel topLeftDarkGrayPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topLeftDarkGrayPanel.setPreferredSize(new Dimension(100, 30));
        topLeftDarkGrayPanel.setBackground(Color.GRAY);
        topLeftContainerPanel.add(topLeftDarkGrayPanel, BorderLayout.WEST);

        // Create the toggle button
        JButton toggleButton = new JButton(">>");
        toggleButton.setPreferredSize(new Dimension(50, 25));


        // Add the top-left container panel to the frame
        add(topLeftContainerPanel, BorderLayout.NORTH);

        // Create the navigation panel
        JPanel navigationPanel = new JPanel(new GridBagLayout());
        navigationPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        navigationPanel.setPreferredSize(new Dimension(100, getHeight()));
        navigationPanel.setBackground(Color.DARK_GRAY);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 0, 5, 0);
        gbc.gridx = 0;
        gbc.gridy = 0;

        // Create buttons
        JButton homeButton = new JButton("Home");
        JButton searchButton = new JButton("Search");
        JButton profileButton = new JButton("Profile");
        JButton logButton;
        JButton newPostButton = null;

        // Log button logic
        if (user == null) {
            logButton = new JButton("Login");
            logButton.addActionListener(e -> {
                parentFrame.setContentPane(new Login(parentFrame, data));
                parentFrame.revalidate();
                parentFrame.repaint();
            });
        } else {
            if(user.isAdmin){
                newPostButton = new JButton("New Post");
            }
            logButton = new JButton("Logout");
            logButton.addActionListener(e -> {
                int response = JOptionPane.showConfirmDialog(null, "Are you sure you want to logout of " + data.getUser().getUsername() + "?", "Logout?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (response == JOptionPane.YES_OPTION) {
                    parentFrame.setContentPane(new Login(parentFrame, data));
                    parentFrame.revalidate();
                    parentFrame.repaint();
                }
            });
        }

        // Add action listeners to buttons
        homeButton.addActionListener(e -> {
            parentFrame.setContentPane(new MainPanel(parentFrame, data, new Home(parentFrame, data)));
            parentFrame.revalidate();
            parentFrame.repaint();
        });

        // Add buttons to navigation panel
        gbc.gridy = 2;
        navigationPanel.add(homeButton, gbc);
        gbc.gridy++;
        navigationPanel.add(searchButton, gbc);
        if(user!=null&&user.isAdmin){
            gbc.gridy++;
            assert newPostButton != null;
            navigationPanel.add(newPostButton, gbc);
        }
        gbc.gridy++;
        navigationPanel.add(profileButton, gbc);
        gbc.gridy++;
        navigationPanel.add(logButton, gbc);

        // Add navigation and content panels to the main panel
        add(navigationPanel, BorderLayout.WEST);

        JPanel contentPanel = new JPanel(new CardLayout());
        contentPanel.add(displayPanel);
        add(contentPanel, BorderLayout.CENTER);
        toggleButton.addActionListener(e -> toggleNavigationPanel(navigationPanel, toggleButton, topLeftDarkGrayPanel));
        topLeftDarkGrayPanel.add(toggleButton);
        navigationPanel.setVisible(false);

        // Make sure the main panel gets the focus for keyboard events
        setFocusable(true);
        requestFocusInWindow();
    }

    private void toggleNavigationPanel(JPanel navPanel, JButton toggleButton, JPanel topLeftPanel) {
        if (isCollapsed) {
            toggleButton.setText("<<");
            topLeftPanel.setBackground(Color.DARK_GRAY);
            isCollapsed = false;
        } else {
            toggleButton.setText(">>");
            topLeftPanel.setBackground(Color.GRAY);
            isCollapsed = true;
        }
        navPanel.setVisible(!isCollapsed);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Data data = new Data(new Users(), new Posts());
            JFrame frame = new JFrame("Climate Action Program");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(600, 400);
            frame.setLocationRelativeTo(null);
            frame.setContentPane(new MainPanel(frame, data, new JPanel() {{
                setBackground(Color.GRAY);
                add(new JLabel("MainPanel") {{
                    setForeground(Color.WHITE);
                }});
            }}));
            frame.setVisible(true);
        });
    }
}