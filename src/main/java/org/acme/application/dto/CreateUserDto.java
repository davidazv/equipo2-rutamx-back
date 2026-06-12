package org.acme.application.dto;

import jakarta.validation.constraints.*;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Datos requeridos para crear un nuevo usuario en el sistema")
public class CreateUserDto {

    @Schema(description = "Nombre(s) del usuario", example = "Juan", required = true)
    @NotBlank
    private String firstName;

    @Schema(description = "Apellido(s) del usuario", example = "Pérez", required = true)
    @NotBlank
    private String lastName;

    @Schema(description = "Correo electrónico único del usuario", example = "juan.perez@empresa.mx", required = true)
    @NotBlank
    @Email
    private String email;

    @Schema(description = "Contraseña (mín. 8 chars, al menos 1 mayúscula y 1 dígito)", example = "Segura123", required = true)
    @NotBlank
    @Size(min = 8, max = 64)
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d).+$",
             message = "La contraseña debe tener al menos una mayúscula y un dígito")
    private String password;

    @Schema(description = "ID del rol asignado al usuario (ver /admin/roles)", example = "1", required = true)
    @NotNull
    private Long roleId;

    public CreateUserDto() {
        // intentionally empty
    }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Long getRoleId() { return roleId; }
    public void setRoleId(Long roleId) { this.roleId = roleId; }
}
