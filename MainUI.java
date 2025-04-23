package srms;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.LinkedList;

import srms.*;
import srms.Event;

public class MainUI extends JFrame {

    public static MainUI mainUI;
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private LinkedList<Event> events;  // Your list of events
    private Event selectedEvent;
    private Seat selectedSeat;

    // Panels
    private JPanel loginPanel, eventListPanel, eventDetailPanel, seatReservePanel;

    // For seat buttons
    private JButton[][] seatButtons;

    public static MainUI getInstance()
    {
        if(mainUI == null)
        {
            mainUI = new MainUI();
        }
        return mainUI;
    }

    private MainUI() {

        Admin admin = Admin.getAdmin();
        this.events = admin.getEvents();

        setTitle("Seat and Venue Reservation System");
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Maximize window
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        createLoginPanel();
        createEventListPanel();
        createEventDetailPanel();
        createSeatReservePanel();

        add(mainPanel);
        cardLayout.show(mainPanel, "login");
        setVisible(true);
    }

    private void createLoginPanel() {
        loginPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        loginPanel.setBackground(Color.WHITE);

        JLabel userLabel = new JLabel("User ID:");
        JTextField userField = new JTextField(20);
        JLabel passLabel = new JLabel("Password:");
        JPasswordField passField = new JPasswordField(20);
        JLabel statusLabel = new JLabel(" ", SwingConstants.CENTER);
        JButton loginButton = new JButton("Login");

        loginButton.addActionListener(e -> {
            String user = userField.getText();
            String pass = new String(passField.getPassword());
            if (user.equals("admin") && pass.equals("1234")) {
                cardLayout.show(mainPanel, "events");
            } else {
                statusLabel.setText("Wrong password!");
                statusLabel.setForeground(Color.RED);
            }
        });

        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        loginPanel.add(userLabel, gbc);
        gbc.gridx = 1;
        loginPanel.add(userField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        loginPanel.add(passLabel, gbc);
        gbc.gridx = 1;
        loginPanel.add(passField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        loginPanel.add(loginButton, gbc);

        gbc.gridy = 3;
        loginPanel.add(statusLabel, gbc);

        mainPanel.add(loginPanel, "login");
    }

    private void createEventListPanel() {
        eventListPanel = new JPanel(new BorderLayout());
        DefaultListModel<String> listModel = new DefaultListModel<>();

        for (Event event : events) {
            listModel.addElement(event.getTitle());
        }

        JList<String> eventListUI = new JList<>(listModel);
        eventListUI.setFont(new Font("Arial", Font.PLAIN, 20));
        eventListUI.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(eventListUI);

        eventListUI.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2 && eventListUI.getSelectedIndex() != -1) {
                    selectedEvent = events.get(eventListUI.getSelectedIndex());
                    updateEventDetailUI();
                    cardLayout.show(mainPanel, "details");
                }
            }
        });

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> cardLayout.show(mainPanel, "login"));

        eventListPanel.add(scrollPane, BorderLayout.CENTER);
        eventListPanel.add(logoutButton, BorderLayout.SOUTH);

        mainPanel.add(eventListPanel, "events");
    }

    private void createEventDetailPanel() {
        eventDetailPanel = new JPanel(new BorderLayout());

        JLabel titleLabel = new JLabel("Event Title", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));

        JTextArea eventTextArea = new JTextArea();
        eventTextArea.setEditable(false);
        eventTextArea.setLineWrap(true);
        eventTextArea.setWrapStyleWord(true);
        JScrollPane detailScroll = new JScrollPane(eventTextArea);

        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> cardLayout.show(mainPanel, "events"));

        JButton reserveButton = new JButton("Reserve Seats");
        reserveButton.addActionListener(e -> {
            updateSeatGrid();
            cardLayout.show(mainPanel, "reserve");
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(backButton);
        buttonPanel.add(reserveButton);

        eventDetailPanel.add(titleLabel, BorderLayout.NORTH);
        eventDetailPanel.add(detailScroll, BorderLayout.CENTER);
        eventDetailPanel.add(buttonPanel, BorderLayout.SOUTH);

        mainPanel.add(eventDetailPanel, "details");

        // Store for update
        eventDetailPanel.putClientProperty("titleLabel", titleLabel);
        eventDetailPanel.putClientProperty("eventTextArea", eventTextArea);
    }

    private void updateEventDetailUI() {
        JLabel title = (JLabel) eventDetailPanel.getClientProperty("titleLabel");
        JTextArea area = (JTextArea) eventDetailPanel.getClientProperty("eventTextArea");

        title.setText(selectedEvent.getTitle());
        area.setText(selectedEvent.getDescription());
    }

    private void createSeatReservePanel() {
        seatReservePanel = new JPanel(new BorderLayout());
        mainPanel.add(seatReservePanel, "reserve");
    }

    private void updateSeatGrid() {
        JPanel gridPanel = new JPanel(new GridLayout(5, 5, 5, 5));
        seatButtons = new JButton[5][5];
        selectedSeat = null;

        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                int index = i * 5 + j;
                Seat seat = selectedEvent.getSeats().get(index);
                JButton seatButton = new JButton((i + "," + j));
                seatButton.setBackground((seat.getIsReserved()) ? Color.GRAY : Color.LIGHT_GRAY);

                seatButton.setEnabled(!seat.getIsReserved());
                seatButton.addActionListener(e -> {
                    if (selectedSeat != null) {
                        JButton prev = seatButtons[selectedSeat.getRow()][selectedSeat.getCol()];
                        prev.setBackground(Color.LIGHT_GRAY);
                    }
                    selectedSeat = seat;
                    seatButton.setBackground(Color.GREEN);
                });

                seatButtons[i][j] = seatButton;
                gridPanel.add(seatButton);
            }
        }

        JButton reserveNow = new JButton("Reserve");
        reserveNow.addActionListener(e -> {
            if (selectedSeat != null) {
                System.out.println("Reserved Seat at: " + selectedSeat.getRow() + "," + selectedSeat.getCol());
                // Your logic here

                cardLayout.show(mainPanel, "details");
            }
        });

        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> cardLayout.show(mainPanel, "details"));

        JPanel btnPanel = new JPanel();
        btnPanel.add(reserveNow);
        btnPanel.add(backButton);

        seatReservePanel.removeAll();
        seatReservePanel.add(gridPanel, BorderLayout.CENTER);
        seatReservePanel.add(btnPanel, BorderLayout.SOUTH);
        seatReservePanel.revalidate();
        seatReservePanel.repaint();
    }
//    public static void main(String[] args) {
//        // Sample test data
//        ArrayList<Seat> sampleSeats = new ArrayList<>();
//        for (int i = 0; i < 25; i++) sampleSeats.add(new Seat(null, false));
//        ArrayList<Event> demoEvents = new ArrayList<>();
//        demoEvents.add(new Event("Concert", "Live music concert!", new ArrayList<>(sampleSeats)));
//        demoEvents.add(new Event("Tech Talk", "AI and Robotics insights.", new ArrayList<>(sampleSeats)));
//
//        SwingUtilities.invokeLater(() -> new MainUI(demoEvents));
//    }

}
