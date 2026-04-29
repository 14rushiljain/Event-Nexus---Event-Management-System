package model;

import java.io.Serializable;
import java.util.Date;

public class Event implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public int id;
    public String title;
    public String clubOwner;
    public String venue;
    public int capacity;
    public int registeredCount;
    public Date startDate;
    public Date endDate;
    public Date lastDate;

    public Event(int id, String title, String clubOwner, String venue, int capacity, Date start, Date end, Date last) {
        this.id = id;
        this.title = title;
        this.clubOwner = clubOwner;
        this.venue = venue;
        this.capacity = capacity;
        this.startDate = start;
        this.endDate = end;
        this.lastDate = last;
        this.registeredCount = 0;
    }

    public void updateDetails(String title, String venue, int capacity, Date start, Date end, Date last) {
        this.title = title;
        this.venue = venue;
        this.capacity = capacity;
        this.startDate = start;
        this.endDate = end;
        this.lastDate = last;
    }

    public int getSeatsLeft() {
        return Math.max(0, capacity - registeredCount);
    }
}
