package org.acme.interfaces.rest;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.application.usecase.*;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;

import jakarta.transaction.Transactional;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Path("/admin/upload")
@Produces(MediaType.APPLICATION_JSON)
public class UploadResource {

    private static final Logger log = Logger.getLogger(UploadResource.class.getName());

    @Inject
    EntityManager entityManager;

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
    public Response uploadAgency(@MultipartForm UploadForm form) {
        return executeUpload("agency", form, importAgencyUseCase::execute);
    }

    @POST
    @Path("/bus-models")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Transactional
    public Response uploadBusModels(@MultipartForm UploadForm form) {
        return executeUpload("bus-models", form, importBusModelUseCase::execute);
    }

    @POST
    @Path("/calendar")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Transactional
    public Response uploadCalendar(@MultipartForm UploadForm form) {
        return executeUpload("calendar", form, importCalendarUseCase::execute);
    }

    @POST
    @Path("/routes")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Transactional
    public Response uploadRoutes(@MultipartForm UploadForm form) {
        return executeUpload("routes", form, importRouteUseCase::execute);
    }

    @POST
    @Path("/stops")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Transactional
    public Response uploadStops(@MultipartForm UploadForm form) {
        return executeUpload("stops", form, importStopUseCase::execute);
    }

    @POST
    @Path("/trips")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Transactional
    public Response uploadTrips(@MultipartForm UploadForm form) {
        return executeUpload("trips", form, importTripUseCase::execute);
    }

    @POST
    @Path("/stop-times")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Transactional
    public Response uploadStopTimes(@MultipartForm UploadForm form) {
        return executeUpload("stop-times", form, importStopTimeUseCase::execute);
    }

    @POST
    @Path("/shapes")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Transactional
    public Response uploadShapes(@MultipartForm UploadForm form) {
        return executeUpload("shapes", form, importShapeUseCase::execute);
    }

    @POST
    @Path("/frequencies")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Transactional
    public Response uploadFrequencies(@MultipartForm UploadForm form) {
        return executeUpload("frequencies", form, importFrequencyUseCase::execute);
    }

    @POST
    @Path("/afluencia")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Transactional
    public Response uploadAfluencia(@MultipartForm UploadForm form) {
        return executeUpload("afluencia", form, importAfluenciaUseCase::execute);
    }

    @GET
    @Path("/status")
    public Response getTableStatus() {
        String[] keys = {"agency", "calendar", "stops", "bus-models", "shapes",
                "afluencia", "routes", "trips", "stop-times", "frequencies"};
        String[] dbTables = {"agency", "calendar", "stops", "bus_models", "shapes",
                "afluencia_metrobus", "routes", "trips", "stop_times", "frequencies"};

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
        try {
            return ((Number) entityManager
                    .createNativeQuery("SELECT COUNT(*) FROM " + tableName)
                    .getSingleResult()).longValue();
        } catch (Exception e) {
            return 0;
        }
    }

    private Response executeUpload(String tableName, UploadForm form, ImportUseCase useCase) {
        if (form.file == null) {
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
            log.severe("Error uploading " + tableName + ": " + e.getMessage());
            return Response.serverError().entity("Error al importar " + tableName + ": " + e.getMessage()).build();
        }
    }

    @FunctionalInterface
    private interface ImportUseCase {
        org.acme.domain.models.CsvImportResult execute(java.io.InputStream file);
    }
}
