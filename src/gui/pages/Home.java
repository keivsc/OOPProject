package gui.pages;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import gui.MainPanel;
import server.Data;
import server.Post;
import server.Posts;
import server.Users;

public class Home extends JPanel {
    private int page = 1;

    public Home(JFrame parentFrame, Data data) {
        setLayout(new BorderLayout());
        setBackground(Color.GRAY);

        JButton searchButton = new JButton("⌕");
        searchButton.setPreferredSize(new Dimension(25, 25)); // Adjusted size for better visibility
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                parentFrame.setContentPane(new Search(parentFrame, data));
                parentFrame.revalidate();
                parentFrame.repaint();
            }
        });

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.GRAY);
        topPanel.add(searchButton, BorderLayout.EAST);

        JLabel label = new JLabel("Browse Top Posts", JLabel.CENTER);
        label.setVerticalAlignment(JLabel.TOP);
        label.setForeground(Color.WHITE);
        topPanel.add(label, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);

        JPanel postsContainer = new JPanel();
        postsContainer.setLayout(new BoxLayout(postsContainer, BoxLayout.Y_AXIS));
        postsContainer.setBackground(Color.GRAY);

        List<Post> topPosts = data.Posts().browseTopPosts(page);
        for (Post post : topPosts) {
            JPanel newPostPanel = new JPanel();
            newPostPanel.setLayout(new BorderLayout());
            newPostPanel.setBorder(BorderFactory.createLineBorder(Color.WHITE));
            newPostPanel.setBackground(Color.DARK_GRAY);

            // Set fixed size for the panel
            Dimension panelSize = new Dimension(400, 100);
            newPostPanel.setPreferredSize(panelSize);
            newPostPanel.setMinimumSize(panelSize);
            newPostPanel.setMaximumSize(panelSize);

            JPanel titleAuthorPanel = new JPanel();
            titleAuthorPanel.setLayout(new BorderLayout());
            titleAuthorPanel.setBackground(Color.DARK_GRAY);

            JLabel titleLabel = new JLabel(post.getPostTitle());
            titleLabel.setForeground(Color.WHITE);
            titleAuthorPanel.add(titleLabel, BorderLayout.NORTH);

            JLabel authorLabel = new JLabel(data.Users().getUsername(post.getPostAuthor()));
            authorLabel.setForeground(Color.LIGHT_GRAY);
            titleAuthorPanel.add(authorLabel, BorderLayout.CENTER);

            newPostPanel.add(titleAuthorPanel, BorderLayout.CENTER);

            JPanel rightPanel = new JPanel();
            rightPanel.setLayout(new BorderLayout());
            rightPanel.setBackground(Color.DARK_GRAY);

            JLabel dateLabel = new JLabel(post.getPostDate());
            dateLabel.setForeground(Color.LIGHT_GRAY);
            rightPanel.add(dateLabel, BorderLayout.NORTH);

            JLabel likesDislikesLabel = new JLabel("Likes: "+post.getLikes() + " | Dislikes: " + post.getDislikes());
            likesDislikesLabel.setForeground(Color.LIGHT_GRAY);
            rightPanel.add(likesDislikesLabel, BorderLayout.SOUTH);

            newPostPanel.add(rightPanel, BorderLayout.EAST);

            newPostPanel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    parentFrame.setContentPane(new MainPanel(parentFrame, data, new PostViewer(parentFrame, data, post))); // Assuming Home is the previous screen
                    parentFrame.revalidate();
                    parentFrame.repaint();
                };
            });
            postsContainer.add(newPostPanel);
        }

        JScrollPane scrollPane = new JScrollPane(postsContainer);
        add(scrollPane, BorderLayout.CENTER);


        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Data data = new Data(new Users(), new Posts());
            JFrame frame = new JFrame("Home");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(600, 400);
            frame.setLocationRelativeTo(null);
            frame.setContentPane(new Home(frame, data));
            frame.setVisible(true);
        });
    }
}