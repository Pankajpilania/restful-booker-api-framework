package api.clients;

import api.SpecBuilder;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import static io.restassured.RestAssured.given;

public class PingClient {

    @Step("Healthcheck Ping")
    public Response ping() {
        return given()
                .spec(SpecBuilder.getRequestSpec())
                .when()
                .get("/ping");
    }
}
