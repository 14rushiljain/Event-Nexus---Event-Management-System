package view;

import database.DatabaseHelper;
import model.Registration;
import model.Event;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.io.PrintWriter;
import java.io.File;

/**
 * ClubDash.java
 * -----------------------------------------------------------------------
 * This is the main dashboard for Club Owners. 
 * It allows them to view only the events they own, create new ones, 
 * and manage existing attendees.
 */
public class ClubDash extends JFrame {
    private String clubName;
    
    // Setting up the table structure for event overview
    private DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID", "Title", "Venue", "Start Date", "Registered"}, 0);
    private JTable table = new JTable(model);

    public ClubDash(String clubName) {
        this.clubName = clubName;
        
        // Basic window setup
        setTitle("Club Portal | " + clubName);
        setSize(950, 600);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // --- Header Section ---
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(44, 62, 80)); // Professional dark blue theme
        header.setPreferredSize(new Dimension(950, 60));
        
        JLabel nameLabel = new JLabel("  " + clubName.toUpperCase() + " MANAGEMENT");
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        header.add(nameLabel, BorderLayout.WEST);

        // Simple logout to return to the main login screen
        JButton btnOut = new JButton("Logout");
        btnOut.addActionListener(e -> { 
            new LoginFrame().setVisible(true); 
            this.dispose(); 
        });
        header.add(btnOut, BorderLayout.EAST);

        // --- Main Table Section ---
        table.setRowHeight(30);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // Prevent multi-select bugs
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- Footer/Action Section ---
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        footer.setBackground(Color.WHITE);

        JButton btnAdd = new JButton("+ Create New Event");
        JButton btnManage = new JButton("Manage Selected Event");

        // UI styling for the create button to make it stand out
        btnAdd.setBackground(new Color(46, 204, 113)); // Success green
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFont(new Font("SansSerif", Font.BOLD, 13));

        // Wiring up the buttons
        btnAdd.addActionListener(e -> openEventForm(null)); // null means 'New Mode'
        btnManage.addActionListener(e -> showManageMenu());

        footer.add(btnManage);
        footer.add(btnAdd);

        // Assemble the frame
        add(header, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);

        // Populate table and center window
        loadData();
        setLocationRelativeTo(null);
    }

    /**
     * Filters the global event list to show only events 
     * belonging to this specific club.
     */
    private void loadData() {
        model.setRowCount(0); // Clear old data before reloading
        List<Event> events = DatabaseHelper.getEvents();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        for (Event e : events) {
            // Only add the event if the current club owner matches
            if (e.clubOwner.equals(clubName)) {
                model.addRow(new Object[]{
                    e.id, e.title, e.venue, sdf.format(e.startDate), e.registeredCount
                });
            }
        }
    }

    /**
     * Pops up a menu for the selected event from the table.
     */
    private void showManageMenu() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select an event from the list first.");
            return;
        }

        // Identifying which event was clicked via ID
        int eventId = (int) model.getValueAt(row, 0);
        List<Event> events = DatabaseHelper.getEvents();
        Event selectedEvent = null;
        for(Event e : events) if(e.id == eventId) selectedEvent = e;

        // Custom choice dialog instead of making a whole new window
        String[] options = {"View Attendees", "Edit Details", "Export List", "Delete", "Cancel"};
        int choice = JOptionPane.showOptionDialog(this, "Manage Event: " + selectedEvent.title, 
                "Event Actions", 0, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

        switch (choice) {
            case 0 -> viewAttendees(eventId);
            case 1 -> openEventForm(selectedEvent); // Pass existing event to trigger 'Edit Mode'
            case 2 -> exportAttendees(eventId, selectedEvent.title);
            case 3 -> deleteEvent(eventId);
        }
    }

    /**
     * Handles both creating a new event and editing an existing one.
     * Uses a dynamic JPanel inside a JOptionPane.
     */
    private void openEventForm(Event targetEvent) {
        // UI Layout for the pop-up form
        JPanel form = new JPanel(new GridLayout(0, 2, 10, 10));
        JTextField txtTitle = new JTextField();
        JTextField txtVenue = new JTextField();
        JTextField txtCap = new JTextField();
        JTextField txtStart = new JTextField("dd-mm-yyyy");
        JTextField txtEnd = new JTextField("dd-mm-yyyy");
        JTextField txtLast = new JTextField("dd-mm-yyyy");

        SimpleDateFormat sdf = new SimpleDateFormat("dd-mm-yyyy");

        // If editing, fill the fields with the current data
        if (targetEvent != null) {
            txtTitle.setText(targetEvent.title);
            txtVenue.setText(targetEvent.venue);
            txtCap.setText(String.valueOf(targetEvent.capacity));
            txtStart.setText(sdf.format(targetEvent.startDate));
            txtEnd.setText(sdf.format(targetEvent.endDate));
            txtLast.setText(sdf.format(targetEvent.lastDate));
        }

        // Adding labels and input fields to the form panel
        form.add(new JLabel("Event Title:")); form.add(txtTitle);
        form.add(new JLabel("Venue:")); form.add(txtVenue);
        form.add(new JLabel("Capacity:")); form.add(txtCap);
        form.add(new JLabel("Start Date:")); form.add(txtStart);
        form.add(new JLabel("End Date:")); form.add(txtEnd);
        form.add(new JLabel("Reg Deadline:")); form.add(txtLast);

        int result = JOptionPane.showConfirmDialog(this, form, 
                (targetEvent == null ? "Create New Event" : "Edit Event Details"), 
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                List<Event> allEvents = DatabaseHelper.getEvents();
                
                // Extracting data from fields
                String title = txtTitle.getText();
                String venue = txtVenue.getText();
                int cap = Integer.parseInt(txtCap.getText());
                Date start = sdf.parse(txtStart.getText());
                Date end = sdf.parse(txtEnd.getText());
                Date last = sdf.parse(txtLast.getText());

                if (targetEvent == null) {
                    // Generate new ID based on the last event in the list
                    int id = allEvents.isEmpty() ? 1 : allEvents.get(allEvents.size() - 1).id + 1;
                    allEvents.add(new Event(id, title, clubName, venue, cap, start, end, last));
                } else {
                    // Update the existing event by finding the ID match
                    for (Event e : allEvents) {
                        if (e.id == targetEvent.id) {
                            e.updateDetails(title, venue, cap, start, end, last);
                            break;
                        }
                    }
                }

                // Sync with file/database and update UI
                DatabaseHelper.saveEvents(allEvents);
                loadData();
                JOptionPane.showMessageDialog(this, "Event updated successfully!");

            } catch (Exception ex) {
                // If the user messes up the date format or enters text in Capacity
                JOptionPane.showMessageDialog(this, "Input Error: Check date formats (dd-mm-yyyy) and capacity numbers.");
            }
        }
    }

    
     // Grabs the student list for the event and shows it in a scrollable area.
     
    private void viewAttendees(int eventId) {
        List<Registration> regs = DatabaseHelper.getRegistrationsForEvent(eventId);
        if (regs.isEmpty()) {
            JOptionPane.showMessageDialog(this, "The registration list is currently empty.");
            return;
        }
        
        StringBuilder sb = new StringBuilder("Registered Students:\n\n");
        for (Registration r : regs) sb.append("• ").append(r.studentUser).append("\n");
        
        // Wrapping text area in a scroll pane in case there are 100+ students
        JOptionPane.showMessageDialog(this, new JScrollPane(new JTextArea(sb.toString(), 15, 25)));
    }

    
     // Saves the attendee list to a local .txt file.
    
    private void exportAttendees(int eventId, String title) {
        List<Registration> regs = DatabaseHelper.getRegistrationsForEvent(eventId);
        // Replace spaces with underscores to prevent file system issues
        String fileName = title.replaceAll("\\s+", "_") + "_Attendees.txt";
        
        try (PrintWriter writer = new PrintWriter(new File(fileName))) {
            writer.println("Attendee List for: " + title);
            writer.println("====================================");
            for (Registration r : regs) writer.println(r.studentUser);
            
            JOptionPane.showMessageDialog(this, "Success! List saved to: " + fileName);
        } catch (Exception e) { 
            JOptionPane.showMessageDialog(this, "Failed to export file.");
            e.printStackTrace(); 
        }
    }

   
    //  Removes the event from the database after a confirmation check.
  
    private void deleteEvent(int id) {
        if (JOptionPane.showConfirmDialog(this, "Are you sure you want to permanently delete this event?", "Confirm Deletion", 0) == 0) {
            List<Event> events = DatabaseHelper.getEvents();
            events.removeIf(e -> e.id == id);
            DatabaseHelper.saveEvents(events);
            loadData(); // Refresh the table view
        }
    }
}
