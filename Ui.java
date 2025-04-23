package srms;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Ui {
    private static Ui instance;
    private JFrame frame;

    //private JPanel panel;


    public static Ui getInstance()
    {
        if(instance == null)
        {
            instance = new Ui(new JFrame()   /*,new JPanel()*/);
        }
        return instance;
    }

    private Ui(JFrame frame   /*, JPanel panel*/)
    {
        frame.setTitle("SRMS");
        frame.setSize(1200, 1000);
        frame.setLayout(new FlowLayout());
        frame.setContentPane(frame.getContentPane());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.frame = frame;

        //this.panel = panel;
    }

//    public String getInput(String prompt)
//    {
//
//        frame.add(PopupMenu);
//    }

    //public

    

}
