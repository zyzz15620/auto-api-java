package api.test;

import api.data.GetCountriesData;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import net.javacrumbs.jsonunit.core.Option;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonPartEquals;
import static org.hamcrest.MatcherAssert.assertThat;


public class GetCountriesApiTests {
    @BeforeAll
    static void setUp(){
        RestAssured.baseURI = "http://localhost";
        RestAssured.port =  3000;
    }

    @Test
    public void verifyGetCountriesApiResponseSchema(){
        RestAssured.get("/api/v1/countries")
                .then()
                .assertThat()
                .body(matchesJsonSchemaInClasspath("json-schema/get-countries-json-schema.json"));
    }

    @Test
    public void verifyGetCountriesApiResponseValue(){
        String expected = GetCountriesData.ALL_COUNTRIES;
        Response actualResponse = RestAssured.get("/api/v1/countries");
        String actualResponseBody = actualResponse.asString();

        assertThat(actualResponseBody, jsonEquals(expected).when(Option.IGNORING_ARRAY_ORDER));

        assertThat(actualResponseBody, jsonPartEquals("[0].name" , "Viet Nam"));

    }
}
