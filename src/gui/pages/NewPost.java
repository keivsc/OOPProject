package gui.pages;

import gui.MainPanel;
import server.Comments;
import server.Data;
import server.Posts;
import server.Users;

import javax.swing.*;
import java.awt.*;
import java.util.function.Supplier;

public class NewPost extends JPanel {
    public NewPost(JFrame parentFrame, Data data){
        setName("New Post");
        setLayout(new BorderLayout());
        setBackground(Color.GRAY);

        // Title label
        JLabel pageTitle = new JLabel("New Post");
        pageTitle.setFont(new Font("Arial", Font.BOLD, 30));
        pageTitle.setForeground(Color.WHITE);
        pageTitle.setHorizontalAlignment(SwingConstants.CENTER);
        add(pageTitle, BorderLayout.NORTH);

        // Main panel for form elements
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.GRAY);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;

        // Post title label
        JLabel titleLabel = new JLabel("Post Title: ");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
        formPanel.add(titleLabel, gbc);

        // Post title text field
        gbc.gridx = 1;
        JTextField titleText = new JTextField(20);
        titleText.setFont(new Font("Arial", Font.PLAIN, 16));
        formPanel.add(titleText, gbc);

        // Post content label
        gbc.gridx = 0;
        gbc.gridy++;
        JLabel contentLabel = new JLabel("Content: ");
        contentLabel.setFont(new Font("Arial", Font.BOLD, 16));
        contentLabel.setForeground(Color.WHITE);
        formPanel.add(contentLabel, gbc);

        // Post content text area
        gbc.gridx = 1;
        JTextArea contentTextArea = new JTextArea(10, 20);
        contentTextArea.setFont(new Font("Arial", Font.PLAIN, 16));
        contentTextArea.setLineWrap(true);
        contentTextArea.setWrapStyleWord(true);
        JScrollPane contentScrollPane = new JScrollPane(contentTextArea);
        formPanel.add(contentScrollPane, gbc);

        // Add form panel to center
        add(formPanel, BorderLayout.CENTER);

        // Add a button panel with Submit button at the bottom
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.GRAY);
        JButton submitButton = new JButton("Submit");
        submitButton.setFont(new Font("Arial", Font.BOLD, 16));
        submitButton.addActionListener(e -> {
            String title = titleText.getText();
            String content = contentTextArea.getText();
            int result = data.Posts().newPost(data.getUser().getId(), title, content);
            if(result == 1){
                JOptionPane.showMessageDialog(parentFrame, "Post creation failed", "Error", JOptionPane.ERROR_MESSAGE);
            }else {
                JOptionPane.showMessageDialog(parentFrame, "Post submitted!\nTitle: " + title);
            }
            Supplier<JPanel> homePanelSupplier = () -> new Home(parentFrame, data);
            parentFrame.setContentPane(new MainPanel(parentFrame, data, homePanelSupplier.get()));
            parentFrame.revalidate();
            parentFrame.repaint();

        });
        buttonPanel.add(submitButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    public static void main(String args[]){
        SwingUtilities.invokeLater(() -> {
            Data data = new Data(new Users(), new Posts(), new Comments());
            JFrame frame = new JFrame("Climate Action Program");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(600, 400);
            frame.setLocationRelativeTo(null);
            frame.setContentPane(new NewPost(frame, data));
            frame.setVisible(true);
        });
    }
}