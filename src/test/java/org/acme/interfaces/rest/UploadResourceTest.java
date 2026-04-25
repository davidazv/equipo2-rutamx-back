package org.acme.interfaces.rest;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UploadResourceTest {

    // ── Agency (no FK deps) ──────────────────────────────────────────

    @Test
    @Order(1)
    void uploadAgencyShouldImportValidCsv() {
        String csv = "agency_id,agency_name,agency_url,agency_timezone,agency_lang,agency_color\n"
                + "TEST_AG,Test Agency,http://test.com,America/Mexico_City,es,FF0000\n"
                + "TEST_AG2,Agency Two,http://two.com,America/Mexico_City,es,00FF00\n";

        given()
                .multiPart("file", "agency.csv", csv.getBytes(), "application/octet-stream")
                .when().post("/admin/upload/agency")
                .then()
                .statusCode(200)
                .body("tableName", equalTo("agency"))
                .body("totalRows", equalTo(2))
                .body("importedRows", equalTo(2))
                .body("skippedRows", equalTo(0))
                .body("errors", empty());
    }

    @Test
    @Order(2)
    void uploadAgencyShouldReturn400WhenFileMissing() {
        given()
                .contentType("multipart/form-data")
                .when().post("/admin/upload/agency")
                .then()
                .statusCode(400);
    }

    @Test
    @Order(3)
    void uploadAgencyShouldReturnErrorsForWrongHeaders() {
        String csv = "wrong,headers\ndata,here\n";

        given()
                .multiPart("file", "bad.csv", csv.getBytes(), "application/octet-stream")
                .when().post("/admin/upload/agency")
                .then()
                .statusCode(200)
                .body("importedRows", equalTo(0))
                .body("errors.size()", greaterThan(0))
                .body("errors[0]", containsString("Encabezados incorrectos"));
    }

    @Test
    @Order(4)
    void uploadAgencyShouldHandleEmptyCsvBody() {
        String csv = "agency_id,agency_name,agency_url,agency_timezone,agency_lang,agency_color\n";

        given()
                .multiPart("file", "empty.csv", csv.getBytes(), "application/octet-stream")
                .when().post("/admin/upload/agency")
                .then()
                .statusCode(200)
                .body("totalRows", equalTo(0))
                .body("importedRows", equalTo(0));
    }

    // ── Bus Models (no FK deps) ──────────────────────────────────────

    @Test
    @Order(10)
    void uploadBusModelsShouldImportValidCsv() {
        String csv = "name,manufacturer,fuel_type,autonomy_km,passenger_capacity,unit_cost_usd,"
                + "battery_capacity_kwh,energy_consumption_kwh_km,fuel_consumption_l_km,"
                + "maintenance_cost_per_km,co2_emissions_g_km\n"
                + "TestBus,Yutong,ELECTRIC,300,85,420000,352.08,1.0,0.0,0.12,0\n";

        given()
                .multiPart("file", "bus_models.csv", csv.getBytes(), "application/octet-stream")
                .when().post("/admin/upload/bus-models")
                .then()
                .statusCode(200)
                .body("tableName", equalTo("bus_model"))
                .body("importedRows", equalTo(1))
                .body("errors", empty());
    }

    @Test
    @Order(11)
    void uploadBusModelsShouldReturnErrorsForWrongHeaders() {
        String csv = "bad_header\ndata\n";

        given()
                .multiPart("file", "bad.csv", csv.getBytes(), "application/octet-stream")
                .when().post("/admin/upload/bus-models")
                .then()
                .statusCode(200)
                .body("errors[0]", containsString("Encabezados incorrectos"));
    }

    // ── Calendar (no FK deps) ────────────────────────────────────────

    @Test
    @Order(20)
    void uploadCalendarShouldImportValidCsv() {
        String csv = "service_id,monday,tuesday,wednesday,thursday,friday,saturday,sunday,start_date,end_date\n"
                + "SVC_TEST,1,1,1,1,1,0,0,20260101,20261231\n";

        given()
                .multiPart("file", "calendar.csv", csv.getBytes(), "application/octet-stream")
                .when().post("/admin/upload/calendar")
                .then()
                .statusCode(200)
                .body("tableName", equalTo("calendar"))
                .body("importedRows", equalTo(1))
                .body("errors", empty());
    }

    @Test
    @Order(21)
    void uploadCalendarShouldReturnErrorsForWrongHeaders() {
        String csv = "bad\ndata\n";

        given()
                .multiPart("file", "bad.csv", csv.getBytes(), "application/octet-stream")
                .when().post("/admin/upload/calendar")
                .then()
                .statusCode(200)
                .body("errors[0]", containsString("Encabezados incorrectos"));
    }

    // ── Stops (no FK deps on own table) ──────────────────────────────

    @Test
    @Order(30)
    void uploadStopsShouldImportValidCsv() {
        String csv = "stop_id,stop_name,stop_lat,stop_lon,zone_id,wheelchair_boarding\n"
                + "STOP_T1,Parada Test,19.345718,-99.065139,Z1,1\n";

        given()
                .multiPart("file", "stops.csv", csv.getBytes(), "application/octet-stream")
                .when().post("/admin/upload/stops")
                .then()
                .statusCode(200)
                .body("tableName", equalTo("stops"))
                .body("importedRows", equalTo(1))
                .body("errors", empty());
    }

    @Test
    @Order(31)
    void uploadStopsShouldReturnErrorsForWrongHeaders() {
        String csv = "bad\ndata\n";

        given()
                .multiPart("file", "bad.csv", csv.getBytes(), "application/octet-stream")
                .when().post("/admin/upload/stops")
                .then()
                .statusCode(200)
                .body("errors[0]", containsString("Encabezados incorrectos"));
    }

    // ── Shapes (no FK deps) ──────────────────────────────────────────

    @Test
    @Order(40)
    void uploadShapesShouldImportValidCsv() {
        String csv = "shape_id,shape_pt_lat,shape_pt_lon,shape_pt_sequence,shape_dist_traveled\n"
                + "SH_TEST,19.345718,-99.065139,1,0.0\n"
                + "SH_TEST,19.355000,-99.070000,2,5.5\n";

        given()
                .multiPart("file", "shapes.csv", csv.getBytes(), "application/octet-stream")
                .when().post("/admin/upload/shapes")
                .then()
                .statusCode(200)
                .body("tableName", equalTo("shapes"))
                .body("importedRows", equalTo(2))
                .body("errors", empty());
    }

    @Test
    @Order(41)
    void uploadShapesShouldReturnErrorsForWrongHeaders() {
        String csv = "bad\ndata\n";

        given()
                .multiPart("file", "bad.csv", csv.getBytes(), "application/octet-stream")
                .when().post("/admin/upload/shapes")
                .then()
                .statusCode(200)
                .body("errors[0]", containsString("Encabezados incorrectos"));
    }

    // ── Afluencia (no FK deps) ───────────────────────────────────────

    @Test
    @Order(50)
    void uploadAfluenciaShouldImportValidCsv() {
        String csv = "fecha,mes,anio,linea,tipo_pago,afluencia\n"
                + "2026-03-15,Marzo,2026,Linea 1,Tarjeta,15000.50\n";

        given()
                .multiPart("file", "afluencia.csv", csv.getBytes(), "application/octet-stream")
                .when().post("/admin/upload/afluencia")
                .then()
                .statusCode(200)
                .body("tableName", equalTo("afluencia_metrobus"))
                .body("importedRows", equalTo(1))
                .body("errors", empty());
    }

    @Test
    @Order(51)
    void uploadAfluenciaShouldReturnErrorsForWrongHeaders() {
        String csv = "bad\ndata\n";

        given()
                .multiPart("file", "bad.csv", csv.getBytes(), "application/octet-stream")
                .when().post("/admin/upload/afluencia")
                .then()
                .statusCode(200)
                .body("errors[0]", containsString("Encabezados incorrectos"));
    }

    // ── Routes (needs agency) ────────────────────────────────────────
    // Seed agency first, then upload routes

    @Test
    @Order(60)
    void uploadRoutesShouldImportValidCsv() {
        // Seed agency first (routes FK → agency)
        String agencyCsv = "agency_id,agency_name,agency_url,agency_timezone,agency_lang,agency_color\n"
                + "ROUTE_AG,Route Test Agency,http://test.com,America/Mexico_City,es,009B3A\n";
        given()
                .multiPart("file", "agency.csv", agencyCsv.getBytes(), "application/octet-stream")
                .when().post("/admin/upload/agency")
                .then().statusCode(200);

        String csv = "route_id,agency_id,route_short_name,route_long_name,route_type,route_color,route_text_color\n"
                + "RT1,ROUTE_AG,R1,Route One,3,FF0000,FFFFFF\n";

        given()
                .multiPart("file", "routes.csv", csv.getBytes(), "application/octet-stream")
                .when().post("/admin/upload/routes")
                .then()
                .statusCode(200)
                .body("tableName", equalTo("routes"))
                .body("importedRows", equalTo(1))
                .body("errors", empty());
    }

    @Test
    @Order(61)
    void uploadRoutesShouldReturnErrorsForWrongHeaders() {
        String csv = "bad\ndata\n";

        given()
                .multiPart("file", "bad.csv", csv.getBytes(), "application/octet-stream")
                .when().post("/admin/upload/routes")
                .then()
                .statusCode(200)
                .body("errors[0]", containsString("Encabezados incorrectos"));
    }

    // ── Trips (needs route + calendar) ───────────────────────────────

    @Test
    @Order(70)
    void uploadTripsShouldImportValidCsv() {
        // Seed parents: agency → route, calendar
        String agencyCsv = "agency_id,agency_name,agency_url,agency_timezone,agency_lang,agency_color\n"
                + "TRIP_AG,Trip Agency,http://test.com,America/Mexico_City,es,009B3A\n";
        given()
                .multiPart("file", "a.csv", agencyCsv.getBytes(), "application/octet-stream")
                .when().post("/admin/upload/agency").then().statusCode(200);

        String routeCsv = "route_id,agency_id,route_short_name,route_long_name,route_type,route_color,route_text_color\n"
                + "TRIP_RT,TRIP_AG,TR,Trip Route,3,FF0000,FFFFFF\n";
        given()
                .multiPart("file", "r.csv", routeCsv.getBytes(), "application/octet-stream")
                .when().post("/admin/upload/routes").then().statusCode(200);

        String calCsv = "service_id,monday,tuesday,wednesday,thursday,friday,saturday,sunday,start_date,end_date\n"
                + "TRIP_SVC,1,1,1,1,1,0,0,20260101,20261231\n";
        given()
                .multiPart("file", "c.csv", calCsv.getBytes(), "application/octet-stream")
                .when().post("/admin/upload/calendar").then().statusCode(200);

        String csv = "route_id,service_id,trip_id,shape_id,trip_headsign,trip_short_name,direction_id\n"
                + "TRIP_RT,TRIP_SVC,TRIP_1,,,, 0\n";

        given()
                .multiPart("file", "trips.csv", csv.getBytes(), "application/octet-stream")
                .when().post("/admin/upload/trips")
                .then()
                .statusCode(200)
                .body("tableName", equalTo("trips"))
                .body("importedRows", equalTo(1))
                .body("errors", empty());
    }

    @Test
    @Order(71)
    void uploadTripsShouldReturnErrorsForWrongHeaders() {
        String csv = "bad\ndata\n";

        given()
                .multiPart("file", "bad.csv", csv.getBytes(), "application/octet-stream")
                .when().post("/admin/upload/trips")
                .then()
                .statusCode(200)
                .body("errors[0]", containsString("Encabezados incorrectos"));
    }

    // ── Stop Times (needs trip + stop) ───────────────────────────────

    @Test
    @Order(80)
    void uploadStopTimesShouldImportValidCsv() {
        // Seed full chain: agency → route, calendar → trip, stop
        seedFullChainForStopTimes();

        String csv = "trip_id,timepoint,stop_id,stop_sequence,arrival_time,departure_time\n"
                + "ST_TRIP,1,ST_STOP,1,08:00:00,08:01:00\n";

        given()
                .multiPart("file", "st.csv", csv.getBytes(), "application/octet-stream")
                .when().post("/admin/upload/stop-times")
                .then()
                .statusCode(200)
                .body("tableName", equalTo("stop_times"))
                .body("importedRows", equalTo(1))
                .body("errors", empty());
    }

    @Test
    @Order(81)
    void uploadStopTimesShouldReturnErrorsForWrongHeaders() {
        String csv = "bad\ndata\n";

        given()
                .multiPart("file", "bad.csv", csv.getBytes(), "application/octet-stream")
                .when().post("/admin/upload/stop-times")
                .then()
                .statusCode(200)
                .body("errors[0]", containsString("Encabezados incorrectos"));
    }

    // ── Frequencies (needs trip) ─────────────────────────────────────

    @Test
    @Order(90)
    void uploadFrequenciesShouldImportValidCsv() {
        // Seed: agency → route, calendar → trip
        seedFullChainForFrequencies();

        String csv = "trip_id,start_time,end_time,headway_secs,exact_times\n"
                + "FQ_TRIP,06:00:00,22:00:00,600,1\n";

        given()
                .multiPart("file", "freq.csv", csv.getBytes(), "application/octet-stream")
                .when().post("/admin/upload/frequencies")
                .then()
                .statusCode(200)
                .body("tableName", equalTo("frequencies"))
                .body("importedRows", equalTo(1))
                .body("errors", empty());
    }

    @Test
    @Order(91)
    void uploadFrequenciesShouldReturnErrorsForWrongHeaders() {
        String csv = "bad\ndata\n";

        given()
                .multiPart("file", "bad.csv", csv.getBytes(), "application/octet-stream")
                .when().post("/admin/upload/frequencies")
                .then()
                .statusCode(200)
                .body("errors[0]", containsString("Encabezados incorrectos"));
    }

    // ── Helpers ──────────────────────────────────────────────────────

    private void seedFullChainForStopTimes() {
        String agCsv = "agency_id,agency_name,agency_url,agency_timezone,agency_lang,agency_color\n"
                + "ST_AG,ST Agency,http://t.com,America/Mexico_City,es,009B3A\n";
        given().multiPart("file", "a.csv", agCsv.getBytes(), "application/octet-stream")
                .when().post("/admin/upload/agency").then().statusCode(200);

        String rtCsv = "route_id,agency_id,route_short_name,route_long_name,route_type,route_color,route_text_color\n"
                + "ST_RT,ST_AG,S,ST Route,3,FF0000,FFFFFF\n";
        given().multiPart("file", "r.csv", rtCsv.getBytes(), "application/octet-stream")
                .when().post("/admin/upload/routes").then().statusCode(200);

        String calCsv = "service_id,monday,tuesday,wednesday,thursday,friday,saturday,sunday,start_date,end_date\n"
                + "ST_SVC,1,1,1,1,1,0,0,20260101,20261231\n";
        given().multiPart("file", "c.csv", calCsv.getBytes(), "application/octet-stream")
                .when().post("/admin/upload/calendar").then().statusCode(200);

        String tripCsv = "route_id,service_id,trip_id,shape_id,trip_headsign,trip_short_name,direction_id\n"
                + "ST_RT,ST_SVC,ST_TRIP,,,,0\n";
        given().multiPart("file", "t.csv", tripCsv.getBytes(), "application/octet-stream")
                .when().post("/admin/upload/trips").then().statusCode(200);

        String stopCsv = "stop_id,stop_name,stop_lat,stop_lon,zone_id,wheelchair_boarding\n"
                + "ST_STOP,ST Stop,19.345,-99.065,Z1,\n";
        given().multiPart("file", "s.csv", stopCsv.getBytes(), "application/octet-stream")
                .when().post("/admin/upload/stops").then().statusCode(200);
    }

    private void seedFullChainForFrequencies() {
        String agCsv = "agency_id,agency_name,agency_url,agency_timezone,agency_lang,agency_color\n"
                + "FQ_AG,FQ Agency,http://t.com,America/Mexico_City,es,009B3A\n";
        given().multiPart("file", "a.csv", agCsv.getBytes(), "application/octet-stream")
                .when().post("/admin/upload/agency").then().statusCode(200);

        String rtCsv = "route_id,agency_id,route_short_name,route_long_name,route_type,route_color,route_text_color\n"
                + "FQ_RT,FQ_AG,F,FQ Route,3,FF0000,FFFFFF\n";
        given().multiPart("file", "r.csv", rtCsv.getBytes(), "application/octet-stream")
                .when().post("/admin/upload/routes").then().statusCode(200);

        String calCsv = "service_id,monday,tuesday,wednesday,thursday,friday,saturday,sunday,start_date,end_date\n"
                + "FQ_SVC,1,1,1,1,1,0,0,20260101,20261231\n";
        given().multiPart("file", "c.csv", calCsv.getBytes(), "application/octet-stream")
                .when().post("/admin/upload/calendar").then().statusCode(200);

        String tripCsv = "route_id,service_id,trip_id,shape_id,trip_headsign,trip_short_name,direction_id\n"
                + "FQ_RT,FQ_SVC,FQ_TRIP,,,,0\n";
        given().multiPart("file", "t.csv", tripCsv.getBytes(), "application/octet-stream")
                .when().post("/admin/upload/trips").then().statusCode(200);
    }
}
