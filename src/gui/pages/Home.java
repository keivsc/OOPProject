package gui.pages;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import gui.MainPanel;
import server.Comments;
import server.Data;
import server.types.Post;
import server.Posts;
import server.Users;

public class Home extends JPanel {
    private int currentPage = 1;
    private final JPanel postsContainer;
    private final JButton prevButton;
    private final JButton nextButton;
    private final JLabel nextLabel;
    private List<Post> topPosts;
    private Data data;

    public Home(JFrame parentFrame, Data data) {
        setName("Home");
        this.data = data;
        this.topPosts = this.data.Posts().browseTopPosts(currentPage);
        setLayout(new BorderLayout());
        setBackground(Color.GRAY);

        // Create top panel
        JPanel topPanel = createTopPanel(parentFrame, data);
        add(topPanel, BorderLayout.NORTH); // Ensure topPanel is added

        // Create posts container
        postsContainer = new JPanel(new GridBagLayout());
        postsContainer.setBackground(Color.GRAY);

        JPanel containerPanel = new JPanel(new BorderLayout());
        containerPanel.setBackground(Color.GRAY);
        containerPanel.add(postsContainer, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(containerPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBackground(Color.GRAY);
        scrollPane.setBorder(BorderFactory.createEmptyBorder()); // Remove the white border
        add(scrollPane, BorderLayout.CENTER);

        // Create pagination panel
        JPanel paginationPanel = new JPanel();
        paginationPanel.setBackground(Color.DARK_GRAY);

        prevButton = new JButton("Previous");
        prevButton.setEnabled(false);
        prevButton.addActionListener((ActionEvent e) -> navigateResults(parentFrame, -1));
        paginationPanel.add(prevButton);

        nextLabel = new JLabel("Page " + (currentPage ));
        nextLabel.setForeground(Color.WHITE);
        paginationPanel.add(nextLabel);

        nextButton = new JButton("Next");
        nextButton.setEnabled(false);
        nextButton.addActionListener((ActionEvent e) -> navigateResults(parentFrame, 1));
        paginationPanel.add(nextButton);

        add(paginationPanel, BorderLayout.SOUTH);

        // Display initial posts
        displayPosts(parentFrame);
    }

    private JPanel createTopPanel(JFrame parentFrame, Data data) {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.DARK_GRAY);

        // Create search button
        JButton searchButton = createSearchButton(parentFrame, data);
        topPanel.add(searchButton, BorderLayout.EAST);

        // Create title label
        JLabel titleLabel = new JLabel("Browse Top Posts", JLabel.CENTER);
        titleLabel.setVerticalAlignment(JLabel.TOP);
        titleLabel.setForeground(Color.WHITE);
        topPanel.add(titleLabel, BorderLayout.CENTER);

        return topPanel;
    }


    private JButton createSearchButton(JFrame parentFrame, Data data) {
        Supplier<JPanel> searchPanelSupplier = () ->  new Search(parentFrame, data);
        JButton searchButton = new JButton("⌕");
        searchButton.setPreferredSize(new Dimension(50, 50));
        searchButton.setFont(new Font("Arial", Font.PLAIN, 30));
        searchButton.addActionListener((ActionEvent e) -> {
            parentFrame.setContentPane(new MainPanel(parentFrame, data, searchPanelSupplier.get()));
            parentFrame.revalidate();
            parentFrame.repaint();
        });
        return searchButton;
    }

    private void navigateResults(JFrame parentFrame, int direction) {
        currentPage += direction;
        topPosts = this.data.Posts().browseTopPosts(currentPage);
        displayPosts(parentFrame);
        updateNavigationButtons();
    }

    private void displayPosts(JFrame parentFrame) {
        postsContainer.removeAll();
        postsContainer.setLayout(new GridBagLayout());  // Ensure GridBagLayout is used

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;  // Start at the top
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.FIRST_LINE_START;  // Ensure alignment to the top

        int displayLimit = 10;
        for (int i = 0; i < Math.min(topPosts.size(), displayLimit); i++) {
            gbc.gridy++;  // Move to the next row for the next post panel
            Post post = topPosts.get(i);

            JPanel newPostPanel = new JPanel(new BorderLayout());
            newPostPanel.setBorder(BorderFactory.createLineBorder(Color.WHITE));
            newPostPanel.setBackground(Color.DARK_GRAY);

            JPanel titleAuthorPanel = new JPanel(new BorderLayout());
            titleAuthorPanel.setBackground(Color.DARK_GRAY);

            JLabel titleLabel = new JLabel(post.getPostTitle());
            titleLabel.setForeground(Color.WHITE);
            titleAuthorPanel.add(titleLabel, BorderLayout.NORTH);

            JLabel authorLabel = new JLabel("Written by " + data.Users().getUsername(post.getPostAuthor()));
            authorLabel.setForeground(Color.LIGHT_GRAY);
            titleAuthorPanel.add(authorLabel, BorderLayout.CENTER);

            newPostPanel.add(titleAuthorPanel, BorderLayout.CENTER);

            JPanel rightPanel = new JPanel(new GridBagLayout());
            rightPanel.setBackground(Color.DARK_GRAY);

            GridBagConstraints rightGbc = new GridBagConstraints();
            rightGbc.insets = new Insets(0, 0, 0, 0);
            rightGbc.gridx = 0;
            rightGbc.gridy = 0;
            rightGbc.anchor = GridBagConstraints.NORTHEAST;
            rightGbc.weightx = 1.0;
            rightGbc.weighty = 1.0;
            rightGbc.fill = GridBagConstraints.NONE;

            JLabel dateLabel = new JLabel(post.getPostDate());
            dateLabel.setForeground(Color.LIGHT_GRAY);
            rightPanel.add(dateLabel, rightGbc);

            rightGbc.gridy = 1;
            rightGbc.anchor = GridBagConstraints.SOUTHEAST;
            rightGbc.weighty = 0.0;
            JLabel likesDislikesLabel = new JLabel("Likes: " + post.getLikes() + " | Dislikes: " + post.getDislikes());
            likesDislikesLabel.setForeground(Color.LIGHT_GRAY);
            rightPanel.add(likesDislikesLabel, rightGbc);

            Supplier<JPanel> postViewerSupplier = () -> new PostViewer(parentFrame, data, post);
            newPostPanel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    parentFrame.setContentPane(new MainPanel(parentFrame, data, postViewerSupplier.get()));
                    parentFrame.revalidate();
                    parentFrame.repaint();
                }
            });

            newPostPanel.add(rightPanel, BorderLayout.EAST);
            postsContainer.add(newPostPanel, gbc);
        }

        postsContainer.revalidate();
        postsContainer.repaint();
    }

    private void updateNavigationButtons() {
        prevButton.setEnabled(currentPage > 1);
        nextButton.setEnabled(topPosts.size() == 11);// Check if there is more than 10 results
        nextLabel.setText("Page " + (currentPage));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Data data = new Data(new Users(), new Posts(), new Comments());
            JFrame frame = new JFrame("Home");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(600, 400);
            frame.setLocationRelativeTo(null);
            frame.setContentPane(new Home(frame, data));
            frame.setVisible(true);
        });
    }
}