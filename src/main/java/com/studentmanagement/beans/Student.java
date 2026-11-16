package com.studentmanagement.beans;

import java.util.UUID;

public class Student {

    private int id;
    private Integer userId;
    private String uid = UUID.randomUUID().toString();
    private String name;
    private String email;
    private String phone;
    private String department;
    private double cgpa;
    private String semester;
    private String username;
    private String password;
    private User user;

    public Student() {
    }

    public Student(String name, String email, String phone, String department, double cgpa) {
        this(0, null, UUID.randomUUID().toString(), name, email, phone, department, cgpa, null, null, null);
    }

    public Student(int id, String uid, String name, String email, String phone, String department, double cgpa) {
        this(id, null, uid, name, email, phone, department, cgpa, null, null, null);
    }

    public Student(int id, Integer userId, String uid, String name, String email, String phone, String department, double cgpa, String semester) {
        this(id, userId, uid, name, email, phone, department, cgpa, semester, null, null);
    }

    public Student(int id, String uid, String name, String email, String phone, String department, double cgpa, String username, String password) {
        this(id, null, uid, name, email, phone, department, cgpa, null, username, password);
    }

    public Student(int id, String uid, String name, String email, String phone, String department, double cgpa, String semester, String username, String password) {
        this(id, null, uid, name, email, phone, department, cgpa, semester, username, password);
    }

    public Student(int id, Integer userId, String uid, String name, String email, String phone, String department, double cgpa, String semester, String username, String password) {
        this.id = (id > 0) ? id : (userId != null && userId > 0 ? userId : 0);
        this.userId = this.id > 0 ? this.id : null;
        this.uid = (uid != null && !uid.trim().isEmpty()) ? uid.trim() : UUID.randomUUID().toString();
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.department = department;
        this.cgpa = cgpa;
        this.semester = semester;
        this.username = username;
        this.password = password;
        if (this.id > 0 && username != null && !username.trim().isEmpty()) {
            this.user = new User(this.id, username, password, 2, 1);
            this.user.setStudent(this);
        }
    }

    public Student(int id, String uid, String name, String email, String phone, String department, double cgpa, User user) {
        this(id, user != null && user.getId() > 0 ? user.getId() : (id > 0 ? id : null), uid, name, email, phone, department, cgpa, null, user != null ? user.getUsername() : null, user != null ? user.getPassword() : null);
        setUser(user);
    }

    public int getId() {
        if (id > 0) return id;
        if (user != null && user.getId() > 0) return user.getId();
        if (userId != null && userId.intValue() > 0) return userId.intValue();
        return id;
    }

    public void setId(int id) {
        this.id = id;
        this.userId = id > 0 ? id : null;
        if (user != null && id > 0 && user.getId() != id) {
            user.setId(id);
        }
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public double getCgpa() {
        return cgpa;
    }

    public void setCgpa(double cgpa) {
        this.cgpa = cgpa;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public Integer getUserId() {
        int curId = getId();
        return curId > 0 ? Integer.valueOf(curId) : null;
    }

    public void setUserId(Integer userId) {
        if (userId != null && userId.intValue() > 0) {
            setId(userId.intValue());
        }
    }

    public String getUsername() {
        if (user != null && user.getUsername() != null && !user.getUsername().trim().isEmpty()) {
            return user.getUsername();
        }
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
        if (user != null) {
            user.setUsername(username);
        }
    }

    public String getPassword() {
        if (user != null && user.getPassword() != null && !user.getPassword().isEmpty()) {
            return user.getPassword();
        }
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
        if (user != null) {
            user.setPassword(password);
        }
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
        if (user != null) {
            if (user.getId() > 0) {
                this.userId = user.getId();
            }
            if (user.getUsername() != null) {
                this.username = user.getUsername();
            }
            if (user.getPassword() != null) {
                this.password = user.getPassword();
            }
            if (user.getStudent() != this) {
                user.setStudent(this);
            }
        }
    }

    @Override
    public String toString() {
        return "Student [id=" + id + ", userId=" + userId + ", uid=" + uid + ", name=" + name + ", email=" + email
                + ", phone=" + phone + ", department=" + department + ", cgpa=" + cgpa 
                + ", semester=" + semester + ", username=" + getUsername() + "]";
    }
}
