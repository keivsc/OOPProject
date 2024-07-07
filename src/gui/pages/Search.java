package gui.pages;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import gui.MainPanel;
import server.Comments;
import server.Data;
import server.Posts;
import server.Users;
import server.types.Post;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.Supplier;

public class Search extends JPanel {
    private final JTextField searchArea;
    private final JPanel resultsContainer;
    private final JButton prevButton;
    private final JButton nextButton;
    private final JLabel nextLabel;
    private int currentPage = 1;
    private List<Post> searchResults;
    private Data data;

    public Search(JFrame parentFrame, Data data) {
        this.data = data;
        setName("Search");
        setLayout(new BorderLayout());
        setBackground(Color.GRAY);

        // Create search button
        JButton searchButton = new JButton("⌕");
        searchButton.setPreferredSize(new Dimension(50, 50));
        searchButton.setFont(new Font("Arial", Font.PLAIN, 30));

        // Create back button
        JButton backButton = new JButton("Back");
        backButton.setFont(new Font("Arial", Font.BOLD, 14));
        Supplier<JPanel> homePanelSupplier = () -> new Home(parentFrame, data);
        backButton.addActionListener(e -> {
            parentFrame.setContentPane(new MainPanel(parentFrame, data, homePanelSupplier.get()));
            parentFrame.revalidate();
            parentFrame.repaint();
        });

        // Create top panel with search area and buttons
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.DARK_GRAY);

        searchArea = new JTextField();
        searchArea.setToolTipText("Enter search text");
        searchArea.setFont(new Font("Arial", Font.PLAIN, 16));
        searchArea.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        JScrollPane searchScrollPane = new JScrollPane(searchArea);
        searchScrollPane.setPreferredSize(new Dimension(400, 50));

        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.add(searchScrollPane, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);

        topPanel.add(backButton, BorderLayout.WEST);
        topPanel.add(searchPanel, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);

        SwingUtilities.invokeLater(searchArea::requestFocusInWindow);

        resultsContainer = new JPanel(new GridBagLayout());
        resultsContainer.setBackground(Color.GRAY);


        JPanel containerPanel = new JPanel(new BorderLayout());
        containerPanel.setBackground(Color.GRAY);
        containerPanel.add(resultsContainer, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(containerPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBackground(Color.gray);
        add(scrollPane, BorderLayout.CENTER);

        JPanel paginationPanel = new JPanel();
        paginationPanel.setBackground(Color.DARK_GRAY);

        prevButton = new JButton("Previous");
        prevButton.setEnabled(false);
        prevButton.addActionListener((ActionEvent e) -> navigateResults(parentFrame, -1));
        paginationPanel.add(prevButton);

        nextLabel = new JLabel("Page " + (currentPage));
        nextLabel.setForeground(Color.WHITE);
        paginationPanel.add(nextLabel);

        nextButton = new JButton("Next");
        nextButton.setEnabled(false);
        nextButton.addActionListener((ActionEvent e) -> navigateResults(parentFrame, 1));
        paginationPanel.add(nextButton);

        add(paginationPanel, BorderLayout.SOUTH);

        searchButton.addActionListener((ActionEvent e) -> performSearch(parentFrame));

        // Add DocumentListener to searchArea to detect text changes
        searchArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                performSearch(parentFrame);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                performSearch(parentFrame);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                performSearch(parentFrame);
            }
        });
    }

    private void performSearch(JFrame parentFrame) {
        String query = searchArea.getText().trim();
        if (query.isEmpty()) {
            resultsContainer.removeAll();
            resultsContainer.revalidate();
            resultsContainer.repaint();
            resetPaginationPanel();
            return;
        }
        currentPage = 1;
        searchResults = data.Posts().searchPosts(query, currentPage);
        displayResults(parentFrame);
        updateNavigationButtons();
    }

    private void navigateResults(JFrame parentFrame, int direction) {
        currentPage += direction;
        searchResults = data.Posts().searchPosts(searchArea.getText().trim(), currentPage);
        displayResults(parentFrame);
        updateNavigationButtons();
    }

    private void displayResults(JFrame parentFrame) {
        resultsContainer.removeAll();
        resultsContainer.setLayout(new GridBagLayout());  // Ensure GridBagLayout is used

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;  // Start at the top
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.FIRST_LINE_START;  // Ensure alignment to the top
        resultsContainer.add(new JLabel("Results"){{setForeground(Color.WHITE); setFont(new Font("Arial", Font.PLAIN, 20));}});
        int displayLimit = 10;
        for (int i = 0; i < Math.min(searchResults.size(), displayLimit); i++) {
            gbc.gridy++;  // Move to the next row for the next post panel
            Post post = searchResults.get(i);

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
            resultsContainer.add(newPostPanel, gbc);
        }

        resultsContainer.revalidate();
        resultsContainer.repaint();
    }

    private void updateNavigationButtons() {
        prevButton.setEnabled(currentPage > 1);
        nextButton.setEnabled(searchResults.size() == 11); // Check if there is more than 10 results
        nextLabel.setText("Page " + (currentPage));
    }

    private void resetPaginationPanel() {
        currentPage = 1;
        prevButton.setEnabled(false);
        nextButton.setEnabled(false);
        nextLabel.setText("Page 1");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Data data = new Data(new Users(), new Posts(), new Comments());
            JFrame frame = new JFrame("Search Page");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(600, 400);
            frame.setLocationRelativeTo(null);
            frame.setContentPane(new Search(frame, data));
            frame.setVisible(true);
        });
    }
}