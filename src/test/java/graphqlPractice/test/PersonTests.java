package graphqlPractice.test;

import graphqlPractice.model.Graphql;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static org.hamcrest.MatcherAssert.assertThat;

public class PersonTests {
    final String path = "https://swapi-graphql.netlify.app/.netlify/functions/index";

    @Test
    public void verifyPersonResponseSchema() {
        RestAssured.given().log().all()
                .header("Content-Type", "application/json")
                .body(new Graphql(GraphqlPracticeData.person10Query, null))
                .post(path)
                .then()
                .assertThat()
                .body(matchesJsonSchemaInClasspath("json-schema/graphql-person-schema.json"));
    }

    @Test
    public void verifyPersonResponseValue() {
        Response actualResponse = RestAssured.given().log().all()
                .header("Content-Type", "application/json")
                .body(new Graphql(GraphqlPracticeData.person10Query, null))
                .post(path);
        assertThat(actualResponse.asString(), jsonEquals(GraphqlPracticeData.person10Response));
    }

    @ParameterizedTest
    @MethodSource("personIdProvider")
    public void verifyPersonWithID(Integer id) {
        Map<String, Integer> fieldID = Map.of("ID", id);
        Graphql query = new Graphql(GraphqlPracticeData.person10Query, fieldID);

        Response actualResponse = RestAssured.given().log().all()
                .header("Content-Type", "application/json")
                .body(query)
                .post(path);
        assertThat(actualResponse.asString(), jsonEquals(GraphqlPracticeData.person10Response));
    }

    public static Stream<Integer> personIdProvider(){
        List<Integer> personIds = new ArrayList<>();
        for (int i = 1; i <= 84; i++) {
            personIds.add(i);
        }
        return personIds.stream();
    }
}
