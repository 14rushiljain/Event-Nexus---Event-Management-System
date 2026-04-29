package view;

import database.DatabaseHelper;
import model.User;
import model.Registration;
import model.Event; 

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;//importing all the necessary libraries

/**
 * AdminDash.java
 * ---------------------------------------------------------
 * This is the primary control hub for the system. 
 * I used a CardLayout for the main content area to keep the 
 * interface clean without spawning a million different windows.
 */

public class AdminDash extends JFrame 
{
    
    // Using CardLayout so we can swap between the dashboard and tables easily
    private final CardLayout viewManager = new CardLayout();    
    private final JPanel displayStack = new JPanel(viewManager);
    
    // UI labels for the summary cards
    private JLabel countUsers, countEvents, countRegs;
    
    // Table models - we'll clear and refill these whenever refreshData is called
    private DefaultTableModel userTableMap, eventTableMap, regTableMap;

    public AdminDash() {
        setupMainFrame();
        syncDataWithDatabase(); // Initial data fetch
    }

    private void setupMainFrame()
    {
        setTitle("Nexus Event Management | Admin Portal");
        setSize(1150, 720); // Bit of extra padding for table columns
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center on screen
        setLayout(new BorderLayout());

        // Sidebar Navigation -
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(34, 45, 50)); // Darker slate theme
        sidebar.setPreferredSize(new Dimension(230, 700));

        JLabel navHeader = new JLabel("EVENT NEXUS");
        navHeader.setForeground(new Color(236, 240, 241));
        navHeader.setFont(new Font("Arial", Font.BOLD, 18));
        navHeader.setBorder(BorderFactory.createEmptyBorder(25, 20, 25, 20));
        sidebar.add(navHeader);

        // Map sidebar buttons to their respective 'Card' IDs
        sidebar.add(navButton("Overview", "PANEL_HOME"));
        sidebar.add(navButton("Manage Users", "PANEL_USERS"));
        sidebar.add(navButton("Audit Events", "PANEL_EVENTS"));
        sidebar.add(navButton("Registration Logs", "PANEL_REGS"));
        
        sidebar.add(Box.createVerticalGlue()); // Pushes the logout button to the bottom
        
        JButton logoutBtn = new JButton("Logout System");
        logoutBtn.addActionListener(e -> { 
            new LoginFrame().setVisible(true); 
            this.dispose(); 
        });
        sidebar.add(logoutBtn);
        sidebar.add(Box.createVerticalStrut(20));

        // - Content Cards Initialization 
        displayStack.add(initSummaryCard(), "PANEL_HOME");
        
        userTableMap = new DefaultTableModel(new String[]{"Username", "Account Type", "Affiliated Club", "Current Status"}, 0);
        displayStack.add(initTableSection("User & Club Access Control", userTableMap, "MODE_USER"), "PANEL_USERS");
        
        eventTableMap = new DefaultTableModel(new String[]{"ID", "Event Title", "Organizing Club", "Venue"}, 0);
        displayStack.add(initTableSection("Global Event Management", eventTableMap, "MODE_EVENT"), "PANEL_EVENTS");

        
        
        regTableMap = new DefaultTableModel(new String[]{"Student ID", "Event ID", "Event Title"}, 0);
        displayStack.add(initTableSection("Master Registration Audit", regTableMap, "MODE_REG"), "PANEL_REGS");

        add(sidebar, BorderLayout.WEST);
        add(displayStack, BorderLayout.CENTER);
    }

    private JPanel initSummaryCard()
    {
        JPanel wrapper = new JPanel(new GridLayout(1, 3, 20, 20));
        wrapper.setBackground(Color.WHITE);
        wrapper.setBorder(BorderFactory.createEmptyBorder(40, 40, 380, 40));
        
        
        
        
        countUsers = new JLabel("0", SwingConstants.CENTER); 
        countEvents = new JLabel("0", SwingConstants.CENTER); 
        countRegs = new JLabel("0", SwingConstants.CENTER);
        // Passing the target card ID so clicking the stat card takes you there
        wrapper.add(buildStatTile("Total Members", countUsers, new Color(52, 152, 219), "PANEL_USERS"));
        wrapper.add(buildStatTile("Active Listings", countEvents, new Color(46, 204, 113), "PANEL_EVENTS"));
        wrapper.add(buildStatTile("Sign-ups", countRegs, new Color(155, 89, 182), "PANEL_REGS"));
        
        return wrapper;
    }

    private JPanel buildStatTile(String label, JLabel val, Color color, String jumpTo) {
        JPanel tile = new JPanel(new BorderLayout());
        tile.setBackground(new Color(245, 245, 245));
        tile.setBorder(BorderFactory.createMatteBorder(0, 0, 4, 0, color));
        tile.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // If the user clicks the big box, jump to the relevant table
        tile.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { viewManager.show(displayStack, jumpTo); }
        });

        
        
        
        val.setFont(new Font("SansSerif", Font.BOLD, 45));
        tile.add(new JLabel(label, SwingConstants.CENTER), BorderLayout.NORTH);
        tile.add(val, BorderLayout.CENTER);
        return tile;
    }

    private JPanel initTableSection(String header, DefaultTableModel model, String uiMode) 
    
    
    {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel title = new JLabel(header);
        title.setFont(new Font("Tahoma", Font.BOLD, 18));
        p.add(title, BorderLayout.NORTH);

        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionRow.setBackground(Color.WHITE);

        
        
        
        // Contextual buttons: Show different buttons depending on which table we are looking at
        if (uiMode.equals("MODE_USER")) {
            JButton addClub = new JButton("Create Club Account");
            JButton toggleBtn = new JButton("Toggle Lock");
            JButton deleteBtn = new JButton("Delete User");
            
            deleteBtn.setForeground(Color.RED);

            addClub.addActionListener(e -> launchClubCreator());
            toggleBtn.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row != -1) processStatusFlip(model.getValueAt(row, 0).toString());
            });
            deleteBtn.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row != -1) handleUserRemoval(model.getValueAt(row, 0).toString());
            });

            actionRow.add(deleteBtn);
            actionRow.add(toggleBtn); 
            actionRow.add(addClub);
        } else if (uiMode.equals("MODE_EVENT")) {
            JButton killEvent = new JButton("Cancel Event");
            killEvent.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row != -1) handleEventCancellation((int)model.getValueAt(row, 0));
            });
            actionRow.add(killEvent);
        }
        p.add(actionRow, BorderLayout.SOUTH);
        return p;
    }//it reurns p 

    public void syncDataWithDatabase() {
        // Grab everything from our helper class
        List<User> allUsers = DatabaseHelper.getUsers();
        List<model.Event> allEvents = DatabaseHelper.getEvents(); 
        List<Registration> allRegs = DatabaseHelper.getRegs();

        // Update the big numbers on the dashboard
        countUsers.setText(String.valueOf(allUsers.size()));
        countEvents.setText(String.valueOf(allEvents.size()));
        countRegs.setText(String.valueOf(allRegs.size()));

        // Update User Table
        userTableMap.setRowCount(0);
        for(User u : allUsers) {
            userTableMap.addRow(new Object[]{u.username, u.role, u.clubName, u.isActive ? "ACTIVE" : "LOCKED"});
        }

        
        
        // Update Event Table
        eventTableMap.setRowCount(0);
        for(model.Event ev : allEvents) {
            eventTableMap.addRow(new Object[]{ev.id, ev.title, ev.clubOwner, ev.venue});
        }

        
        // Update Registration Table (Mapping IDs to Titles for readability)
        regTableMap.setRowCount(0);
        for (Registration r : allRegs) {
            String eName = "N/A";
            for (model.Event ev : allEvents) {
                if (ev.id == r.eventId) { eName = ev.title; break; }
            }
            regTableMap.addRow(new Object[]{r.studentUser, r.eventId, eName});
        }
    }

    private void handleUserRemoval(String target) 
    {
        // Safety check: Don't let the admin delete themselves
        if ("admin".equalsIgnoreCase(target)) {
            JOptionPane.showMessageDialog(this, "Operation denied: Master admin cannot be removed.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to wipe " + target + "? This cannot be undone.", "Warning", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        List<User> users = DatabaseHelper.getUsers();
        List<model.Event> events = DatabaseHelper.getEvents();
        List<Registration> regs = DatabaseHelper.getRegs();

        // If we delete a student, we need to update the event's registration counts
        for (Registration r : regs) {
            if (r.studentUser.equals(target)) {
                for (model.Event ev : events) {
                    if (ev.id == r.eventId) {
                        ev.registeredCount = Math.max(0, ev.registeredCount - 1);
                        break;
                    }
                }
            }
        }

        regs.removeIf(r -> r.studentUser.equals(target));
        users.removeIf(u -> u.username.equals(target));

        DatabaseHelper.saveUsers(users);
        DatabaseHelper.saveEvents(events);
        DatabaseHelper.saveRegs(regs);

        syncDataWithDatabase(); // Refresh the UI
        JOptionPane.showMessageDialog(this, "User purged successfully.");
    }

    private void handleEventCancellation(int id)
    {
        List<model.Event> list = DatabaseHelper.getEvents();
        if (list.removeIf(e -> e.id == id)) {
            DatabaseHelper.saveEvents(list);
            syncDataWithDatabase();
        }
    }

    private void launchClubCreator() 
    {
        // Quick input dialogs for new club setup
        String club = JOptionPane.showInputDialog(this, "New Club Name:");
        if (club == null || club.trim().isEmpty()) return;
        
        String uid = JOptionPane.showInputDialog(this, "Create Club Username:");
        String pass = JOptionPane.showInputDialog(this, "Create Password:");
        
        if (uid != null && pass != null) {
            List<User> current = DatabaseHelper.getUsers();
            current.add(new User(uid, pass, "CLUB", club));
            DatabaseHelper.saveUsers(current);
            syncDataWithDatabase();
        }
    }

    private void processStatusFlip(String uid) 
    {
        if ("admin".equalsIgnoreCase(uid)) return;
        List<User> list = DatabaseHelper.getUsers();
        for (User u : list) {
            if (u.username.equals(uid)) {
                u.isActive = !u.isActive; // Flip the boolean
                break;
            }
        }
        DatabaseHelper.saveUsers(list);
        syncDataWithDatabase();
    }

    private JButton navButton(String text, String cardID) 
    
    {
        JButton b = new JButton(text);
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        b.setMaximumSize(new Dimension(230, 40));
        b.setBackground(new Color(34, 45, 50));
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(null);
        
        
        
        // Lambda for card switching
        b.addActionListener(e -> viewManager.show(displayStack, cardID));
        
        return b;
    }
}
