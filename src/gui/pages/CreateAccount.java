package gui.pages;

import server.Comments;
import server.Data;
import server.Posts;
import server.Users;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.function.Supplier;

public class CreateAccount extends JPanel {
    private Pattern pattern = Pattern.compile("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$");

    public CreateAccount(JFrame parentFrame, Data data) {
        setLayout(new GridBagLayout());
        setName("CreateAccount");
        setBackground(Color.GRAY);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.gridheight = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("Creating a new account");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        titleLabel.setForeground(Color.WHITE);
        add(titleLabel, gbc);
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.gridx = 0;
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setForeground(Color.WHITE);
        add(usernameLabel, gbc);

        gbc.gridx = 1;
        JTextField usernameText = new JTextField(15);
        add(usernameText, gbc);

        gbc.gridy = 2;
        gbc.gridx = 0;
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setForeground(Color.WHITE);
        add(emailLabel, gbc);

        gbc.gridx = 1;
        JTextField emailText = new JTextField(15);
        add(emailText, gbc);

        gbc.gridy = 3;
        gbc.gridx = 0;
        JLabel passwordLabel = new JLabel("Enter Password:");
        passwordLabel.setForeground(Color.WHITE);
        add(passwordLabel, gbc);

        gbc.gridx = 1;
        JPasswordField passwordText = new JPasswordField(15);
        add(passwordText, gbc);

        gbc.gridy = 4;
        gbc.gridx = 0;
        JLabel authCodeLabel = new JLabel("Enter Auth Code:");
        authCodeLabel.setForeground(Color.WHITE);
        add(authCodeLabel, gbc);

        gbc.gridx = 1;
        JPasswordField authCodeText = new JPasswordField(15);
        add(authCodeText, gbc);

        gbc.gridy = 5;
        gbc.gridx = 0;
        JButton backButton = new JButton("Back");
        add(backButton, gbc);

        gbc.gridx = 1;
        JButton createAccountButton = new JButton("Create Account");
        add(createAccountButton, gbc);

        createAccountButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameText.getText();
                String email = emailText.getText();
                String password = new String(passwordText.getPassword());
                String authCode = new String(authCodeText.getPassword());
                if (email.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Please fill all the required fields");
                } else {
                    Matcher matcher = pattern.matcher(email);
                    if (!matcher.matches()) {
                        JOptionPane.showMessageDialog(null, "Please enter a valid email address");
                        return;
                    }
                    if (data.Users().newUser(username, email, password, authCode) == 1) {
                        JOptionPane.showMessageDialog(null, "Account with this email already exists");
                    } else {
                        JOptionPane.showMessageDialog(null, "Account created successfully");
                        backButton.doClick(0);
                    }
                }
            }
        });

        Supplier<JPanel> loginPanelSupplier = () -> new Login(parentFrame, data);

        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                parentFrame.setContentPane(loginPanelSupplier.get());
                parentFrame.revalidate();
                parentFrame.repaint();
            }
        });

        KeyAdapter keyListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    createAccountButton.doClick();
                }
            }
        };

        addKeyListener(keyListener);
        passwordText.addKeyListener(keyListener);
        authCodeText.addKeyListener(keyListener);

        setFocusable(true);
        requestFocusInWindow();
    }

    public static void main(String args[]) {
        SwingUtilities.invokeLater(() -> {
            Data data = new Data(new Users(), new Posts(), new Comments());
            JFrame frame = new JFrame("Climate Action Program");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(600, 400);
            frame.setLocationRelativeTo(null);
            frame.setContentPane(new CreateAccount(frame, data));
            frame.setVisible(true);
        });
    }
}