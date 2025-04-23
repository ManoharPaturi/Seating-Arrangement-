package srms;

import java.util.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
//import jakarta.persistence.*;


//@Entity
//@Inheritance(strategy = InheritanceType.JOINED)
public class User {
//    @Id
    protected String id;

    protected String password;

//    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    protected Collection<Seat> reservedSeats = new LinkedList<>();

//    public User() {}

    public User(String id, String password)
    {
        this.id = id;
        this.password = hashPassword(password);

    }

    public String getId()
    {
        return id;
    }
    public Collection<Seat> getReservedSeats() {return reservedSeats;}

    public boolean checkPassword(String password)
    {
        return hashPassword(this.password).equals(hashPassword(password));
    }

    public String changePassword(String Password,String newPassword)
    {
        String hashedNewPassword = hashPassword(newPassword);
        if(password.equals(hashPassword(Password)))
        {
            if(this.password.equals(hashedNewPassword))
            {
                return "new password is equal to old Password";
            }

            this.password = hashedNewPassword;
        }
        return "password incorrect";
    }



    private static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashedBytes);
        } catch (NoSuchAlgorithmException e) {

            throw new RuntimeException("Hashing algorithm not available", e);

        }

    }

    public String getPassword(Sql sql)
    {
        return password;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        return map;
}
}




