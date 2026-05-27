package org.acme.application.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Campos actualizables de un usuario (todos opcionales)")
public class UpdateUserDto {

    @Schema(description = "Nuevo nombre(s)", example = "Carlos")
    private String firstName;

    @Schema(description = "Nuevo apellido(s)", example = "López")
    private String lastName;

    @Schema(description = "Nuevo ID de rol", example = "2")
    private Long roleId;

    public UpdateUserDto() {}

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public Long getRoleId() { return roleId; }
    public void setRoleId(Long roleId) { this.roleId = roleId; }
}
