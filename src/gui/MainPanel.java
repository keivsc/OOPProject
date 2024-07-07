package gui;

import gui.pages.*;
import server.Comments;
import server.Data;
import server.Posts;
import server.Users;
import server.types.User;

import javax.swing.*;
import java.awt.*;
import java.util.function.Supplier;

public class MainPanel extends JPanel {

    private boolean isCollapsed;

    public MainPanel(JFrame parentFrame, Data data, JPanel displayPanel) {
        this(parentFrame, data, displayPanel, true);
    }
    private void configureDisplayPanel(JPanel displayPanel) {
        displayPanel.setMinimumSize(new Dimension(800, 500));
        displayPanel.setPreferredSize(new Dimension(800, 500));
    }

    private void setupContentPanel(JPanel displayPanel) {
        JPanel contentPanel = new JPanel(new BorderLayout());
        configureDisplayPanel(displayPanel);

        contentPanel.add(displayPanel, BorderLayout.CENTER);

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16); // Speed up mouse wheel scrolling
        scrollPane.setBorder(null); // Remove the border
        add(scrollPane, BorderLayout.CENTER);
    }

    public MainPanel(JFrame parentFrame, Data data, JPanel displayPanel, boolean isCollapsed) {
        this.isCollapsed = isCollapsed;
        setLayout(new BorderLayout());
        User user = data.getUser();

        // Create top panel
        JPanel topPanel = createTopPanel(parentFrame);
        add(topPanel, BorderLayout.NORTH);

        // Create navigation panel
        JPanel navigationPanel = createNavigationPanel(parentFrame, data, user);
        navigationPanel.setVisible(!isCollapsed);
        add(navigationPanel, BorderLayout.WEST);

        // Setup content panel
        setupContentPanel(displayPanel);

        // Toggle button action listener
        JButton toggleButton = (JButton) ((JPanel) topPanel.getComponent(0)).getComponent(0);
        toggleButton.addActionListener(e -> toggleNavigationPanel(navigationPanel, toggleButton));

        // Make sure the main panel gets the focus for keyboard events
        setFocusable(true);
        requestFocusInWindow();
    }

    private JPanel createTopPanel(JFrame parentFrame) {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setPreferredSize(new Dimension(parentFrame.getWidth(), 30));
        topPanel.setBackground(Color.DARK_GRAY);

        // Left panel with toggle button
        JPanel topLeftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topLeftPanel.setBackground(Color.DARK_GRAY);
        topPanel.add(topLeftPanel, BorderLayout.WEST);

        JButton toggleButton = new JButton(isCollapsed ? ">>" : "<<");
        toggleButton.setPreferredSize(new Dimension(50, 25));
        topLeftPanel.add(toggleButton);

        // Right panel to balance the layout
        JPanel topRightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topRightPanel.setBackground(Color.DARK_GRAY);
        topPanel.add(topRightPanel, BorderLayout.EAST);

        return topPanel;
    }

    private JPanel createNavigationPanel(JFrame parentFrame, Data data, User user) {
        JPanel navigationPanel = new JPanel(new GridBagLayout());
        navigationPanel.setVisible(false);
        navigationPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        navigationPanel.setPreferredSize(new Dimension(100, getHeight()));
        navigationPanel.setBackground(Color.DARK_GRAY);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 0, 5, 0);
        gbc.gridx = 0;
        gbc.gridy = 0;

        // Create buttons
        JButton homeButton = createNavButton("Home", parentFrame, data, () -> new Home(parentFrame, data));
        JButton searchButton = createNavButton("Search", parentFrame, data, () -> new Search(parentFrame, data));

        JButton logButton = user == null ? createLoginButton(parentFrame, data) : createLogoutButton(parentFrame, data, user);

        // Add buttons to navigation panel
        navigationPanel.add(homeButton, gbc);
        gbc.gridy++;
        navigationPanel.add(searchButton, gbc);
        if (user != null && user.isAdmin) {
            JButton newPostButton = createNavButton("New Post", parentFrame, data, () -> new NewPost(parentFrame, data));
            gbc.gridy++;
            navigationPanel.add(newPostButton, gbc);
        }
        if (user != null) {
            JButton profileButton = createNavButton("Profile", parentFrame, data, () -> new Profile(parentFrame, data));
            gbc.gridy++;
            navigationPanel.add(profileButton, gbc);
        }
        gbc.gridy++;
        navigationPanel.add(logButton, gbc);

        return navigationPanel;
    }

    private JButton createNavButton(String text, JFrame parentFrame, Data data, Supplier<JPanel> panelSupplier) {
        JButton button = new JButton(text);
        button.addActionListener(e -> {
            JPanel panel = panelSupplier.get();
            parentFrame.setContentPane(new MainPanel(parentFrame, data, panel, isCollapsed));
            parentFrame.revalidate();
            parentFrame.repaint();
        });
        return button;
    }

    private JButton createLoginButton(JFrame parentFrame, Data data) {
        JButton button = new JButton("Login");
        button.addActionListener(e -> {
            parentFrame.setContentPane(new Login(parentFrame, data));
            parentFrame.revalidate();
            parentFrame.repaint();
        });
        return button;
    }

    private JButton createLogoutButton(JFrame parentFrame, Data data, User user) {
        JButton button = new JButton("Logout");
        button.addActionListener(e -> {
            int response = JOptionPane.showConfirmDialog(null, "Are you sure you want to logout of " + user.getUsername() + "?", "Logout?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (response == JOptionPane.YES_OPTION) {
                parentFrame.setContentPane(new Login(parentFrame, data));
                parentFrame.revalidate();
                parentFrame.repaint();
            }
        });
        return button;
    }

    private void toggleNavigationPanel(JPanel navPanel, JButton toggleButton) {
        isCollapsed = !isCollapsed;
        toggleButton.setText(isCollapsed ? ">>" : "<<");
        navPanel.setVisible(!isCollapsed);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Data data = new Data(new Users(), new Posts(), new Comments());
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