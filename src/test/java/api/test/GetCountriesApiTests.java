package api.test;

import api.data.GetCountriesData;
import api.model.country.Country;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import net.javacrumbs.jsonunit.core.Option;
import org.checkerframework.checker.units.qual.C;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonPartEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;


public class GetCountriesApiTests {
    private static final String GET_COUNTRIES_PATH = "/api/v1/countries";
    private static final String GET_COUNTRIES_PATH_V2 = "/api/v2/countries";
    private static final String GET_COUNTRIES_BY_CODE_PATH = "/api/v1/countries/{code}";

    @BeforeAll
    static void setUp(){
        RestAssured.baseURI = "http://localhost";
        RestAssured.port =  3000;
    }

    @Test
    public void verifyGetCountriesApiResponseSchema(){
        RestAssured.get(GET_COUNTRIES_PATH)
                .then()
                .assertThat()
                .body(matchesJsonSchemaInClasspath("json-schema/get-countries-json-schema.json"));
    }

    @Test
    public void verifyGetCountriesApiResponseValue(){
        String expected = GetCountriesData.ALL_COUNTRIES;
        Response actualResponse = RestAssured.get(GET_COUNTRIES_PATH);
        String actualResponseBody = actualResponse.asString();

        assertThat(actualResponseBody, jsonEquals(expected).when(Option.IGNORING_ARRAY_ORDER));
        assertThat(actualResponseBody, jsonPartEquals("[0].name" , "Viet Nam"));

    }

    @Test
    public void verifyGetCountriesApiResponseSchemaV2(){
        RestAssured.get(GET_COUNTRIES_PATH_V2)
                .then()
                .assertThat()
                .body(matchesJsonSchemaInClasspath("json-schema/get-countries-json-schema-v2.json"));
    }

    @Test
    public void verifyGetCountriesApiResponseValueV2(){
        String expected = GetCountriesData.ALL_COUNTRIES_V2;
        Response actualResponse = RestAssured.get(GET_COUNTRIES_PATH_V2);
        String actualResponseBody = actualResponse.asString();

        assertThat(actualResponseBody, jsonEquals(expected).when(Option.IGNORING_ARRAY_ORDER));
        assertThat(actualResponseBody, jsonPartEquals("[0].gdp" , 223.9));

    }

    @Test
    public void verifyGetCountriesApiByCodeResponseSchema(){
        RestAssured.get(GET_COUNTRIES_BY_CODE_PATH)
                .then()
                .assertThat()
                .body(matchesJsonSchemaInClasspath("json-schema/get-countries-by-code-json-schema.json"));
    }

    static Stream<Country> countryProvider() throws JsonProcessingException {
//        List<Country> countries = new ArrayList<>();
//        Country vietNam = new Country("Viet Nam", "VN");
//        Country usa = new Country("USA a", "US");
//        countries.add(usa);
//        countries.add(vietNam);
        ObjectMapper mapper = new ObjectMapper();
        List<Country> countries = mapper.readValue(GetCountriesData.ALL_COUNTRIES, new TypeReference<List<Country>>() {});
        return countries.stream();
    }


    @ParameterizedTest
    @MethodSource("countryProvider")
    public void verifyGetCountriesApiByCodeResponseValue(Country country){
        Map<String, String> params = new HashMap<>();
        params.put("code", country.getCode());
        Response actualResponse = RestAssured.given().log().all().get(GET_COUNTRIES_BY_CODE_PATH, params);
        assertThat(200, equalTo(actualResponse.statusCode()));
        String actualResponseBody = actualResponse.asString();
        assertThat(String.format("Actual: %s\n Expected: %s\n", actualResponseBody, country),actualResponseBody, jsonEquals(country)); //since this is 2 object, we don't need to care about it's order
    }
}
