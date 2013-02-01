package fi.vm.sade.auth.ldap;

import java.util.Arrays;

public class User {

    private static final long serialVersionUID = 7487133273442955818L;

    private String userName;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String department;
    private String groups[] = new String[0];
//    private String roles[] = new String[0];

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String[] getGroups() {
        return groups;
    }

    public void setGroups(String[] groups) {
        this.groups = groups;
    }

//    public String[] getRoles() {
//        return roles;
//    }

//    public void setRoles(String[] roles) {
//        this.roles = roles;
//    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("UserImpl[");
//        buffer.append("id = ").append(id);
        buffer.append(" userName = ").append(userName);
        buffer.append(" email = ").append(email);
        buffer.append(" firstName = ").append(firstName);
        buffer.append(" lastName = ").append(lastName);
        buffer.append(" password = ").append(password);
        buffer.append(" roles = ").append(Arrays.toString(groups));
        buffer.append("]");
        return buffer.toString();
    }
}
