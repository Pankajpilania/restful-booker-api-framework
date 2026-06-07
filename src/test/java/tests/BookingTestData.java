package tests;

import models.Booking;
import models.BookingDates;

public class BookingTestData {

    public static Booking defaultBooking() {
        return new Booking(
                "John",
                "Doe",
                150,
                true,
                new BookingDates("2026-08-01", "2026-08-10"),
                "Breakfast"
        );
    }

    public static Booking updatedBooking() {
        return new Booking(
                "John",
                "Smith",
                300,
                false,
                new BookingDates("2026-09-01", "2026-09-10"),
                "Dinner"
        );
    }
}
