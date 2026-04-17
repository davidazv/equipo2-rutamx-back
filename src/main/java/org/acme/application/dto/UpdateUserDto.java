package org.acme.application.dto;

public class UpdateUserDto {

    private String firstName;
    private String lastName;
    private Long roleId;

    public UpdateUserDto() {}

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public Long getRoleId() { return roleId; }
    public void setRoleId(Long roleId) { this.roleId = roleId; }
}
