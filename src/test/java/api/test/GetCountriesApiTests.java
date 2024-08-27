package api.test;

import api.data.GetCountriesData;
import api.model.Country;
import api.model.CountryVerTwo;
import api.model.Filter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;
import net.javacrumbs.jsonunit.core.Option;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonPartEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;


public class GetCountriesApiTests {
    private static final String GET_COUNTRIES_PATH = "/api/v1/countries";
    private static final String GET_COUNTRIES_PATH_V3 = "/api/v3/countries";
    private static final String GET_COUNTRIES_PATH_V2 = "/api/v2/countries";
    private static final String GET_COUNTRIES_BY_CODE_PATH = "/api/v1/countries/{code}";

    @BeforeAll
    static void setUp(){
        RestAssured.baseURI = "http://localhost";
        RestAssured.port =  3000;
    }

    //API 1
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

    //API 2
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

    //API 3
    static Stream<Country> countryProvider() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        List<Country> countries = mapper.readValue(GetCountriesData.ALL_COUNTRIES, new TypeReference<>() {});
        return countries.stream();
    }
    @ParameterizedTest
    @MethodSource("countryProvider")
    public void verifyGetCountriesApiByCodeResponseSchema(Country country){
        Map<String, String> params = new HashMap<>();
        params.put("code", country.getCode());
        RestAssured.get(GET_COUNTRIES_BY_CODE_PATH, params)
                .then()
                .assertThat()
                .body(matchesJsonSchemaInClasspath("json-schema/get-countries-by-code-json-schema.json"));
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

    //API 4
    static Stream<Filter> filterProvider() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        List<Filter> filters = mapper.readValue(GetCountriesData.FILTER_INPUT_DATA_V3, new TypeReference<>() {});
        return filters.stream();
    }
    @ParameterizedTest
    @MethodSource("filterProvider")
    public void verifyGetCountriesApiByFilterResponseSchemaV3(Filter filter){
        Response actualResponse =  RestAssured.given()
                .queryParam("gdp", Double.toString(filter.getGdp()))
                .queryParam("operator", filter.getOperator())
                .get(GET_COUNTRIES_PATH_V3);
        assertThat(200,equalTo(actualResponse.statusCode()));
        List<CountryVerTwo> countries = actualResponse.as(new TypeRef<>() {}); //Have to initiate List<> to handle if api return null
        if (countries.isEmpty()) {
            System.out.println("No countries match the filter criteria.");
        } else {
            actualResponse.then().assertThat()
                    .body(matchesJsonSchemaInClasspath("json-schema/get-countries-by-filter-json-schema-v3.json"));
        }
    }

    @Test
    void verifyCountryByFilterGivenGreaterThan(){
        verifyCountryByFilterGiven(4000, ">");
    }
    @Test
    void verifyCountryByFilterGivenGreaterThanOrEqualTo(){
        verifyCountryByFilterGiven(5000, ">=");
    }
    @Test
    void verifyCountryByFilterGivenEqualTo(){
        verifyCountryByFilterGiven(5000, "==");
    }
    @Test
    void verifyCountryByFilterGivenNotEqualTo(){
        verifyCountryByFilterGiven(5000, "!=");
    }
    @Test
    void verifyCountryByFilterGivenLessThan(){
        verifyCountryByFilterGiven(5000, "<");
    }
    @Test //This is the original approach before verifyCountryByFilterGiven() is made
    void verifyCountryByFilterGivenLessThanOrEqualTo(){
        Response actualResponse = RestAssured.given().log().all()
                .queryParam("gdp", 5000)
                .queryParam("operator", "<=")
                .get(GET_COUNTRIES_PATH_V3);
        assertThat(200,equalTo(actualResponse.statusCode()));
        List<CountryVerTwo> countries = actualResponse.as(new TypeRef<>() {});
        countries.forEach(country -> assertThat(country.getGdp(), not(equalTo(5000f))));
    }

    void verifyCountryByFilterGiven(float gdp, String operator){
        Matcher<Float> matcher = switch (operator) {
            case "!=" -> not(equalTo(gdp));
            case "==" -> equalTo(gdp);
            case ">" -> greaterThan(gdp);
            case ">=" -> greaterThanOrEqualTo(gdp);
            case "<" -> lessThan(gdp);
            case "<=" -> lessThanOrEqualTo(gdp);
            default -> throw new IllegalArgumentException("Unsupported operator: " + operator);
        };
        Response actualResponse = RestAssured.given().log().all()
                .queryParam("gdp", gdp)
                .queryParam("operator", operator)
                .get(GET_COUNTRIES_PATH_V3);
        List<CountryVerTwo> countries = actualResponse.as(new TypeRef<>() {});
        final Matcher<Float> finalMatcher = matcher;
        countries.forEach(country -> assertThat(country.getGdp(), finalMatcher));
    }
}
