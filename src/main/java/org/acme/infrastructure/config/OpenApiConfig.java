package org.acme.infrastructure.config;

import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;

import jakarta.ws.rs.core.Application;

@OpenAPIDefinition(
    info = @Info(
        title = "RutaMx API",
        version = "1.0.0",
        description = """
            API para simulación y monitoreo de flotilla de autobuses eléctricos en la CDMX.

            ## Roles y acceso
            | Rol   | Descripción |
            |-------|-------------|
            | ADMIN | Acceso total — usuarios, carga de datos, catálogo de buses y toda la analítica |
            | CEO   | Analítica estratégica: KPIs, reporte comparativo, ROI |
            | COO   | Operaciones: KPIs, tiempos de viaje, rutas |
            | CMO   | Marketing: estadísticas por ruta, campañas ambientales, reporte comparativo |
            """,
        contact = @Contact(name = "Equipo RutaMx")
    )
)
public class OpenApiConfig extends Application {
}
