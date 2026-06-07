package api.clients;

import api.SpecBuilder;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import models.AuthRequest;
import static io.restassured.RestAssured.given;

public class AuthClient {

    @Step("Generate Auth Token")
    public Response createToken(AuthRequest authRequest) {
        return given()
                .spec(SpecBuilder.getRequestSpec())
                .body(authRequest)
                .when()
                .post("/auth");
    }
}
