package view;

import database.DatabaseHelper;
import model.Event;
import model.Registration;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * StudentDash.java
 * ---------------------------------------------------------
 * This is the main interface for students (and guests).
 * It uses a TabbedPane to separate "Looking for events" from 
 * "Events I've already joined."
 */
public class StudentDash extends JFrame 
{
    // Table models to handle the data for discovery and personal schedule
    private DefaultTableModel discoverModel, myScheduleModel;
    private JTable discoverTable, myScheduleTable;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    public StudentDash()
    {
        // Standard window setup
        setTitle("Student Hub | EventNexus");
        setSize(1000, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Top Branding / Welcome Bar 
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(44, 62, 80)); // Matching the Admin/Club dash theme
        header.setPreferredSize(new Dimension(1000, 60));
        
        // Show current username from the DatabaseHelper session
        JLabel welcome = new JLabel("  Welcome, " + DatabaseHelper.currentUser);
        welcome.setForeground(Color.WHITE);
        welcome.setFont(new Font("SansSerif", Font.BOLD, 16));
        
        JButton btnOut = new JButton("Exit Hub");
        btnOut.addActionListener(e -> { 
            new LoginFrame().setVisible(true); 
            this.dispose(); 
        });
        
        header.add(welcome, BorderLayout.WEST); 
        header.add(btnOut, BorderLayout.EAST);

        // Navigation Tabs
        // JTabbedPane is used here instead of CardLayout for a more standard "User Portal" feel
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Discover Events", createDiscoverPanel());
        tabs.addTab("My Registered Events", createSchedulePanel());

        add(header, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);

        // Initial data pull and window positioning
        loadData();
        setLocationRelativeTo(null);
    }

    /**
     * Builds the UI for the event discovery list.
     */
    private JPanel createDiscoverPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Added 'Seats' and 'Status' columns so students know if an event is full or already joined
        discoverModel = new DefaultTableModel(new String[]{"ID", "Title", "Club", "Venue", "Date", "Seats", "Status"}, 0);
        discoverTable = new JTable(discoverModel);
        
        JButton btnReg = new JButton("Register for Selected Event");
        btnReg.setBackground(new Color(46, 204, 113)); // Green for positive action
        btnReg.setForeground(Color.WHITE);
        btnReg.addActionListener(e -> handleRegistration());

        panel.add(new JScrollPane(discoverTable), BorderLayout.CENTER);
        panel.add(btnReg, BorderLayout.SOUTH);
        return panel;
    }

    /**
     * Builds the UI for the student's personal schedule.
     */
    private JPanel createSchedulePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        myScheduleModel = new DefaultTableModel(new String[]{"ID", "Event Title", "Venue", "Date"}, 0);
        myScheduleTable = new JTable(myScheduleModel);

        JButton btnDeReg = new JButton("De-register (Cancel Participation)");
        btnDeReg.setBackground(new Color(231, 76, 60)); // Red for cancellation
        btnDeReg.setForeground(Color.WHITE);
        btnDeReg.addActionListener(e -> handleDeRegistration());

        panel.add(new JScrollPane(myScheduleTable), BorderLayout.CENTER);
        panel.add(btnDeReg, BorderLayout.SOUTH);
        return panel;
    }

    /**
     * Refreshes both tables by pulling fresh data from the database files.
     */
    private void loadData() {
        discoverModel.setRowCount(0);
        myScheduleModel.setRowCount(0);
        
        List<Event> events = DatabaseHelper.getEvents();
        List<Registration> regs = DatabaseHelper.getRegs();

        for (Event e : events) {
            // Check if the current logged-in user is already on the attendee list
            boolean isReg = isUserRegistered(e.id, regs);
            
            // Populate the Discovery table (shows everything)
            discoverModel.addRow(new Object[]{
                e.id, e.title, e.clubOwner, e.venue, sdf.format(e.startDate), 
                (e.capacity - e.registeredCount), (isReg ? "REGISTERED" : "OPEN")
            });

            // If the user is registered, also add it to their personal "Schedule" tab
            if (isReg) {
                myScheduleModel.addRow(new Object[]{ e.id, e.title, e.venue, sdf.format(e.startDate) });
            }
        }
    }

    /**
     * Helper method to cross-reference event IDs with the registration list.
     */
    private boolean isUserRegistered(int eventId, List<Registration> regs) {
        if ("Guest".equals(DatabaseHelper.currentUser)) return false;
        
        for (Registration r : regs) {
            if (r.eventId == eventId && r.studentUser.equals(DatabaseHelper.currentUser)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Logic for joining an event. Handles Guests, Overbooking, and Double-Registration.
     */
    private void handleRegistration() {
        // Block guests from signing up
        if ("Guest".equals(DatabaseHelper.currentUser)) {
            JOptionPane.showMessageDialog(this, "Guests cannot register. Please login with a student account.");
            return;
        }

        int row = discoverTable.getSelectedRow();
        if (row == -1) return;

        int id = (int) discoverModel.getValueAt(row, 0);
        
        // Prevent registering for the same thing twice
        if (discoverModel.getValueAt(row, 6).equals("REGISTERED")) {
            JOptionPane.showMessageDialog(this, "You are already registered for this event!");
            return;
        }

        List<Event> events = DatabaseHelper.getEvents();
        List<Registration> regs = DatabaseHelper.getRegs();

        for (Event e : events) {
            if (e.id == id) {
                // Check if there is physically room left in the venue
                if (e.registeredCount < e.capacity) {
                    e.registeredCount++; // Increment the global counter
                    regs.add(new Registration(DatabaseHelper.currentUser, id)); // Create the link
                    
                    // Save both files to keep the data synced
                    DatabaseHelper.saveEvents(events);
                    DatabaseHelper.saveRegs(regs);
                    
                    JOptionPane.showMessageDialog(this, "Registration successful!");
                    loadData(); // Refresh UI
                } else {
                    JOptionPane.showMessageDialog(this, "Sorry, this event is at maximum capacity!");
                }
                break;
            }
        }
    }

    /**
     * Logic for leaving an event. Updates registration counts so others can join.
     */
    private void handleDeRegistration() {
        int row = myScheduleTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select an event from your schedule to de-register.");
            return;
        }

        // Ask for confirmation so users don't accidentally leave an event
        int confirm = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to cancel your registration?", "Confirm Cancellation", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        int id = (int) myScheduleModel.getValueAt(row, 0);
        List<Event> events = DatabaseHelper.getEvents();
        List<Registration> regs = DatabaseHelper.getRegs();

        // Remove the specific registration entry
        regs.removeIf(r -> r.eventId == id && r.studentUser.equals(DatabaseHelper.currentUser));
        
        // Find the event and subtract from the counter
        for (Event e : events) {
            if (e.id == id) {
                e.registeredCount = Math.max(0, e.registeredCount - 1);
                break;
            }
        }

        // Update database and refresh view
        DatabaseHelper.saveEvents(events);
        DatabaseHelper.saveRegs(regs);
        JOptionPane.showMessageDialog(this, "Your spot has been cancelled.");
        loadData();
    }
}
