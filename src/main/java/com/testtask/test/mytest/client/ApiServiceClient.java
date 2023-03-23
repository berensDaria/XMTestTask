package com.testtask.test.mytest.client;

import static io.restassured.RestAssured.given;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class ApiServiceClient {

    private static final String BASE_URL = "https://swapi.dev/api/";

    private final RequestSpecification serviceSpec;

    public ApiServiceClient() {
        this.serviceSpec = new RequestSpecBuilder()
                .setBaseUri(BASE_URL).build()
                .contentType(ContentType.JSON)
                .header("Accept", "application/json");
    }

    public Response searchFilmByTitle(String title) {
        return searchByTerm("/films", title);
    }

    public Response searchPeopleByName(String name) {
        return searchByTerm("/people", name);
    }

    private Response searchByTerm(String path, String searchTerm) {
        return given().
                spec(serviceSpec).
                queryParam("search", searchTerm).
                when().get(path).
                then().
                extract().response();
    }

    public Response getObjectByUrl(String url) {
        return given().
                spec(serviceSpec).
                baseUri(url).
                when().get().
                then().
                extract().response();
    }
}

