package api.clients;

import api.SpecBuilder;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import models.Booking;
import java.util.Map;
import static io.restassured.RestAssured.given;

public class BookingClient {

    @Step("Create a new booking")
    public Response createBooking(Booking booking) {
        return given()
                .spec(SpecBuilder.getRequestSpec())
                .body(booking)
                .when()
                .post("/booking");
    }

    @Step("Get booking by ID: {bookingId}")
    public Response getBooking(int bookingId) {
        return given()
                .spec(SpecBuilder.getRequestSpec())
                .when()
                .get("/booking/" + bookingId);
    }

    @Step("Update booking using PUT for ID: {bookingId}")
    public Response updateBooking(int bookingId, Booking booking, String token) {
        return given()
                .spec(SpecBuilder.getRequestSpec())
                .header("Cookie", "token=" + token)
                .body(booking)
                .when()
                .put("/booking/" + bookingId);
    }

    @Step("Partially update booking using PATCH for ID: {bookingId}")
    public Response partialUpdateBooking(int bookingId, Map<String, Object> partialPayload, String token) {
        return given()
                .spec(SpecBuilder.getRequestSpec())
                .header("Cookie", "token=" + token)
                .body(partialPayload)
                .when()
                .patch("/booking/" + bookingId);
    }

    @Step("Delete booking ID: {bookingId}")
    public Response deleteBooking(int bookingId, String token) {
        return given()
                .spec(SpecBuilder.getRequestSpec())
                .header("Cookie", "token=" + token)
                .when()
                .delete("/booking/" + bookingId);
    }
}
