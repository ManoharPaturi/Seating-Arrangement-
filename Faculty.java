package srms;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class Faculty extends User{

    private String name;
    private String dept;
    private Collection<Event> events;


    public Faculty(String id,String name, String dept)
    {
        super(id,id);
        this.events = new ArrayList<Event>();
        this.name = name;
        this.dept = dept;
    }

    public Collection<Event> getEvents() {
        return events;
    }

    public String getName() {return name;}
    public String getDept() {return dept;}

    public String createEvent(Venue venue, LocalDateTime start, LocalDateTime end, String title, String description)
    {
        Event event = new Event(venue,this,start,end,title,description);
        Event result = venue.addevent(event);
        if(result.equals(event))
        {
            this.events.add(event);
            return "Event created";
        }

        return "Failed to create event";
    }

    public String cancelEvent(Event event)
    {
        if(this.events.contains(event))
        {
            if(event.getVenue().cancelEvent(event))
            {
                events.remove(event);
                return "Event cancelled";
            }

        }
        return "Failed to cancel event";
    }

    public String getEventsInfo() {
        StringBuilder sb = new StringBuilder();
        for (Event event : events) {
            sb.append("Title: ").append(event.getTitle())
                    .append(", Venue: ").append(event.getVenue().getName())
                    .append(", Start: ").append(event.getStart())
                    .append("\n");
        }
        return sb.length() > 0 ? sb.toString() : "No events created.";
    }
    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = super.toMap();
        map.put("name", name);
        map.put("dept", dept);
        return map;
}

}
