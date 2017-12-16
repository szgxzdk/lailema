package ma.laile.entity;

/**
 * Created by swain on 17-12-6.
 */


import java.util.Date;

public class Student {
    private Integer id;
    private String name;
    private Date createdTime;
    private String username;
    private String password;
    private Integer classOfStudent;
    private Date lastLoginTime;
    private String token;
    private boolean valid;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Date getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(Date lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public boolean getValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public Integer getClassOfStudent() {
        return classOfStudent;
    }

    public void setClassOfStudent(Integer classOfStudent) {
        this.classOfStudent = classOfStudent;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Student(String name, Date createdTime, String username, String password, Integer classOfStudent,
                   Date lastLoginTime, String token, boolean valid) {
        super();
        this.name = name;
        this.createdTime = createdTime;
        this.username = username;
        this.password = password;
        this.classOfStudent = classOfStudent;
        this.lastLoginTime = lastLoginTime;
        this.token = token;
        this.valid = valid;
    }

    public Student() {
        super();
    }
}

