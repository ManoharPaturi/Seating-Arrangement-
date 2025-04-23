package srms;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class SrmsController {
    private final Admin admin = Admin.getAdmin();
    private final Sql sql = Sql.connection();

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> credentials) {
        String userId = credentials.get("userId");
        String password = credentials.get("password");
        Map<String, Object> response = new HashMap<>();

        User user = null;
        if (sql.isConnected()) {
            user = sql.loadUsers().stream()
                    .filter(u -> u.getId().equals(userId) && u.checkPassword(password))
                    .findFirst()
                    .orElse(null);
        }
        if (user == null) {
            user = admin.getUsers().stream()
                    .filter(u -> u.getId().equals(userId) && u.checkPassword(password))
                    .findFirst()
                    .orElse(null);
        }
        if (user == null && userId.equals("admin") && password.equals("admin123")) {
            user = admin.getUsers().stream()
                    .filter(u -> u.getId().equals("admin"))
                    .findFirst()
                    .orElse(null);
        }

        if (user != null) {
            String userType = user instanceof Student ? "student" : user instanceof Faculty ? "faculty" : "admin";
            response.put("token", "jwt-" + userId); // Dummy JWT
            response.put("userType", userType);
            response.put("user", user.toMap());
            return ResponseEntity.ok(response);
        }
        response.put("message", "Invalid ID or Password!");
        return ResponseEntity.badRequest().body(response);
    }

    @GetMapping("/events")
    public List<Map<String, Object>> getEvents(@RequestHeader("Authorization") String token) {
        validateToken(token);
        return admin.getEvents().stream().map(Event::toMap).collect(Collectors.toList());
    }

    @GetMapping("/events/{eventId}/seats")
    public List<Map<String, Object>> getSeats(@PathVariable int eventId, @RequestHeader("Authorization") String token) {
        validateToken(token);
        Event event = admin.getEvents().stream()
                .filter(e -> e.getId() == eventId)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Event not found"));
        return event.getSeats().stream().map(Seat::toMap).collect(Collectors.toList());
    }

    @PostMapping("/seats/{seatId}/reserve")
    public Map<String, String> reserveSeat(@PathVariable int seatId, @RequestHeader("Authorization") String token) {
        String userId = validateToken(token);
        Student student = (Student) admin.getUsers().stream()
                .filter(u -> u.getId().equals(userId) && u instanceof Student)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Student not found"));
        Seat seat = admin.getEvents().stream()
                .flatMap(e -> e.getSeats().stream())
                .filter(s -> s.getId() == seatId)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Seat not found"));
        String result = student.reserveRequest(seat);
        return Map.of("message", result);
    }

    @GetMapping("/students/reserved-seats")
    public List<Map<String, Object>> getReservedSeats(@RequestHeader("Authorization") String token) {
        String userId = validateToken(token);
        Student student = (Student) admin.getUsers().stream()
                .filter(u -> u.getId().equals(userId) && u instanceof Student)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Student not found"));
        return student.getReservedSeats().stream().map(seat -> {
            Map<String, Object> map = seat.toMap();
            map.put("event", seat.getEvent().toMap());
            map.put("venue", seat.getVenue().toMap());
            return map;
        }).collect(Collectors.toList());
    }

    @GetMapping("/faculties/events")
    public List<Map<String, Object>> getFacultyEvents(@RequestHeader("Authorization") String token) {
        String userId = validateToken(token);
        Faculty faculty = (Faculty) admin.getUsers().stream()
                .filter(u -> u.getId().equals(userId) && u instanceof Faculty)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Faculty not found"));
        return faculty.getEvents().stream().map(Event::toMap).collect(Collectors.toList());
    }

    @PostMapping("/events")
    public Map<String, String> createEvent(@RequestBody Map<String, Object> eventData, @RequestHeader("Authorization") String token) {
        String userId = validateToken(token);
        Faculty faculty = (Faculty) admin.getUsers().stream()
                .filter(u -> u.getId().equals(userId) && u instanceof Faculty)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Faculty not found"));
        try {
            int venueId = ((Number) eventData.get("venueId")).intValue();
            Venue venue = admin.getVenues().stream()
                    .filter(v -> v.getVenueId() == venueId)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Venue not found"));
            String title = (String) eventData.get("title");
            String description = (String) eventData.get("description");
            LocalDateTime start = LocalDateTime.parse((String) eventData.get("start"));
            LocalDateTime end = LocalDateTime.parse((String) eventData.get("end"));
            String result = faculty.createEvent(venue, start, end, title, description);
            return Map.of("message", result);
        } catch (Exception e) {
            return Map.of("message", "Failed to create event: " + e.getMessage());
        }
    }

    @DeleteMapping("/events/{eventId}")
    public Map<String, String> cancelEvent(@PathVariable int eventId, @RequestHeader("Authorization") String token) {
        String userId = validateToken(token);
        Faculty faculty = (Faculty) admin.getUsers().stream()
                .filter(u -> u.getId().equals(userId) && u instanceof Faculty)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Faculty not found"));
        Event event = admin.getEvents().stream()
                .filter(e -> e.getId() == eventId)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Event not found"));
        String result = faculty.cancelEvent(event);
        return Map.of("message", result);
    }

    @GetMapping("/admin/overview")
    public Map<String, List<Map<String, Object>>> getAdminOverview(@RequestHeader("Authorization") String token) {
        validateToken(token);
        return Map.of(
                "users", admin.getUsers().stream().map(User::toMap).collect(Collectors.toList()),
                "venues", admin.getVenues().stream().map(Venue::toMap).collect(Collectors.toList()),
                "events", admin.getEvents().stream().map(Event::toMap).collect(Collectors.toList())
        );
    }

    private String validateToken(String token) {
        if (!token.startsWith("Bearer jwt-")) {
            throw new RuntimeException("Invalid token");
        }
        return token.replace("Bearer jwt-", "");
    }
}