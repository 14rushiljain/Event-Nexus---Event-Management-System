package model;

import java.io.Serializable;

public class User implements Serializable 

{
    private static final long serialVersionUID = 1L;
    
    public String username;
    public String password;
    public String role;     
    public String clubName;  
    public boolean isActive; 

    public User(String username, String password, String role, String clubName) 
    
    {
        this.username = username;
        this.password = password;
        this.role = role;
        this.clubName = clubName;
        this.isActive = true;      // attaching and mentioning and connecting the methods 
    }

    
    
    @Override
    public String toString() 
    {
        return username + " (" + role + ")";
    }
}
