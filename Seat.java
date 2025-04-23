package srms;

import java.util.HashMap;
import java.util.Map;

public class Seat {
    public static int count = 0;
    private final int id;
    private User user;
    private boolean isReserved;
    private final int eventId;
    private final Venue venue;
    private final Event event;
    private final int row;
    private final int col;

    public Seat(User user, Venue venue, Event event, int row, int col)
    {
        this.id = count + 1;
        this.user = user;
        this.venue = venue;
        this.event = event;
        this.eventId = event.getId();
        this.row = row;
        this.col = col;
        this.isReserved = false;
        count++;
    }



    public Venue getVenue() {return venue;}
    public Event getEvent() {return event;}
    public int getRow() {return row;}
    public int getCol() {return col;}
    public boolean getIsReserved() {return isReserved;}
    public User getUser() {return user;}
    public int getId() {return id;}

    public boolean Reserve(User user)
    {
        if(!this.isReserved)
        {

            for(Seat seat : user.getReservedSeats())
            {
                if(this.event.equals(seat.event))
                {
                    return false;
                }
            }

            this.user = user;
            this.isReserved = true;
            this.event.addAttendee(user);
            return true;

        }
        return false;
    }

    public boolean cancel(User user)
    {
        if(this.user.equals(user))
        {
            this.isReserved = false;
            this.user = null;
            this.event.removeAttendee(user);
            return true;
        }
        return false;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("row", row);
        map.put("col", col);
        map.put("isReserved", isReserved);
        map.put("eventId", eventId);
        return map;
}
}

