package gui.pages;
import gui.MainPanel;
import server.Data;
import server.Post;
import server.Posts;
import server.Users;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PostViewer extends JPanel {
    public PostViewer(JFrame parentFrame, Data data, Post post) {
        post = data.Posts().getPost(post.getPostId());
        setLayout(new BorderLayout());
        setBackground(Color.GRAY);

        JLabel title = new JLabel(post.getPostTitle());
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setForeground(Color.WHITE);

        JLabel author = new JLabel("Author: " + data.Users().getUsername(post.getPostAuthor()));
        author.setFont(new Font("Arial", Font.BOLD, 14));
        author.setForeground(Color.WHITE);

        JLabel date = new JLabel("Posted on: " + post.getPostDate());
        date.setFont(new Font("Arial", Font.BOLD, 14));
        date.setForeground(Color.WHITE);

        JTextArea content = new JTextArea(post.getPostContent());
        content.setFont(new Font("Arial", Font.PLAIN, 16));
        content.setLineWrap(true);
        content.setWrapStyleWord(true);
        content.setEditable(false);
        content.setBackground(Color.GRAY);
        content.setForeground(Color.WHITE);
        JScrollPane contentScrollPane = new JScrollPane(content);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(Color.GRAY);
        infoPanel.add(title);
        infoPanel.add(author);
        infoPanel.add(date);
        infoPanel.setBackground(Color.DARK_GRAY);

        add(infoPanel, BorderLayout.NORTH);
        add(contentScrollPane, BorderLayout.CENTER);
        JButton backButton = new JButton("Back");
        backButton.setFont(new Font("Arial", Font.BOLD, 14));
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                parentFrame.setContentPane(new MainPanel(parentFrame, data, new Home(parentFrame, data))); // Assuming Home is the previous screen
                parentFrame.revalidate();
                parentFrame.repaint();
            }
        });

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.GRAY);
        topPanel.add(infoPanel, BorderLayout.CENTER);
        topPanel.add(backButton, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);
        setVisible(true);
    }

    public static void main(String args[]){
        SwingUtilities.invokeLater(() -> {
            Data data = new Data(new Users(), new Posts());
            JFrame frame = new JFrame("Home");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(600, 400);
            frame.setLocationRelativeTo(null);
            frame.setContentPane(new PostViewer(frame, data, data.Posts().getPost(1)));
            frame.setVisible(true);
        });
    }
}
