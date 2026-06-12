package org.acme.application.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Campos actualizables de un usuario (todos opcionales)")
public class UpdateUserDto {

    @Schema(description = "Nuevo nombre(s)", example = "Carlos")
    @Size(max = 100)
    @Pattern(regexp = "^[\\p{L} .'-]*$", message = "firstName con caracteres no permitidos")
    private String firstName;

    @Schema(description = "Nuevo apellido(s)", example = "López")
    @Size(max = 100)
    @Pattern(regexp = "^[\\p{L} .'-]*$", message = "lastName con caracteres no permitidos")
    private String lastName;

    @Schema(description = "Nuevo ID de rol", example = "2")
    @Positive
    private Long roleId;

    public UpdateUserDto() {
        // intentionally empty
    }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public Long getRoleId() { return roleId; }
    public void setRoleId(Long roleId) { this.roleId = roleId; }
}
