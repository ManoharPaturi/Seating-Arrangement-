package srms;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

public class Admin extends User  {
    private static Admin admin;
    private LinkedList<User> users;
    private LinkedList<Venue> venues;
    private LinkedList<Event> events;
    private HashMap<Faculty,Collection<Event>> reservationClashes;

    public static Admin getAdmin()
    {
        if(admin == null) {admin = new Admin();}
        return admin;
    }

    private Admin()
    {
        super("admin","admin");
        this.users = new LinkedList<>();
        this.venues = new LinkedList<>();
        this.events = new LinkedList<>();
    }

    public LinkedList<Event> getEvents()
    {
        return events;
    }

    public LinkedList<User> getUsers()
    {
        return users;
    }

    public LinkedList<Venue> getVenues() {
        return venues;
    }

    public void reservationClash(Event newEvent, Event clashingEvent)
    {
        LinkedList<Event> Events = new LinkedList<>();
        Events.add(newEvent);
        Events.add(clashingEvent);
        this.reservationClashes.put(newEvent.getFaculty(),Events);
    }

    public void addUser(User user) {
        users.add(user);
    }

    public void addVenue(Venue venue) {
        venues.add(venue);
    }

    public void addEvent(Event event) {
        events.add(event);
    }





}
