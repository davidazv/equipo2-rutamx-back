package org.acme.interfaces.rest;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.application.usecase.*;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;

import jakarta.transaction.Transactional;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

@Path("/admin/upload")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Carga de Datos (CSV)", description = "Importación de archivos CSV con datos GTFS y de afluencia. Página: Admin Panel > pestaña Carga de Datos. **Roles:** ADMIN")
public class UploadResource {

    private static final Logger log = Logger.getLogger(UploadResource.class.getName());

    private static final String ERROR_INESPERADO = "Error al procesar el archivo";

    private static final String KEY_AGENCY      = "agency";
    private static final String KEY_CALENDAR    = "calendar";
    private static final String KEY_STOPS       = "stops";
    private static final String KEY_BUS_MODELS  = "bus-models";
    private static final String KEY_SHAPES      = "shapes";
    private static final String KEY_AFLUENCIA   = "afluencia";
    private static final String KEY_ROUTES      = "routes";
    private static final String KEY_TRIPS       = "trips";
    private static final String KEY_STOP_TIMES  = "stop-times";
    private static final String KEY_FREQUENCIES = "frequencies";

    // Whitelist of physical table names countTable() may query. Prevents SQL
    // injection via dynamic table name interpolation in native queries.
    private static final Set<String> ALLOWED_COUNT_TABLES = Set.of(
            KEY_AGENCY, KEY_CALENDAR, KEY_STOPS, "bus_models", KEY_SHAPES,
            "afluencia_metrobus", KEY_ROUTES, KEY_TRIPS, "stop_times", KEY_FREQUENCIES);

    // Whitelist of logical keys executeUpload() may record in upload_metadata.
    private static final Set<String> ALLOWED_UPLOAD_KEYS = Set.of(
            KEY_AGENCY, KEY_CALENDAR, KEY_STOPS, KEY_BUS_MODELS, KEY_SHAPES,
            KEY_AFLUENCIA, KEY_ROUTES, KEY_TRIPS, KEY_STOP_TIMES, KEY_FREQUENCIES);

    private final EntityManager entityManager;
    private final ImportAgencyUseCase importAgencyUseCase;
    private final ImportBusModelUseCase importBusModelUseCase;
    private final ImportCalendarUseCase importCalendarUseCase;
    private final ImportRouteUseCase importRouteUseCase;
    private final ImportStopUseCase importStopUseCase;
    private final ImportTripUseCase importTripUseCase;
    private final ImportStopTimeUseCase importStopTimeUseCase;
    private final ImportShapeUseCase importShapeUseCase;
    private final ImportFrequencyUseCase importFrequencyUseCase;
    private final ImportAfluenciaUseCase importAfluenciaUseCase;

    @Inject
    public UploadResource(
            EntityManager entityManager,
            ImportAgencyUseCase importAgencyUseCase,
            ImportBusModelUseCase importBusModelUseCase,
            ImportCalendarUseCase importCalendarUseCase,
            ImportRouteUseCase importRouteUseCase,
            ImportStopUseCase importStopUseCase,
            ImportTripUseCase importTripUseCase,
            ImportStopTimeUseCase importStopTimeUseCase,
            ImportShapeUseCase importShapeUseCase,
            ImportFrequencyUseCase importFrequencyUseCase,
            ImportAfluenciaUseCase importAfluenciaUseCase) {
        this.entityManager = entityManager;
        this.importAgencyUseCase = importAgencyUseCase;
        this.importBusModelUseCase = importBusModelUseCase;
        this.importCalendarUseCase = importCalendarUseCase;
        this.importRouteUseCase = importRouteUseCase;
        this.importStopUseCase = importStopUseCase;
        this.importTripUseCase = importTripUseCase;
        this.importStopTimeUseCase = importStopTimeUseCase;
        this.importShapeUseCase = importShapeUseCase;
        this.importFrequencyUseCase = importFrequencyUseCase;
        this.importAfluenciaUseCase = importAfluenciaUseCase;
    }

    @POST
    @Path("/agency")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Transactional
    @Operation(summary = "Importar agencias desde CSV (GTFS agency.txt)", description = "Página: Admin Panel > Carga de Datos. **Roles:** ADMIN")
    @APIResponse(responseCode = "200", description = "Importación exitosa con resumen de filas procesadas")
    @APIResponse(responseCode = "400", description = "Archivo no proporcionado")
    @APIResponse(responseCode = "500", description = ERROR_INESPERADO)
    @RequestBody(description = "Archivo CSV con datos de agencias",
        content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA,
            schema = @Schema(implementation = UploadForm.class)))
    public Response uploadAgency(@MultipartForm UploadForm form) {
        return executeUpload(KEY_AGENCY, form, importAgencyUseCase::execute);
    }

    @POST
    @Path("/bus-models")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Transactional
    @Operation(summary = "Importar modelos de bus desde CSV", description = "Página: Admin Panel > Carga de Datos. **Roles:** ADMIN")
    @APIResponse(responseCode = "200", description = "Importación exitosa")
    @APIResponse(responseCode = "400", description = "Archivo no proporcionado")
    @APIResponse(responseCode = "500", description = ERROR_INESPERADO)
    @RequestBody(description = "Archivo CSV con datos de modelos de bus",
        content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA,
            schema = @Schema(implementation = UploadForm.class)))
    public Response uploadBusModels(@MultipartForm UploadForm form) {
        return executeUpload(KEY_BUS_MODELS, form, importBusModelUseCase::execute);
    }

    @POST
    @Path("/calendar")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Transactional
    @Operation(summary = "Importar calendario desde CSV (GTFS calendar.txt)", description = "Página: Admin Panel > Carga de Datos. **Roles:** ADMIN")
    @APIResponse(responseCode = "200", description = "Importación exitosa")
    @APIResponse(responseCode = "400", description = "Archivo no proporcionado")
    @APIResponse(responseCode = "500", description = ERROR_INESPERADO)
    @RequestBody(description = "Archivo CSV con datos de calendario GTFS",
        content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA,
            schema = @Schema(implementation = UploadForm.class)))
    public Response uploadCalendar(@MultipartForm UploadForm form) {
        return executeUpload(KEY_CALENDAR, form, importCalendarUseCase::execute);
    }

    @POST
    @Path("/routes")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Transactional
    @Operation(summary = "Importar rutas desde CSV (GTFS routes.txt)", description = "Página: Admin Panel > Carga de Datos. **Roles:** ADMIN")
    @APIResponse(responseCode = "200", description = "Importación exitosa")
    @APIResponse(responseCode = "400", description = "Archivo no proporcionado")
    @APIResponse(responseCode = "500", description = ERROR_INESPERADO)
    @RequestBody(description = "Archivo CSV con datos de rutas GTFS",
        content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA,
            schema = @Schema(implementation = UploadForm.class)))
    public Response uploadRoutes(@MultipartForm UploadForm form) {
        return executeUpload(KEY_ROUTES, form, importRouteUseCase::execute);
    }

    @POST
    @Path("/stops")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Transactional
    @Operation(summary = "Importar paradas desde CSV (GTFS stops.txt)", description = "Página: Admin Panel > Carga de Datos. **Roles:** ADMIN")
    @APIResponse(responseCode = "200", description = "Importación exitosa")
    @APIResponse(responseCode = "400", description = "Archivo no proporcionado")
    @APIResponse(responseCode = "500", description = ERROR_INESPERADO)
    @RequestBody(description = "Archivo CSV con datos de paradas GTFS",
        content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA,
            schema = @Schema(implementation = UploadForm.class)))
    public Response uploadStops(@MultipartForm UploadForm form) {
        return executeUpload(KEY_STOPS, form, importStopUseCase::execute);
    }

    @POST
    @Path("/trips")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Transactional
    @Operation(summary = "Importar viajes desde CSV (GTFS trips.txt)", description = "Página: Admin Panel > Carga de Datos. **Roles:** ADMIN")
    @APIResponse(responseCode = "200", description = "Importación exitosa")
    @APIResponse(responseCode = "400", description = "Archivo no proporcionado")
    @APIResponse(responseCode = "500", description = ERROR_INESPERADO)
    @RequestBody(description = "Archivo CSV con datos de viajes GTFS",
        content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA,
            schema = @Schema(implementation = UploadForm.class)))
    public Response uploadTrips(@MultipartForm UploadForm form) {
        return executeUpload(KEY_TRIPS, form, importTripUseCase::execute);
    }

    @POST
    @Path("/stop-times")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Transactional
    @Operation(summary = "Importar horarios de paradas desde CSV (GTFS stop_times.txt)", description = "Página: Admin Panel > Carga de Datos. **Roles:** ADMIN")
    @APIResponse(responseCode = "200", description = "Importación exitosa")
    @APIResponse(responseCode = "400", description = "Archivo no proporcionado")
    @APIResponse(responseCode = "500", description = ERROR_INESPERADO)
    @RequestBody(description = "Archivo CSV con stop_times GTFS",
        content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA,
            schema = @Schema(implementation = UploadForm.class)))
    public Response uploadStopTimes(@MultipartForm UploadForm form) {
        return executeUpload(KEY_STOP_TIMES, form, importStopTimeUseCase::execute);
    }

    @POST
    @Path("/shapes")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Transactional
    @Operation(summary = "Importar trazados geográficos desde CSV (GTFS shapes.txt)", description = "Página: Admin Panel > Carga de Datos. **Roles:** ADMIN")
    @APIResponse(responseCode = "200", description = "Importación exitosa")
    @APIResponse(responseCode = "400", description = "Archivo no proporcionado")
    @APIResponse(responseCode = "500", description = ERROR_INESPERADO)
    @RequestBody(description = "Archivo CSV con shapes GTFS",
        content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA,
            schema = @Schema(implementation = UploadForm.class)))
    public Response uploadShapes(@MultipartForm UploadForm form) {
        return executeUpload(KEY_SHAPES, form, importShapeUseCase::execute);
    }

    @POST
    @Path("/frequencies")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Transactional
    @Operation(summary = "Importar frecuencias desde CSV (GTFS frequencies.txt)", description = "Página: Admin Panel > Carga de Datos. **Roles:** ADMIN")
    @APIResponse(responseCode = "200", description = "Importación exitosa")
    @APIResponse(responseCode = "400", description = "Archivo no proporcionado")
    @APIResponse(responseCode = "500", description = ERROR_INESPERADO)
    @RequestBody(description = "Archivo CSV con frecuencias GTFS",
        content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA,
            schema = @Schema(implementation = UploadForm.class)))
    public Response uploadFrequencies(@MultipartForm UploadForm form) {
        return executeUpload(KEY_FREQUENCIES, form, importFrequencyUseCase::execute);
    }

    @POST
    @Path("/afluencia")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Transactional
    @Operation(summary = "Importar datos de afluencia de pasajeros desde CSV", description = "Página: Admin Panel > Carga de Datos. **Roles:** ADMIN")
    @APIResponse(responseCode = "200", description = "Importación exitosa")
    @APIResponse(responseCode = "400", description = "Archivo no proporcionado")
    @APIResponse(responseCode = "500", description = ERROR_INESPERADO)
    @RequestBody(description = "Archivo CSV con datos de afluencia Metrobús",
        content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA,
            schema = @Schema(implementation = UploadForm.class)))
    public Response uploadAfluencia(@MultipartForm UploadForm form) {
        return executeUpload(KEY_AFLUENCIA, form, importAfluenciaUseCase::execute);
    }

    @GET
    @Path("/status")
    @Operation(summary = "Estado de carga de tablas",
        description = "Devuelve el conteo de filas y la fecha de última importación de cada tabla. Página: Admin Panel > Carga de Datos. **Roles:** ADMIN")
    @APIResponse(responseCode = "200", description = "Estado actual de cada tabla de datos")
    public Response getTableStatus() {
        String[] keys = {KEY_AGENCY, KEY_CALENDAR, KEY_STOPS, KEY_BUS_MODELS, KEY_SHAPES,
                KEY_AFLUENCIA, KEY_ROUTES, KEY_TRIPS, KEY_STOP_TIMES, KEY_FREQUENCIES};
        String[] dbTables = {KEY_AGENCY, KEY_CALENDAR, KEY_STOPS, "bus_models", KEY_SHAPES,
                "afluencia_metrobus", KEY_ROUTES, KEY_TRIPS, "stop_times", KEY_FREQUENCIES};

        Map<String, String> timestamps = loadTimestamps();

        Map<String, Map<String, Object>> result = new LinkedHashMap<>();
        for (int i = 0; i < keys.length; i++) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("rowCount", countTable(dbTables[i]));
            entry.put("uploadedAt", timestamps.get(keys[i]));
            result.put(keys[i], entry);
        }
        return Response.ok(result).build();
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> loadTimestamps() {
        List<Object[]> rows = entityManager
                .createNativeQuery("SELECT table_name, uploaded_at FROM upload_metadata")
                .getResultList();
        Map<String, String> map = new LinkedHashMap<>();
        for (Object[] row : rows) {
            map.put((String) row[0], row[1].toString());
        }
        return map;
    }

    private long countTable(String tableName) {
        if (!ALLOWED_COUNT_TABLES.contains(tableName)) {
            return 0;
        }
        try {
            return ((Number) entityManager
                    .createNativeQuery("SELECT COUNT(*) FROM " + tableName)
                    .getSingleResult()).longValue();
        } catch (Exception e) {
            return 0;
        }
    }

    private Response executeUpload(String tableName, UploadForm form, ImportUseCase useCase) {
        if (!ALLOWED_UPLOAD_KEYS.contains(tableName)) {
            return Response.status(400).entity("Tabla no permitida").build();
        }
        if (form == null || form.file == null) {
            return Response.status(400).entity("Archivo requerido").build();
        }
        try {
            var result = useCase.execute(form.file);
            entityManager.createNativeQuery(
                    "INSERT INTO upload_metadata (table_name, uploaded_at) VALUES (?1, ?2) " +
                    "ON DUPLICATE KEY UPDATE uploaded_at = ?2")
                    .setParameter(1, tableName)
                    .setParameter(2, java.sql.Timestamp.from(Instant.now()))
                    .executeUpdate();
            return Response.ok(result).build();
        } catch (Exception e) {
            log.log(java.util.logging.Level.SEVERE, "Error uploading: {0}", e.getMessage());
            return Response.serverError().entity("Error al importar " + tableName).build();
        }
    }

    @FunctionalInterface
    private interface ImportUseCase {
        org.acme.domain.models.CsvImportResult execute(java.io.InputStream file);
    }
}
