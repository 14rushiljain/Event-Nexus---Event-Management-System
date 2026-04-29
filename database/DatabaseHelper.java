package database;

import model.*;
import java.io.*;
import java.util.*;

public class DatabaseHelper {
    public static String currentUser = null;
    private static final String USER_FILE = "users.dat";
    private static final String EVENT_FILE = "events.dat";
    private static final String REG_FILE = "registrations.dat";


    public static void initialize() {
       
        File uf = new File(USER_FILE);
        List<User> users;

        if (!uf.exists()) {
            users = new ArrayList<>();//it WORKS so do not TOUCH !!!!!!!!!!!
           
            users.add(new User("admin", "admin123", "ADMIN", "SYSTEM"));
            saveUsers(users);
        } else {
           
            users = getUsers();
            boolean adminExists = false;
            for (User u : users) {
                if (u.username.equals("admin")) {
                    u.isActive = true;
                    adminExists = true;
                    break;
                }
            }
            if (!adminExists) {
                users.add(new User("admin", "admin123", "ADMIN", "SYSTEM"));
            }
            saveUsers(users);
        }

       
        if (!new File(EVENT_FILE).exists()) {
            saveEvents(new ArrayList<Event>());
        }

   
        if (!new File(REG_FILE).exists()) {
            saveRegs(new ArrayList<Registration>());
        }
    }

    
    
  
    private static void saveList(String fileName, List<?> list) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileName))) {
            oos.writeObject(list);
        } catch (IOException e) {
            System.err.println("Database Write Error (" + fileName + "): " + e.getMessage());
        }
        
        
    }

    private static List<?> loadList(String fileName) {
        File file = new File(fileName);
        if (!file.exists()) return new ArrayList<>();

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (List<?>) ois.readObject();
        } catch (Exception e) {
            
            return new ArrayList<>();
        }
    }
    
   

    @SuppressWarnings("unchecked")
    public static List<User> getUsers() { 
        return (List<User>) loadList(USER_FILE); 
    }
    
    public static void saveUsers(List<User> list) { 
        saveList(USER_FILE, list); 
    }

    @SuppressWarnings("unchecked")
    public static List<Event> getEvents() { 
        return (List<Event>) loadList(EVENT_FILE); 
    }
    
    public static void saveEvents(List<Event> list) { 
        saveList(EVENT_FILE, list); 
    }

    @SuppressWarnings("unchecked")
    public static List<Registration> getRegs() { 
        return (List<Registration>) loadList(REG_FILE); 
    }
    
    public static void saveRegs(List<Registration> list) { 
        saveList(REG_FILE, list); 
       
    }

    public static List<Registration> getRegistrationsForEvent(int eventId) {
        List<Registration> all = getRegs();
        List<Registration> res = new ArrayList<>();
        for (Registration r : all) {
            if (r.eventId == eventId) {
                res.add(r);
            }
        }
        return res;
    }
}
