package ch.uzh.ifi.hase.soprafs24.models.user;

public class UserUpdate {
    private String name;
    private String username;
    private String birthday;
    private String email;
    private String password;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getBirthday() { return birthday; }
    public void setBirthday(String birthday) { this.birthday = birthday; }

    public String getEmail() { return email;}
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

}
