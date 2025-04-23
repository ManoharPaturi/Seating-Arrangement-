package srms;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Venue {
    public static int count;
    private final int  venueId;
    private int[][] SeatsArray;
    private String name;
    private int rowsize;
    private int colsize;
    private Collection<Event> events = new ArrayList<Event>();
    private Collection<Faculty> facultiesAllowed =  new ArrayList<Faculty>();



    public Venue(int rowsize,int colsize,String name,Collection<Event> events,Collection<Faculty> facultiesAllowed)
    {
        this.venueId = count + 1;
        initseats(rowsize,colsize);
        this.rowsize = rowsize;
        this.colsize = colsize;
        this.name=name;
        this.events.addAll(events);
        this.facultiesAllowed.addAll(facultiesAllowed);
        count++;

    }

    public Venue()
    {
        this.venueId = count + 1;
        count++;
    }



    private void initseats(int rowsize,int colsize)
    {
        this.SeatsArray = new int[rowsize][colsize];

    }

    public int getVenueId(){return venueId;}
    public int getColsize() {return colsize;}
    public int getRowsize() {return rowsize;}
    public String getName(){return name;}
    public Collection<Event> getEvents() {return events;}
    public Collection<Faculty> getFacultiesAllowed() {return facultiesAllowed;}
    public int[][] getSeatsArray() {return SeatsArray;}
    public static int getCount() {return count;}

    public  Event addevent(Event event)
    {
        if(!this.facultiesAllowed.contains(event.getFaculty()))
        {
            return null;
        }

        for (Event Tevent : events)
        {
            LocalDateTime es = event.getStart();
            LocalDateTime ee = event.getEnd();
            LocalDateTime ts = Tevent.getStart();
            LocalDateTime te = Tevent.getEnd();
            if(!(es.isAfter(te.plusMinutes(15)) || ee.isBefore(ts.minusMinutes(15))))
            {

                return Tevent;
            }
        }

        this.events.add(event);
        return event;
    }

    public boolean cancelEvent(Event event)
    {
        if(this.events.contains(event))
        {
            this.events.remove(event);
            return true;
        }
        return false;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", venueId);
        map.put("name", name);
        map.put("rowsize", rowsize);
        map.put("colsize", colsize);
        return map;
}

}
