package srms;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class Event {
    public static int count;
    private final int id;
    private Venue venue;
    private String title;
    private LocalDateTime start, end;
    private ArrayList<Seat> seats = new ArrayList<>();
    private String description;
    private Faculty faculty;
    private HashMap<String,String[]> StudentAccess = new HashMap<>(5);
    private LinkedList<User> Attendees = new LinkedList<>();



    public Event(Venue venue, Faculty f,LocalDateTime start, LocalDateTime end,String title ,String description)
    {
        this.id = count + 1;
        this.venue = venue;
        this.faculty = f;
        this.start = start;
        this.end = end;
        this.description = description;
        this.title = title;

        initseats();
        count++;

    }

    private void initseats()
    {
        for(int i = 0;i < this.venue.getRowsize();i++)
        {
            for(int j = 0;j < this.venue.getColsize();j++)
            {
                seats.add(new Seat( null,this.venue,this,i,j));
            }
        }
    }

    public Venue getVenue() {return this.venue;}
    public LocalDateTime getStart() {return this.start;}
    public LocalDateTime getEnd() {return this.end;}
    public String getDescription() {return this.description;}
    public Faculty getFaculty() {return faculty;}
    public ArrayList<Seat> getSeats() {return seats;}
    public LinkedList<User> getAttendees() {return Attendees;}
    public int getId() {return id;}
    public String getTitle() {return title;}
    public static int getcount() {return count;}

    public void addAttendee(User user)
    {
        Attendees.add(user);
    }

    public void removeAttendee(User user)
    {
        Attendees.remove(user);
    }

    public Seat getSeat(int row, int col) {
        return seats.stream()
                .filter(s -> s.getRow() == row && s.getCol() == col)
                .findFirst()
                .orElse(null);
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("title", title);
        map.put("description", description);
        map.put("start", start.toString());
        map.put("end", end.toString());
        map.put("venue", venue.toMap());
        map.put("seats", seats.stream().map(Seat::toMap).collect(Collectors.toList()));
        return map;
}
}


