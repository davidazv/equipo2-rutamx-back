package org.acme.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Nueva contraseña para restablecer la cuenta del usuario")
public class ResetPasswordDto {

    @Schema(description = "Nueva contraseña (mín. 8 chars, al menos 1 mayúscula y 1 dígito)", example = "Nueva@Segura1", required = true)
    @NotBlank
    @Size(min = 8, max = 64)
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d).+$",
             message = "La contraseña debe tener al menos una mayúscula y un dígito")
    private String newPassword;

    public ResetPasswordDto() {
        // intentionally empty
    }

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}
