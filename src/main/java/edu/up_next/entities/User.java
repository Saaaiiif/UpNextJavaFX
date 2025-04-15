package edu.up_next.entities;
import java.util.List;
public class User {


    private int id;
    private String email;
    private String roles;  // Use List<String> to store roles
    private String password;
    private String firstname;
    private String lastname;
    private String speciality;
    private String description;
    private String image;

    private int num;
    private boolean is_active;
    private boolean is_verified;

    public User(int id, String email, String roles, String s, String firstname, String lastname, String speciality) {
        this.id = id;
        this.email = email;
        this.roles = roles;
        this.password = s;
        this.firstname = firstname;
        this.lastname = lastname;
        this.speciality = speciality;

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getSpeciality() {
        return speciality;
    }

    public void setSpeciality(String speciality) {
        this.speciality = speciality;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public boolean isIs_verified() {
        return is_verified;
    }

    public void setIs_verified(boolean is_verified) {
        this.is_verified = is_verified;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public boolean isIs_active() {
        return is_active;
    }

    public void setIs_active(boolean is_active) {
        this.is_active = is_active;
    }

    public User() {
    }

    public User(int id, String email, String roles, String password, String firstname, String lastname, String speciality, String description, String image, boolean is_verified, int num, boolean is_active) {
        this.id = id;
        this.email = email;
        this.roles = roles;
        this.password = password;
        this.firstname = firstname;
        this.lastname = lastname;
        this.speciality = speciality;
        this.description = description;
        this.image = image;
        this.is_verified = is_verified;
        this.num = num;
        this.is_active = is_active;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email=" + email +
                ", roles='" + roles + '\'' +
                ", password='" + password + '\'' +
                ", firstname='" + firstname + '\'' +
                ", lastname='" + lastname + '\'' +
                ", speciality='" + speciality + '\'' +
                ", description='" + description + '\'' +
                ", image='" + image + '\'' +
                ", is_verified=" + is_verified +
                ", num=" + num +
                ", is_active=" + is_active +
                '}';
    }
}
