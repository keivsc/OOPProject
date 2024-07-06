package gui;

import gui.pages.Login;
import server.*;

import javax.swing.*;
import java.awt.*;


public class ClimateActionProgram extends JFrame {

    public ClimateActionProgram(Data data){
        setTitle("Climate Action Program");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        add(new Login(this, data), BorderLayout.CENTER);
        setBackground(Color.GRAY);
        setVisible(true);
    };


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Data data = new Data(new Users(), new Posts());
            ClimateActionProgram frame = new ClimateActionProgram(data);
        });
    }
}