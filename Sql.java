

package srms;

import java.sql.*;
import java.util.ArrayList;
import java.time.LocalDateTime;

import java.sql.*;
import java.util.*;

public class Sql {
    private Connection connection;
    public static Sql instance;
    private boolean isConnected;

    private Sql() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/srmsdata", "root", "Athul*2007");
            isConnected = true;
        } catch (SQLException e) {
            connection = null;
            isConnected = false;
            System.out.println("SQL connection failed: " + e.getMessage() + ". Using in-memory data.");
        }
    }

    public static Sql connection() {
        if (instance == null) {
            instance = new Sql();
        }
        return instance;
    }


    public void save(Admin admin) throws SQLException {
        Sql sql = Sql.connection();
        try (Connection conn = sql.connection) {
            conn.setAutoCommit(false);

            PreparedStatement psUser = conn.prepareStatement("INSERT INTO users (id, password, role) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE password=?, role=?");
            PreparedStatement psStudent = conn.prepareStatement("REPLACE INTO students (id, name, semester, year, course, section) VALUES (?, ?, ?, ?, ?, ?)");
            PreparedStatement psFaculty = conn.prepareStatement("REPLACE INTO faculty (id, name, dept) VALUES (?, ?, ?)");
            PreparedStatement psVenue = conn.prepareStatement("REPLACE INTO venues (id, name, rowsize, colsize) VALUES (?, ?, ?, ?)");
            PreparedStatement psEvent = conn.prepareStatement("REPLACE INTO events (id, title, description, start, end, venue_id, faculty_id) VALUES (?, ?, ?, ?, ?, ?, ?)");
            PreparedStatement psSeat = conn.prepareStatement("REPLACE INTO seats (id, row, col, is_reserved, event_id, user_id) VALUES (?, ?, ?, ?, ?, ?)");

            for (User user : admin.getUsers()) {
                String role = (user instanceof Student) ? "Student" : (user instanceof Faculty ? "Faculty" : "User");
                psUser.setString(1, user.getId());
                psUser.setString(2, user.getPassword(this));
                psUser.setString(3, role);
                psUser.setString(4, user.getPassword(this));
                psUser.setString(5, role);
                psUser.executeUpdate();

                if (user instanceof Student student) {
                    psStudent.setString(1, student.getId());
                    psStudent.setString(2, student.getName());
                    psStudent.setInt(3, student.getSemester());
                    psStudent.setInt(4, student.getYear());
                    psStudent.setString(5, student.getCourse());
                    psStudent.setString(6, student.getSection());
                    psStudent.executeUpdate();
                } else if (user instanceof Faculty faculty) {
                    psFaculty.setString(1, faculty.getId());
                    psFaculty.setString(2, faculty.getName());
                    psFaculty.setString(3, faculty.getDept());
                    psFaculty.executeUpdate();
                }
            }

            for (Venue venue : admin.getVenues()) {
                psVenue.setInt(1, venue.getVenueId());
                psVenue.setString(2, venue.getName());
                psVenue.setInt(3, venue.getRowsize());
                psVenue.setInt(4, venue.getColsize());
                psVenue.executeUpdate();
            }

            for (Event event : admin.getEvents()) {
                psEvent.setInt(1, event.getId());
                psEvent.setString(2, event.getTitle());
                psEvent.setString(3, event.getDescription());
                psEvent.setString(4, event.getStart().toString());
                psEvent.setString(5, event.getEnd().toString());
                psEvent.setInt(6, event.getVenue().getVenueId());
                psEvent.setString(7, event.getFaculty().getId());
                psEvent.executeUpdate();

                for (Seat seat : event.getSeats()) {
                    psSeat.setInt(1, seat.getId());
                    psSeat.setInt(2, seat.getRow());
                    psSeat.setInt(3, seat.getCol());
                    psSeat.setBoolean(4, seat.getIsReserved());
                    psSeat.setInt(5, seat.getEvent().getId());
                    psSeat.setString(6, seat.getUser() != null ? seat.getUser().getId() : null);
                    psSeat.executeUpdate();
                }
            }

            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public  Admin load() throws SQLException {
        Admin admin = Admin.getAdmin();
        Sql sql = Sql.connection();
        try (Connection conn = sql.connection) {
            Statement stmt = conn.createStatement();

            ResultSet rsUser = stmt.executeQuery("SELECT * FROM users");
            Map<String, User> userMap = new HashMap<>();
            while (rsUser.next()) {
                String id = rsUser.getString("id");
                String password = rsUser.getString("password");
                String role = rsUser.getString("role");
                User user = switch (role) {
                    case "Student" -> new Student(id);
                    case "Faculty" -> new Faculty(id, "", "");
                    default -> new User(id, password);
                };
                userMap.put(id, user);
                admin.addUser(user);
            }

            ResultSet rsStudent = stmt.executeQuery("SELECT * FROM students");
            while (rsStudent.next()) {
                Student student = new Student(
                        rsStudent.getString("id"),
                        rsStudent.getString("name"),
                        rsStudent.getInt("semester"),
                        rsStudent.getInt("year"),
                        rsStudent.getString("course"),
                        rsStudent.getString("section")
                );
                admin.addUser(student);
                userMap.put(student.getId(), student);
            }

            ResultSet rsFaculty = stmt.executeQuery("SELECT * FROM faculty");
            while (rsFaculty.next()) {
                Faculty faculty = new Faculty(
                        rsFaculty.getString("id"),
                        rsFaculty.getString("name"),
                        rsFaculty.getString("dept")
                );
                admin.addUser(faculty);
                userMap.put(faculty.getId(), faculty);
            }

            Map<Integer, Venue> venueMap = new HashMap<>();
            ResultSet rsVenue = stmt.executeQuery("SELECT * FROM venues");
            while (rsVenue.next()) {
                Venue venue = new Venue(
                        rsVenue.getInt("rowsize"),
                        rsVenue.getInt("colsize"),
                        rsVenue.getString("name"),
                        new ArrayList<>(),
                        new ArrayList<>()
                );
                venueMap.put(rsVenue.getInt("id"), venue);
                admin.addVenue(venue);
            }

            Map<Integer, Event> eventMap = new HashMap<>();
            ResultSet rsEvent = stmt.executeQuery("SELECT * FROM events");
            while (rsEvent.next()) {
                Venue venue = venueMap.get(rsEvent.getInt("venue_id"));
                Faculty faculty = (Faculty) userMap.get(rsEvent.getString("faculty_id"));
                Event event = new Event(
                        venue,
                        faculty,
                        LocalDateTime.parse(rsEvent.getString("start")),
                        LocalDateTime.parse(rsEvent.getString("end")),
                        rsEvent.getString("title"),
                        rsEvent.getString("description")
                );
                eventMap.put(rsEvent.getInt("id"), event);
                admin.addEvent(event);
            }

            ResultSet rsSeat = stmt.executeQuery("SELECT * FROM seats");
            while (rsSeat.next()) {
                Event event = eventMap.get(rsSeat.getInt("event_id"));
                Venue venue = venueMap.get(event.getVenue().getVenueId());
                User user = userMap.get(rsSeat.getString("user_id"));
                Seat seat = new Seat(user, venue, event, rsSeat.getInt("row"), rsSeat.getInt("col"));
                if (rsSeat.getBoolean("is_reserved")) {
                    seat.Reserve(user);
                }
                event.getSeats().add(seat);
            }
        }

        return admin;
    }
}


    /*
    private Collection<Venue> Load() {
        Collection<Venue> venues = new ArrayList<>();
        String sql = "SELECT * FROM venue";
        try (Statement stmt = connection.createStatement();
             ResultSet venuesRS = stmt.executeQuery(sql)) {
            while (venuesRS.next()) {
                // Incomplete implementation
            }
        } catch (SQLException e) {
             System.out.println("Failed to load users from SQL: " + e.getMessage());
        }
        return venues;
    }
    */
}
//import java.sql.*;
//import java.util.ArrayList;
//import java.util.Collection;
//
//public class Sql {
//    private static Sql instance;
//    private Connection connection;
//
//    private Sql()
//    {
//
//        try
//        {
//            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/srmsdata","root","Athul*2007");
//        }
//        catch (SQLException e)
//        {
//            connection = null;
//            System.out.println("SQL connection failed: " + e.getMessage() + ". Using in-memory data.");
//
//        }
//    }
//
//    public static Sql connection()
//    {
//
//        if(instance== null)
//        {
//            instance = new Sql();
//        }
//
//        return instance;
//
//
//    }
//
//    private Collection<Venue> Load()
//    {
//        Collection<Venue> venues = new ArrayList<Venue>();
//        String sql = "Select * from venue;";
//        try {
//            Statement stmt = connection.createStatement();
//            ResultSet VenuesRS = stmt.executeQuery(sql);
//            do
//            {
//                ResultSet EventsRS = stmt.executeQuery("Select * from events where venue_id = " + VenuesRS.getString("venue_id"));
//
//
//            }while(VenuesRS.next());
//            stmt.close();
//
//        }
//        catch (SQLException e)
//        {
//           System.out.println("Failed to load users from SQL: " + e.getMessage());
//        }
//        return venues;
//
//    }

//    private Collection<Event> getEvents(int VenueId)
//    {
//        Collection<Event> events = new ArrayList<>();
//        String sql = "Select * from event;";
//        try {
//            Statement stmt = connection.createStatement();
//            ResultSet resultSet = stmt.executeQuery(sql);
//            do
//            {
//
//            }while(resultSet.next());
//            stmt.close();
//
//        }
//        catch (SQLException e)
//        {
//            System.out.println("Failed to load users from SQL: " + e.getMessage());
//        }
//        return events;
//    }
//
//
//    private Collection<Seat> getSeats(int VenueId,int SeatId)
//    {
//        Collection<Seat> seats = new ArrayList<>();
//
//        try {
//            Statement stmt = connection.createStatement();
//            ResultSet resultSetSeat = stmt.executeQuery( "Select * from seat;");
//            int seatId,rs,cs;
//
//            do
//            {
//
//
//                try
//                {
//                    ResultSet resultSetUser = stmt.executeQuery( "Select * from user where id = seatId;");
//                }
//                catch(SQLException e)
//                {
//                    throw new RuntimeException(e);
//                }
//
//            }while(resultSetSeat.next());
//            stmt.close();
//
//        }
//        catch (SQLException e)
//        {
//            throw new RuntimeException(e);
//        }
//        return seats;
//    }



