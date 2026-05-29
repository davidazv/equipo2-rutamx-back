package org.acme.interfaces.rest;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.annotations.providers.multipart.PartType;

import java.io.InputStream;

public class UploadForm {

    @FormParam("file")
    @PartType(MediaType.APPLICATION_OCTET_STREAM)
    public InputStream file;

    // Blocks path traversal (../) and shell/SQL metacharacters in filename.
    @FormParam("fileName")
    @PartType(MediaType.TEXT_PLAIN)
    @Size(max = 255)
    @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "fileName con caracteres no permitidos")
    public String fileName;
}
