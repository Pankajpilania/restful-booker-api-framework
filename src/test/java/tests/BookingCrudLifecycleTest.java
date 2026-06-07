package tests;

import api.clients.AuthClient;
import api.clients.BookingClient;
import api.clients.PingClient;
import config.ConfigManager;
import io.qameta.allure.*;
import io.restassured.response.Response;
import models.AuthRequest;
import models.AuthResponse;
import models.Booking;
import models.BookingDates;
import models.BookingResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.apache.http.HttpStatus.*;
import static org.junit.jupiter.api.Assertions.*;

@Epic("Booking Management")
@Feature("CRUD Independent Lifecycle & Negative Scenarios")
public class BookingCrudLifecycleTest {

    private static String token;

    private final AuthClient authClient = new AuthClient();
    private final BookingClient bookingClient = new BookingClient();
    private final PingClient pingClient = new PingClient();

    @BeforeAll
    public static void setUpClass() {
        Allure.step("Perform global authentication setup", () -> {
            AuthClient auth = new AuthClient();
            AuthRequest authRequest = new AuthRequest(
                    ConfigManager.getUsername(),
                    ConfigManager.getPassword()
            );
            Response response = auth.createToken(authRequest);
            assertEquals(SC_OK, response.statusCode(), "Global setup token generation should return 200");
            token = response.as(AuthResponse.class).token();
            assertNotNull(token, "Global setup auth token should not be null");
        });
    }

    // --- Helper Methods ---

    private int createBookingAndGetId(Booking booking) {
        Response response = bookingClient.createBooking(booking);
        assertEquals(SC_OK, response.statusCode(), "Helper: Booking creation should return 200");
        return response.as(BookingResponse.class).bookingid();
    }

    private void assertBookingEquals(Booking expected, Booking actual) {
        assertAll(
                () -> assertEquals(expected.firstname(), actual.firstname(), "Firstname mismatch"),
                () -> assertEquals(expected.lastname(), actual.lastname(), "Lastname mismatch"),
                () -> assertEquals(expected.totalprice(), actual.totalprice(), "Totalprice mismatch"),
                () -> assertEquals(expected.depositpaid(), actual.depositpaid(), "Depositpaid mismatch"),
                () -> assertEquals(expected.bookingdates().checkin(), actual.bookingdates().checkin(), "Checkin date mismatch"),
                () -> assertEquals(expected.bookingdates().checkout(), actual.bookingdates().checkout(), "Checkout date mismatch"),
                () -> assertEquals(expected.additionalneeds(), actual.additionalneeds(), "Additional needs mismatch")
        );
    }

    // --- Independent Happy Path Scenarios ---

    @Test
    @Story("Health Check")
    @Description("Verify the API is up and running")
    public void testHealthCheck() {
        Allure.step("Call Ping Endpoint", () -> {
            Response response = pingClient.ping();
            assertEquals(SC_CREATED, response.statusCode(), "Healthcheck should return 201");
        });
    }

    @Test
    @Story("Create Booking")
    @Description("Create a booking and verify all fields in response")
    public void testCreateBooking() {
        Booking originalBookingPayload = BookingTestData.defaultBooking();

        Response response = Allure.step("Create Booking", () -> 
            bookingClient.createBooking(originalBookingPayload)
        );

        Allure.step("Verify Response Fields", () -> {
            assertEquals(SC_OK, response.statusCode(), "Booking creation should return 200");
            BookingResponse bookingResponse = response.as(BookingResponse.class);
            assertTrue(bookingResponse.bookingid() > 0, "Booking ID should be present and positive");
            assertBookingEquals(originalBookingPayload, bookingResponse.booking());
        });
    }

    @Test
    @Story("Read Booking")
    @Description("Fetch a newly created booking and validate full state persistence")
    public void testGetBooking() {
        Booking originalBookingPayload = BookingTestData.defaultBooking();
        int bookingId = Allure.step("Setup: Create dynamic booking", () -> 
            createBookingAndGetId(originalBookingPayload)
        );

        Response response = Allure.step("Fetch booking", () -> 
            bookingClient.getBooking(bookingId)
        );

        Allure.step("Verify persisted booking matches original", () -> {
            assertEquals(SC_OK, response.statusCode(), "Get booking should return 200");
            Booking fetchedBooking = response.as(Booking.class);
            assertBookingEquals(originalBookingPayload, fetchedBooking);
        });
    }

    @Test
    @Story("Update Booking (PUT)")
    @Description("Completely replace a booking record and verify updates via follow-up GET request")
    public void testUpdateBooking() {
        Booking originalBookingPayload = BookingTestData.defaultBooking();
        int bookingId = Allure.step("Setup: Create dynamic booking", () -> 
            createBookingAndGetId(originalBookingPayload)
        );

        Booking updatedPayload = BookingTestData.updatedBooking();

        Response response = Allure.step("Perform PUT update", () -> 
            bookingClient.updateBooking(bookingId, updatedPayload, token)
        );

        Allure.step("Verify PUT response fields match fully", () -> {
            assertEquals(SC_OK, response.statusCode(), "Update booking should return 200");
            Booking responseBooking = response.as(Booking.class);
            assertBookingEquals(updatedPayload, responseBooking);
        });

        Response getResponse = Allure.step("Verify persistence using GET", () -> 
            bookingClient.getBooking(bookingId)
        );

        Allure.step("Verify GET response fields match updated payload fully", () -> {
            assertEquals(SC_OK, getResponse.statusCode(), "GET request should return 200");
            Booking persistedBooking = getResponse.as(Booking.class);
            assertBookingEquals(updatedPayload, persistedBooking);
        });
    }

    @Test
    @Story("Partial Update Booking (PATCH)")
    @Description("Update selected booking fields and verify all fields (updated & unchanged) remain consistent")
    public void testPartialUpdateBooking() {
        Booking original = BookingTestData.defaultBooking();
        int bookingId = Allure.step("Setup: Create dynamic booking", () -> 
            createBookingAndGetId(original)
        );

        Map<String, Object> partialPayload = Map.of(
                "firstname", "Jane",
                "totalprice", 500
        );

        Response response = Allure.step("Perform PATCH partial update", () -> 
            bookingClient.partialUpdateBooking(bookingId, partialPayload, token)
        );

        Booking expectedPatchedBooking = new Booking(
                "Jane",
                original.lastname(),
                500,
                original.depositpaid(),
                original.bookingdates(),
                original.additionalneeds()
        );

        Allure.step("Verify PATCH response fields", () -> {
            assertEquals(SC_OK, response.statusCode(), "Partial update should return 200");
            Booking responseBooking = response.as(Booking.class);
            assertBookingEquals(expectedPatchedBooking, responseBooking);
        });

        Response getResponse = Allure.step("Verify persistence using GET", () -> 
            bookingClient.getBooking(bookingId)
        );

        Allure.step("Verify GET response contains complete patched data", () -> {
            assertEquals(SC_OK, getResponse.statusCode());
            Booking persistedBooking = getResponse.as(Booking.class);
            assertBookingEquals(expectedPatchedBooking, persistedBooking);
        });
    }

    @Test
    @Story("Delete Booking")
    @Description("Delete booking and verify GET request returns 404")
    public void testDeleteBooking() {
        Booking original = BookingTestData.defaultBooking();
        int bookingId = Allure.step("Setup: Create dynamic booking", () -> 
            createBookingAndGetId(original)
        );

        Response deleteResponse = Allure.step("Delete booking", () -> 
            bookingClient.deleteBooking(bookingId, token)
        );

        Allure.step("Verify delete status code", () -> 
            assertEquals(SC_CREATED, deleteResponse.statusCode(), "Delete should return 201")
        );

        Response getResponse = Allure.step("Verify booking is deleted (GET 404)", () -> 
            bookingClient.getBooking(bookingId)
        );

        Allure.step("Verify GET returns 404", () -> 
            assertEquals(SC_NOT_FOUND, getResponse.statusCode(), "Fetching a deleted booking should return 404")
        );
    }

    // --- Negative Scenarios ---

    @Test
    @Story("Update Booking Negative")
    @Description("Verify that updating a booking without token returns 403 Forbidden")
    public void testUpdateBookingWithoutTokenShouldReturn403() {
        Booking original = BookingTestData.defaultBooking();
        int bookingId = Allure.step("Setup: Create dynamic booking", () -> 
            createBookingAndGetId(original)
        );

        Booking updatedPayload = BookingTestData.updatedBooking();

        Response response = Allure.step("Attempt PUT update without token", () -> 
            bookingClient.updateBooking(bookingId, updatedPayload, "")
        );

        Allure.step("Verify response code is 403", () -> 
            assertEquals(SC_FORBIDDEN, response.statusCode(), "Updating booking without token should return 403 Forbidden")
        );
    }

    @Test
    @Story("Delete Booking Negative")
    @Description("Verify that deleting a booking without token returns 403 Forbidden")
    public void testDeleteBookingWithoutTokenShouldReturn403() {
        Booking original = BookingTestData.defaultBooking();
        int bookingId = Allure.step("Setup: Create dynamic booking", () -> 
            createBookingAndGetId(original)
        );

        Response response = Allure.step("Attempt delete without token", () -> 
            bookingClient.deleteBooking(bookingId, "")
        );

        Allure.step("Verify response code is 403", () -> 
            assertEquals(SC_FORBIDDEN, response.statusCode(), "Deleting booking without token should return 403 Forbidden")
        );
    }

    @Test
    @Story("Read Booking Negative")
    @Description("Verify that fetching a booking with an invalid ID returns 404 Not Found")
    public void testGetBookingWithInvalidIdShouldReturn404() {
        Response response = Allure.step("Get booking with invalid ID", () -> 
            bookingClient.getBooking(999999)
        );

        Allure.step("Verify response code is 404", () -> 
            assertEquals(SC_NOT_FOUND, response.statusCode(), "Fetching non-existent booking ID should return 404 Not Found")
        );
    }

    @Test
    @Story("Create Booking Negative")
    @Description("Verify that creating a booking with invalid payload returns an error status code (>= 400)")
    public void testCreateBookingWithInvalidPayloadShouldReturnError() {
        Response response = Allure.step("Send create request with empty body", () -> 
            io.restassured.RestAssured.given()
                .spec(api.SpecBuilder.getRequestSpec())
                .body("{}")
                .when()
                .post("/booking")
        );

        Allure.step("Verify response code indicates client/server error", () -> 
            assertTrue(response.statusCode() >= 400, "Should return client/server error code (>= 400) for empty body")
        );
    }

    @Test
    @Story("Authentication Negative")
    @Description("Verify authentication failure returns bad credentials reason")
    public void testAuthWithInvalidCredentialsShouldReturnReasonBadCredentials() {
        AuthRequest invalidRequest = new AuthRequest("badUser", "badPassword");

        Response response = Allure.step("Submit invalid credentials to /auth", () -> 
            authClient.createToken(invalidRequest)
        );

        Allure.step("Verify auth response contains bad credentials reason", () -> {
            assertEquals(SC_OK, response.statusCode(), "Restful-Booker auth endpoint returns 200 even for bad auth");
            String reason = response.jsonPath().getString("reason");
            assertEquals("Bad credentials", reason, "Response body should show 'Bad credentials' reason");
        });
    }
}
