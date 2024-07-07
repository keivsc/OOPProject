package gui.pages;

import gui.MainPanel;
import server.Comments;
import server.Data;
import server.Posts;
import server.Users;
import server.types.Post;
import server.types.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.Supplier;

public class Profile extends JPanel {
    private final Data data;
    private final User user;
    private final JPanel postsContainer;
    private final JButton prevButton;
    private final JButton nextButton;
    private final JLabel nextLabel;
    private int currentPage = 1;
    private List<Integer> userPosts;
    private final int POSTS_PER_PAGE = 5;

    public Profile(JFrame parentFrame, Data data) {
        this.data = data;
        this.user = data.getUser();
        setName("Profile");
        setLayout(new BorderLayout());
        setBackground(Color.GRAY);

        JPanel infoPanel = createInfoPanel();
        add(infoPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BorderLayout());
        centerPanel.setBackground(Color.GRAY);

        postsContainer = new JPanel(new GridBagLayout());
        postsContainer.setBackground(Color.GRAY);

        JScrollPane scrollPane = new JScrollPane(postsContainer);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16); // Speed up mouse wheel scrolling
        scrollPane.setBorder(null); // Remove the border
        centerPanel.add(scrollPane, BorderLayout.NORTH);

        add(centerPanel, BorderLayout.CENTER);

        // Create pagination panel
        JPanel paginationPanel = new JPanel();
        paginationPanel.setBackground(Color.DARK_GRAY);

        prevButton = new JButton("Previous");
        prevButton.setEnabled(false);
        prevButton.addActionListener((ActionEvent e) -> navigateResults(parentFrame, -1));
        paginationPanel.add(prevButton);

        nextLabel = new JLabel("Page " + currentPage);
        nextLabel.setForeground(Color.WHITE);
        paginationPanel.add(nextLabel);

        nextButton = new JButton("Next");
        nextButton.setEnabled(false);
        nextButton.addActionListener((ActionEvent e) -> navigateResults(parentFrame, 1));
        paginationPanel.add(nextButton);
        paginationPanel.setVisible(false);

        add(paginationPanel, BorderLayout.SOUTH);

        displayUserPosts(parentFrame);
    }

    private JPanel createInfoPanel() {
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(Color.DARK_GRAY);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel usernameLabel = new JLabel("Username: " + user.getUsername());
        usernameLabel.setForeground(Color.WHITE);
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 16));

        JLabel emailLabel = new JLabel("Email: " + user.getEmail());
        emailLabel.setForeground(Color.WHITE);
        emailLabel.setFont(new Font("Arial", Font.BOLD, 16));

        JLabel adminLabel = new JLabel("Admin: " + user.isAdmin);
        adminLabel.setForeground(Color.WHITE);
        adminLabel.setFont(new Font("Arial", Font.BOLD, 16));

        infoPanel.add(usernameLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Add space between labels
        infoPanel.add(emailLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Add space between labels
        infoPanel.add(adminLabel);

        JButton changePasswordButton = createChangePasswordButton();
        infoPanel.add(changePasswordButton);

        return infoPanel;
    }

    private JButton createChangePasswordButton() {
        JButton changePasswordButton = new JButton("Change Password");
        changePasswordButton.setFont(new Font("Arial", Font.BOLD, 14));
        changePasswordButton.addActionListener(e -> changePassword());

        return changePasswordButton;
    }

    private void changePassword() {
        // Create a panel to hold the input fields
        JPanel panel = new JPanel(new GridLayout(2, 2));

        // Create labels and password fields
        JLabel oldPasswordLabel = new JLabel("Old Password:");
        JPasswordField oldPasswordField = new JPasswordField();
        JLabel newPasswordLabel = new JLabel("New Password:");
        JPasswordField newPasswordField = new JPasswordField();

        // Add components to the panel
        panel.add(oldPasswordLabel);
        panel.add(oldPasswordField);
        panel.add(newPasswordLabel);
        panel.add(newPasswordField);

        // Show the JOptionPane with the custom panel
        int result = JOptionPane.showConfirmDialog(this, panel, "Change Password", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        // Check if the user pressed OK
        if (result == JOptionPane.OK_OPTION) {
            // Get the password inputs
            String oldPassword = new String(oldPasswordField.getPassword());
            String newPassword = new String(newPasswordField.getPassword());

            int rs = this.data.Users().changePassword(this.user.getEmail(), oldPassword, newPassword);
            if (rs == 0) {
                JOptionPane.showMessageDialog(this, "Password changed successfully.");
            }else{
                JOptionPane.showMessageDialog(this, "Wrong password!");
            }
        }
    }

    private void navigateResults(JFrame parentFrame, int direction) {
        currentPage += direction;
        displayUserPosts(parentFrame);
        updateNavigationButtons();
    }

    private void displayUserPosts(JFrame parentFrame) {
        postsContainer.removeAll();
        postsContainer.setLayout(new GridBagLayout());  // Ensure GridBagLayout is used

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;  // Start at the top
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.FIRST_LINE_START;  // Ensure alignment to the top

        userPosts = data.Users().getUserPosts(user.getId());

        if (!userPosts.isEmpty()) {
            JLabel postText = new JLabel("Your Posts"){{setFont(new Font("Arial", Font.BOLD, 15));}};
            postText.setForeground(Color.WHITE);
            postsContainer.add(postText);
            int totalPages = (int) Math.ceil((double) userPosts.size() / POSTS_PER_PAGE);
            nextLabel.setText("Page " + currentPage + " of " + totalPages);
            nextLabel.getParent().setVisible(true);

            for (int i = 0; i < Math.min(userPosts.size(), POSTS_PER_PAGE); i++) {
                gbc.gridy++;  // Move to the next row for the next post panel
                int postID = userPosts.get(i + (currentPage - 1) * POSTS_PER_PAGE);

                if (postID > 0) { // Only show posts with postID greater than 1
                    Post post = data.Posts().getPost(postID);
                    if (post != null) {
                        JPanel postPanel = createPostPanel(parentFrame, data, post);
                        postsContainer.add(postPanel, gbc);
                    }
                }
            }

            postsContainer.revalidate();
            postsContainer.repaint();
            updateNavigationButtons();
        }
    }

    private JPanel createPostPanel(JFrame parentFrame, Data data, Post post) {
        JPanel postPanel = new JPanel(new BorderLayout());
        postPanel.setBorder(BorderFactory.createLineBorder(Color.WHITE));
        postPanel.setBackground(Color.DARK_GRAY);
        postPanel.setPreferredSize(new Dimension(400, 100));

        // Create title and author panel
        JPanel titleAuthorPanel = new JPanel(new BorderLayout());
        titleAuthorPanel.setBackground(Color.DARK_GRAY);
        JLabel titleLabel = new JLabel(post.getPostTitle());
        titleLabel.setForeground(Color.WHITE);
        JLabel authorLabel = new JLabel(data.Users().getUsername(post.getPostAuthor()));
        authorLabel.setForeground(Color.LIGHT_GRAY);
        titleAuthorPanel.add(titleLabel, BorderLayout.NORTH);
        titleAuthorPanel.add(authorLabel, BorderLayout.CENTER);
        postPanel.add(titleAuthorPanel, BorderLayout.CENTER);

        // Create right panel for date and likes/dislikes
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(Color.DARK_GRAY);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.NORTHEAST;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.NONE;

        JLabel dateLabel = new JLabel(post.getPostDate());
        dateLabel.setForeground(Color.LIGHT_GRAY);
        rightPanel.add(dateLabel, gbc);

        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.SOUTHEAST;
        gbc.weighty = 0.0;
        JLabel likesDislikesLabel = new JLabel("Likes: " + post.getLikes() + " | Dislikes: " + post.getDislikes());
        likesDislikesLabel.setForeground(Color.LIGHT_GRAY);
        rightPanel.add(likesDislikesLabel, gbc);

        postPanel.add(rightPanel, BorderLayout.EAST);

        // Add mouse listener for clicking on post
        Supplier<JPanel> postViewerSupplier = () -> new PostViewer(parentFrame, data, post);
        postPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                parentFrame.setContentPane(new MainPanel(parentFrame, data, postViewerSupplier.get()));
                parentFrame.revalidate();
                parentFrame.repaint();
            }
        });

        return postPanel;
    }

    private void updateNavigationButtons() {
        prevButton.setEnabled(currentPage > 1);
        nextButton.setEnabled(userPosts.size() > currentPage * POSTS_PER_PAGE);
        int totalPages = (int) Math.ceil((double) userPosts.size() / POSTS_PER_PAGE);
        nextLabel.setText("Page " + currentPage + " of " + totalPages);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Data data = new Data(new Users(), new Posts(), new Comments());
            JFrame frame = new JFrame("Profile");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(600, 400);
            frame.setLocationRelativeTo(null);
            frame.setContentPane(new Profile(frame, data));
            frame.setVisible(true);
        });
    }
}