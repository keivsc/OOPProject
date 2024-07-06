package gui.pages;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import gui.MainPanel;
import server.*;

public class Login extends JPanel {



    public Login(JFrame parentFrame, Data data) {
        setLayout(new GridBagLayout());
        setName("Login");
        setBackground(parentFrame.getBackground());
        setBackground(Color.GRAY);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.gridheight = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("Welcome to the Climate Action Program");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        add(titleLabel, gbc);

        gbc.gridwidth = 1;

        gbc.gridy = 1;
        gbc.gridx = 0;
        JLabel userLabel = new JLabel("Email:");
        userLabel.setForeground(Color.WHITE);
        add(userLabel, gbc);

        gbc.gridx = 1;
        JTextField userText = new JTextField(15);
        add(userText, gbc);

        gbc.gridy = 2;
        gbc.gridx = 0;
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setForeground(Color.WHITE);
        add(passwordLabel, gbc);

        gbc.gridx = 1;
        JPasswordField passwordText = new JPasswordField(15);
        add(passwordText, gbc);

        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        JButton createAccountButton = new JButton("Create Account");
        add(createAccountButton, gbc);

        gbc.gridx = 1;
        JButton loginButton = new JButton("Login");
        add(loginButton, gbc);

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Perform login action
                String username = userText.getText();
                String password = new String(passwordText.getPassword());
                if (username.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(parentFrame, "Please enter all the fields correctly.");
                    return;
                }
                User authorizedUser = data.Users().authorize(username, password);
                if (authorizedUser != null) {
                    JOptionPane.showMessageDialog(null, "You have successfully logged in!");
                    data.setUser(authorizedUser);
                    parentFrame.setContentPane(new MainPanel(parentFrame, data, new Home(parentFrame, data)));
                    parentFrame.revalidate();
                    parentFrame.repaint();
                } else {
                    JOptionPane.showMessageDialog(null, "Wrong email or password / account does not exists!");
                }
            }
        });

        createAccountButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                parentFrame.setContentPane(new CreateAccount(parentFrame, data));
                parentFrame.revalidate();
                parentFrame.repaint();
            }
        });
        KeyAdapter keyListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    loginButton.doClick();
                }
            }
        };
        addKeyListener(keyListener);
        passwordText.addKeyListener(keyListener);

        setFocusable(true);
        requestFocusInWindow();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Data data = new Data(new Users(), new Posts());
            JFrame frame = new JFrame("Climate Action Program");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(600, 400);
            frame.setLocationRelativeTo(null);
            frame.setContentPane(new Login(frame, data));
            frame.setVisible(true);
        });
    }
}