package srms;


import java.util.Map;

public class Student extends User {
    private String name;
    private int semester;
    private int year;
    private String course;
    private String section;
    private static int count = 0;


    public Student(String rollNo)
    {
        super(rollNo, rollNo);
        count++;

    }

    public  Student(String rollNo, String name, int semester, int year, String course, String section)
    {
        super(rollNo, rollNo);
        this.name = name;
        this.semester = semester;
        this.year = year;
        this.course = course;
        this.section = section;
        count++;
    }


    public String getName() {return name;}
    public int getSemester() {return semester;}
    public int getYear() {return year;}
    public String getCourse() {return course;}
    public static int getCount() {return count;}
    public String getSection() {return section;}




    public String reserveRequest(Seat seat)
    {
        if(seat.Reserve(this))
        {
            reservedSeats.add(seat);
            return "Reserved Successfully";
        }
        return "Reservation Failed";
    }


    public String cancelRequest(Seat seat)
    {
        if(seat.cancel(this))
        {
            reservedSeats.remove(seat);
            return "Successfully cancelled seat";
        }
        return "Sorry, but seat is not reserved/or not reserved by you";
    }

    public String getReservedSeatsInfo() {
        StringBuilder sb = new StringBuilder();
        for (Seat seat : reservedSeats) {
            sb.append("Event: ").append(seat.getEvent().getTitle())
                    .append(", Venue: ").append(seat.getVenue().getName())
                    .append(", Seat: (").append(seat.getRow()).append(",").append(seat.getCol()).append(")\n");
        }
        return sb.length() > 0 ? sb.toString() : "No seats reserved.";
}


    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = super.toMap();
        map.put("name", name);
        map.put("semester", semester);
        map.put("year", year);
        map.put("course", course);
        map.put("section", section);
        return map;
}
}

