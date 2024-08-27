package api.test;

import api.data.GetCountriesData;
import api.model.Country;
import api.model.Filter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import net.javacrumbs.jsonunit.core.Option;
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
import static org.hamcrest.Matchers.equalTo;


public class GetCountriesApiTests {
    private static final String GET_COUNTRIES_PATH = "/api/v1/countries";
    private static final String GET_COUNTRIES_PATH_V2 = "/api/v2/countries";
    private static final String GET_COUNTRIES_BY_CODE_PATH = "/api/v1/countries/{code}";
    private static final String GET_COUNTRIES_BY_FILTER_PATH_V3 = "/api/v3/countries?gdp={gdp}&operator={operator}";

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
        List<Filter> filters = mapper.readValue(GetCountriesData.FILTER_INPUT_DATA_V3, new TypeReference<List<Filter>>() {});
        return filters.stream();
    }
    @ParameterizedTest
    @MethodSource("filterProvider")
    public void verifyGetCountriesApiByFilterResponseSchemaV3(Filter filter){
        RestAssured.given()
                .pathParam("gdp", Double.toString(filter.getGdp()))
                .pathParam("operator", filter.getOperator())
                .get(GET_COUNTRIES_BY_FILTER_PATH_V3)
                .then()
                .assertThat()
                .body(matchesJsonSchemaInClasspath("json-schema/get-countries-by-filter-json-schema-v3.json"));
    }
    @ParameterizedTest
    @MethodSource("filterProvider")
    public void verifyGetCountriesApiByFilterResponseValueV3(Filter filter){
        Response actualResponse = RestAssured.given()
                .pathParam("gdp", filter.getGdp())
                .pathParam("operator", filter.getOperator())
                .get(GET_COUNTRIES_BY_FILTER_PATH_V3);
        String actualResponseBody = actualResponse.asString();
        //1. Write your own json filter and then compare with api result
        //2. Iterate each country in actualResponse and see if it in GET_COUNTRIES_BY_FILTER_V3, and see if that country comply with the provided filter
    }

    public void filterCountriesWithGdpV3(double gdp, String operator) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        List<Country> countries = mapper.readValue(GetCountriesData.ALL_COUNTRIES_V3, new TypeReference<List<Country>>() {});

    }

}
