package gui.pages;

import gui.MainPanel;
import server.Comments;
import server.Data;
import server.Posts;
import server.Users;
import server.types.Comment;
import server.types.Post;
import server.types.User;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.Instant;
import java.util.List;
import java.util.function.Supplier;

public class PostViewer extends JPanel {
    private final Data data;
    private final Post post;
    private final JPanel commentsPanel = new JPanel();
    private final User user;
    private boolean isLiked;
    private boolean isDisliked;
    private int currentPage = 1;
    private final int COMMENTS_PER_PAGE = 5;
    private final List<Comment> comments;

    public PostViewer(JFrame parentFrame, Data data, Post post) {
        this.data = data;
        this.post = data.Posts().getPost(post.getPostId());
        this.user = data.getUser();
        this.comments = data.Comments().getCommentsFromPost(post.getPostId());
        checkLiked();
        setName("Post Viewer");
        setLayout(new BorderLayout());
        setBackground(Color.GRAY);

        JPanel mainContentPanel = createMainContentPanel();
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.DARK_GRAY);
        topPanel.add(createInfoPanel(), BorderLayout.CENTER);
        topPanel.add(createTopLeftPanel(parentFrame), BorderLayout.WEST);
        topPanel.add(createButtonPanel("▲", "like", "▼", "dislike"), BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);
        add(mainContentPanel, BorderLayout.CENTER);
    }

    private JPanel createTopLeftPanel(JFrame parentFrame) {
        JPanel topLeftPanel = new JPanel(new BorderLayout());
        topLeftPanel.setBackground(Color.DARK_GRAY);

        JButton backButton = new JButton("Back");
        backButton.setFont(new Font("Arial", Font.BOLD, 14));
        backButton.setPreferredSize(new Dimension(100, 30)); // Set preferred size for width and height
        Supplier<JPanel> homePanelSupplier = () -> new Home(parentFrame, data);
        backButton.addActionListener(e -> {
            parentFrame.setContentPane(new MainPanel(parentFrame, data, homePanelSupplier.get()));
            parentFrame.revalidate();
            parentFrame.repaint();
        });

        topLeftPanel.add(backButton, BorderLayout.CENTER);
        return topLeftPanel;
    }

    private JPanel createButtonPanel(String likeText, String likeCommand, String dislikeText, String dislikeCommand) {
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        buttonPanel.setBackground(Color.DARK_GRAY);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;

        JLabel likeLabel = createLikeDislikeLabel(likeText, likeCommand, Color.GREEN);
        JLabel dislikeLabel = createLikeDislikeLabel(dislikeText, dislikeCommand, Color.RED);

        buttonPanel.add(likeLabel, gbc);
        buttonPanel.add(dislikeLabel, gbc);
        return buttonPanel;
    }

    private JLabel createLikeDislikeLabel(String text, String command, Color activeColor) {
        JLabel label = new JLabel(text, JLabel.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 20));
        label.setForeground(Color.WHITE);
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (user == null) {
                    JOptionPane.showMessageDialog(null, "You must be Logged in to " + command + "!", "Not Logged in!", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                data.Posts().resetLikes(post.getPostId(), user.getId());
                if ((command.equals("like") && isLiked) || (command.equals("dislike") && isDisliked)) {
                    if (command.equals("like")) isLiked = false;
                    else isDisliked = false;
                    label.setForeground(Color.WHITE);
                } else {
                    if (command.equals("like")) {
                        isLiked = true;
                        data.Posts().likePost(post.getPostId(), user.getId());
                        if (isDisliked) {
                            isDisliked = false;
                            label.setForeground(Color.WHITE);
                        }
                    } else {
                        isDisliked = true;
                        data.Posts().dislikePost(post.getPostId(), user.getId());
                        if (isLiked) {
                            isLiked = false;
                            label.setForeground(Color.WHITE);
                        }
                    }
                    label.setForeground(activeColor);
                }
                updateLikeDislikeLabels();
            }
        });
        return label;
    }
    private void checkLiked() {
        if (this.data.getUser() == null) return;
        if (this.post.getLikedUsers().contains(user.getId())) {
            isLiked = true;
            isDisliked = false;
        } else if (this.post.getDislikedUsers().contains(user.getId())) {
            isLiked = false;
            isDisliked = true;
        }
    }

    private void updateLikeDislikeLabels() {
        revalidate();
        repaint();
    }

    private JPanel createMainContentPanel() {
        JPanel mainContentPanel = new JPanel(new GridBagLayout());
        mainContentPanel.setBackground(Color.GRAY);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        mainContentPanel.add(createTitledPanel("Content", createContentPanel(), Color.BLACK), gbc);

        gbc.gridy++;
        mainContentPanel.add(createTitledPanel("Comments", createCommentsPanelWithLabel(), Color.WHITE), gbc);

        gbc.gridy++;
        gbc.weighty = 0.0;
        mainContentPanel.add(createTitledPanel("Add Comment", createCommentInputPanel(data.getUser()), Color.WHITE), gbc);

        return mainContentPanel;
    }

    private JPanel createTitledPanel(String title, JPanel panel, Color borderColor) {
        panel.setBorder(createTitledBorder(borderColor, title));
        return panel;
    }

    private TitledBorder createTitledBorder(Color color, String title) {
        TitledBorder titledBorder = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(color), title);
        titledBorder.setTitleColor(color);
        return titledBorder;
    }

    private JPanel createCommentsPanelWithLabel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.GRAY);
        commentsPanel.setLayout(new BoxLayout(commentsPanel, BoxLayout.Y_AXIS));
        commentsPanel.setBackground(Color.GRAY);
        updateCommentsPanel();
        panel.add(commentsPanel, BorderLayout.CENTER);
        return panel;
    }


    private void updateCommentsPanel() {
        commentsPanel.removeAll();
        int start = (currentPage - 1) * COMMENTS_PER_PAGE;
        int end = Math.min(start + COMMENTS_PER_PAGE, comments.size());

        for (int i = start; i < end; i++) {
            Comment comment = comments.get(i);
            commentsPanel.add(createCommentPanel(comment));
        }

        commentsPanel.add(createPageSelector());
        commentsPanel.revalidate();
        commentsPanel.repaint();
    }

    private JPanel createPageSelector() {
        JPanel pageSelectorPanel = new JPanel();
        pageSelectorPanel.setBackground(Color.GRAY);

        int totalPages = (int) Math.ceil((double) comments.size() / COMMENTS_PER_PAGE);
        if (totalPages == 0) {
            JLabel noCommentsLabel = new JLabel("No Comments.");
            noCommentsLabel.setForeground(Color.WHITE);
            pageSelectorPanel.add(noCommentsLabel);
            return pageSelectorPanel;
        }

        JButton prevButton = new JButton("Previous");
        prevButton.setEnabled(currentPage > 1);
        prevButton.addActionListener(e -> {
            if (currentPage > 1) {
                currentPage--;
                updateCommentsPanel();
            }
        });
        pageSelectorPanel.add(prevButton);

        JLabel pageLabel = new JLabel("Page " + currentPage + " of " + totalPages);
        pageLabel.setForeground(Color.WHITE);
        pageSelectorPanel.add(pageLabel);

        JButton nextButton = new JButton("Next");
        nextButton.setEnabled(currentPage < totalPages);
        nextButton.addActionListener(e -> {
            if (currentPage < totalPages) {
                currentPage++;
                updateCommentsPanel();
            }
        });
        pageSelectorPanel.add(nextButton);

        return pageSelectorPanel;
    }

    private JPanel createInfoPanel() {
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(Color.DARK_GRAY);

        JLabel titleLabel = new JLabel(post.getPostTitle());
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);

        String author = data.Users().getUsername(post.getPostAuthor());
        JLabel authorLabel = new JLabel("Author: " + author);
        authorLabel.setFont(new Font("Arial", Font.BOLD, 14));
        authorLabel.setForeground(Color.WHITE);

        JLabel date = new JLabel("Posted " + post.getPostDate());
        date.setFont(new Font("Arial", Font.BOLD, 14));
        date.setForeground(Color.WHITE);

        infoPanel.add(titleLabel);
        infoPanel.add(authorLabel);
        infoPanel.add(date);

        return infoPanel;
    }

    private JPanel createContentPanel() {
        JTextArea content = new JTextArea(post.getPostContent());
        content.setFont(new Font("Arial", Font.PLAIN, 16));
        content.setLineWrap(true);
        content.setWrapStyleWord(true);
        content.setEditable(false);
        content.setBackground(Color.GRAY);
        content.setForeground(Color.WHITE);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(content, BorderLayout.CENTER);

        return contentPanel;
    }

    private JPanel createCommentInputPanel(User user) {
        JPanel commentInputPanel = new JPanel();
        commentInputPanel.setLayout(new BoxLayout(commentInputPanel, BoxLayout.Y_AXIS));
        commentInputPanel.setBackground(Color.GRAY);
        commentInputPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        if (user == null) {
            commentInputPanel.add(createLabel("You need to be logged in to post a comment!"));
            return commentInputPanel;
        }
        JTextArea commentInputText = new JTextArea(5, 30);
        commentInputText.setFont(new Font("Arial", Font.PLAIN, 16));
        commentInputText.setLineWrap(true);
        commentInputText.setWrapStyleWord(true);
        commentInputText.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        JScrollPane scrollPane = new JScrollPane(commentInputText);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        commentInputPanel.add(scrollPane);

        JButton submitButton = new JButton("Submit");
        submitButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        submitButton.addActionListener(e -> submitComment(commentInputText, commentInputPanel));
        commentInputPanel.add(submitButton);

        return commentInputPanel;
    }

    private void submitComment(JTextArea commentInputText, JPanel commentInputPanel) {
        String commentText = commentInputText.getText();
        if (commentText.trim().isEmpty()) {
            JOptionPane.showMessageDialog(commentInputPanel, "Comment cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int rs = data.Comments().addComment(post.getPostId(), commentText, data.getUser().getId());
        if (rs == 0) {
            commentInputText.setText("");
            Comment newComment = new Comment(data.Comments().getLastID(post.getPostId()) + 1, commentText, data.getUser().getId(), (int) (Instant.now().getEpochSecond()));
            comments.add(newComment);
            currentPage = (int) Math.ceil((double) comments.size() / COMMENTS_PER_PAGE);
            updateCommentsPanel();
        } else {
            JOptionPane.showMessageDialog(null, "Comment was not submitted", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createCommentPanel(Comment comment) {
        JPanel commentPanel = new JPanel();
        commentPanel.setLayout(new BorderLayout());
        commentPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        commentPanel.setBackground(Color.GRAY);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.GRAY);

        JLabel commentAuthorLabel = new JLabel("Author: " + data.Users().getUsername(comment.getAuthorID()));
        commentAuthorLabel.setForeground(Color.WHITE);
        topPanel.add(commentAuthorLabel, BorderLayout.WEST);

        JLabel commentDateLabel = new JLabel(comment.getPostDate());
        commentDateLabel.setForeground(Color.WHITE);
        topPanel.add(commentDateLabel, BorderLayout.EAST);

        commentPanel.add(topPanel, BorderLayout.NORTH);

        JTextArea commentContentArea = new JTextArea(comment.getComment());
        commentContentArea.setForeground(Color.WHITE);
        commentContentArea.setBackground(Color.GRAY);
        commentContentArea.setEditable(false);
        commentContentArea.setLineWrap(true);
        commentContentArea.setWrapStyleWord(true);
        commentPanel.add(commentContentArea, BorderLayout.CENTER);

        return commentPanel;
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        return label;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Data data = new Data(new Users(), new Posts(), new Comments());
            JFrame frame = new JFrame("Home");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(600, 400);
            frame.setLocationRelativeTo(null);
            frame.setContentPane(new PostViewer(frame, data, data.Posts().getPost(1)));
            frame.setVisible(true);
        });
    }
}