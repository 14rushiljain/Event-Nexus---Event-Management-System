package model;
import java.io.Serializable;

public class Registration implements Serializable 

{
    private static final long serialVersionUID = 1L;
    public String studentUser;
    public int eventId;
    

    public Registration(String s, int e)
    {
        this.studentUser = s; this.eventId = e;
    }
}
