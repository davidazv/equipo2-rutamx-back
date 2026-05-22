package org.acme.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.domain.models.User;
import org.acme.domain.repository.UserRepository;

import java.util.List;

@ApplicationScoped
public class ExportUsersUseCase {

    private final UserRepository userRepository;

    @Inject
    public ExportUsersUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String execute() {
        List<User> users = userRepository.findAll();
        StringBuilder sb = new StringBuilder();
        sb.append("nombre,correo,rol\n");
        for (User u : users) {
            String name = escape(u.getFirstName() + " " + u.getLastName());
            String email = escape(u.getEmail());
            String role = escape(u.getRoleName() != null ? u.getRoleName() : "");
            sb.append(name).append(',').append(email).append(',').append(role).append('\n');
        }
        return sb.toString();
    }

    private String escape(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
