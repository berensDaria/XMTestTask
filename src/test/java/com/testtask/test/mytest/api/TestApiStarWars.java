package com.testtask.test.mytest.api;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.in;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import com.testtask.test.mytest.client.ApiServiceClient;

@SpringBootTest
class TestApiStarWars {

    private ApiServiceClient apiServiceClient;

    @BeforeEach
    void setup() {
        apiServiceClient = new ApiServiceClient();
    }

    /*  API Test case
    1.	Find film with a title 'A New Hope'
    2.	Using previous response (1) find person with name “Biggs Darklighter” among the characters that were part of that film.
    3.	Using previous response (2) find which starship he/she was flying on.
    4.	Using previous response (3) check next:
        a.	starship class is “Starfighter”
        b.	“Luke Skywalker” is among pilots that were also flying this kind of starship
     */
    @Test
    void apiTest() {
        // Found film with a title 'A New Hope' and got characters (step 1)
        List<String> charactersFromANewHope = apiServiceClient.searchFilmByTitle("A New Hope")
                .then().assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract().response()
                .getBody().jsonPath().getList("results[0].characters", String.class);

        // Found persons with name 'Biggs Darklighter', filtered them by characters from 'A New Hope' and got his starship (step 2-3)
        String biggsDarklighterStarshipUrl = apiServiceClient.searchPeopleByName("Biggs Darklighter")
                .then().assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract().response()
                .getBody().jsonPath()
                .get("results.findAll{it.url in " + wrapListElementsInQuotesForFilter(charactersFromANewHope) + "}[0].starships[0]");

        // Founded starship of Luke Skywalker (for step 4.b)
        List<String> lukeSkywalkerStarshipsUrls = apiServiceClient.searchPeopleByName("Luke Skywalker")
                .then().assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract().response()
                .getBody().jsonPath()
                .getList("results[0].starships");

        // Got starship of Biggs DarkLighter and checked (step 4)
        apiServiceClient.getObjectByUrl(biggsDarklighterStarshipUrl)
                .then().assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("starship_class", equalTo("Starfighter"),
                        "url", in(lukeSkywalkerStarshipsUrls));
    }

    private static List<String> wrapListElementsInQuotesForFilter(List<String> items) {
        return items.stream().map(item -> "'" + item + "'").collect(Collectors.toList());
    }
}
