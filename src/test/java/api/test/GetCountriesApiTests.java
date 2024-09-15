package api.test;

import api.data.GetCountriesData;
import api.model.country.Country;
import api.model.country.CountryPagination;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.Response;
import net.javacrumbs.jsonunit.core.Option;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
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
    private static final String GET_COUNTRIES_PATH_V5 = "/api/v5/countries";
    private static final String GET_COUNTRIES_PATH_V4 = "/api/v4/countries";
    private static final String GET_COUNTRIES_PATH_V3 = "/api/v3/countries";
    private static final String GET_COUNTRIES_PATH_V2 = "/api/v2/countries";
    private static final String GET_COUNTRIES_BY_CODE_PATH = "/api/v1/countries/{code}";

    @BeforeAll
    static void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 3000;
    }

    //API 3
    static Stream<Country> countryProvider() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        List<Country> countries = mapper.readValue(GetCountriesData.ALL_COUNTRIES, new TypeReference<>() {
        });
        return countries.stream();
    }

    static Stream<Map<String, String>> filterProvider() {
        List<Map<String, String>> filter = new ArrayList<>();
        filter.add(Map.of("gdp", "5000", "operator", ">"));
        filter.add(Map.of("gdp", "5000", "operator", ">="));
        filter.add(Map.of("gdp", "5000", "operator", "<"));
        filter.add(Map.of("gdp", "5000", "operator", "<="));
        filter.add(Map.of("gdp", "5000", "operator", "=="));
        filter.add(Map.of("gdp", "5000", "operator", "!="));
        return filter.stream();
    }

    private static CountryPagination getCountryPagination(int page, int PAGE_SIZE) {
        Response actualResponsePagination = RestAssured.given().log().all()
                .queryParam("page", page)
                .queryParam("size", PAGE_SIZE)
                .get(GET_COUNTRIES_PATH_V4);
        return actualResponsePagination.as(new TypeRef<>() {
        });
    }

    //API 1
    @Test
    public void verifyGetCountriesApiResponseSchema() {
        RestAssured.get(GET_COUNTRIES_PATH)
                .then()
                .assertThat()
                .body(matchesJsonSchemaInClasspath("json-schema/get-countries-json-schema.json"));
    }

    @Test
    public void verifyGetCountriesApiResponseValue() {
        String expected = GetCountriesData.ALL_COUNTRIES;
        Response actualResponse = RestAssured.get(GET_COUNTRIES_PATH);
        String actualResponseBody = actualResponse.asString();
        assertThat(actualResponseBody, jsonEquals(expected).when(Option.IGNORING_ARRAY_ORDER));
        assertThat(actualResponseBody, jsonPartEquals("[0].name", "Viet Nam")); //not needed
    }

    //API 2
    @Test
    public void verifyGetCountriesApiResponseSchemaV2() {
        RestAssured.get(GET_COUNTRIES_PATH_V2)
                .then()
                .assertThat()
                .body(matchesJsonSchemaInClasspath("json-schema/get-countries-json-schema-v2.json"));
    }

    @Test
    public void verifyGetCountriesApiResponseValueV2() {
        String expected = GetCountriesData.ALL_COUNTRIES_V2;
        Response actualResponse = RestAssured.get(GET_COUNTRIES_PATH_V2);
        String actualResponseBody = actualResponse.asString();
        assertThat(actualResponseBody, jsonEquals(expected).when(Option.IGNORING_ARRAY_ORDER));
        assertThat(actualResponseBody, jsonPartEquals("[0].gdp", 223.9)); //not needed
    }

    //API 4
//    static Stream<Filter> filterProvider() throws JsonProcessingException {
//        ObjectMapper mapper = new ObjectMapper();
//        List<Filter> filters = mapper.readValue(GetCountriesData.FILTER_INPUT_DATA_V3, new TypeReference<>() {});
//        return filters.stream();
//    }
//    @ParameterizedTest
//    @MethodSource("filterProvider")
//    public void verifyGetCountriesApiByFilterResponseSchemaV3(Filter filter){
//        Response actualResponse =  RestAssured.given()
//                .queryParam("gdp", Double.toString(filter.getGdp()))
//                .queryParam("operator", filter.getOperator())
//                .get(GET_COUNTRIES_PATH_V3);
//        assertThat(200,equalTo(actualResponse.statusCode()));
//        List<CountryVerTwo> countries = actualResponse.as(new TypeRef<>() {}); //Have to initiate List<> to handle if api return null
//        if (countries.isEmpty()) {
//            System.out.println("No countries match the filter criteria.");
//        } else {
//            actualResponse.then().assertThat()
//                    .body(matchesJsonSchemaInClasspath("json-schema/get-countries-by-filter-json-schema-v3.json"));
//        }
//    }

    @ParameterizedTest
    @MethodSource("countryProvider")
    public void verifyGetCountriesApiByCodeResponseSchema(Country country) {
        RestAssured.given()
                .pathParam("code", country.getCode())
                .log().all().get(GET_COUNTRIES_BY_CODE_PATH)
                .then()
                .assertThat()
                .body(matchesJsonSchemaInClasspath("json-schema/get-countries-by-code-json-schema.json"));
    }

    @ParameterizedTest
    @MethodSource("countryProvider")
    public void verifyGetCountriesApiByCodeResponseValue(Country country) {
        Response actualResponse = RestAssured.given()
                .pathParam("code", country.getCode())
                .log().all().get(GET_COUNTRIES_BY_CODE_PATH);
        assertThat(200, equalTo(actualResponse.statusCode()));
        Country actualResponseBody = actualResponse.as(Country.class);
        assertThat(String.format("Actual: %s\n Expected: %s\n", actualResponseBody, country), actualResponseBody, jsonEquals(country)); //since this is 2 object, we don't need to care about it's order
    }

    //API 4 Get countries by filters
    @ParameterizedTest
    @MethodSource("filterProvider")
    void verifyCountryByFilterGiven(Map<String, String> queryParams) {
        float actualGdp = Float.parseFloat(queryParams.get("gdp"));
        final Matcher<Float> matcher = switch (queryParams.get("operator")) {
            case "!=" -> not(equalTo(actualGdp));
            case "==" -> equalTo(actualGdp);
            case ">" -> greaterThan(actualGdp);
            case ">=" -> greaterThanOrEqualTo(actualGdp);
            case "<" -> lessThan(actualGdp);
            case "<=" -> lessThanOrEqualTo(actualGdp);
            default -> throw new IllegalArgumentException("Unsupported operator: " + actualGdp);
        };
        Response actualResponse = RestAssured.given().log().all()
                .queryParam("gdp", actualGdp)
                .queryParam("operator", queryParams.get("operator"))
                .get(GET_COUNTRIES_PATH_V3);
        assertThat(200, equalTo(actualResponse.statusCode()));
        List<Country> countries = actualResponse.as(new TypeRef<>() {
        });
        countries.forEach(country -> assertThat(country.getGdp(), matcher));
    }

    @Test
    void verifyGetCountriesPagination() {
        int PAGE_SIZE = 3;
        CountryPagination countryPaginationFirstPage = getCountryPagination(1, PAGE_SIZE);
        CountryPagination countryPaginationSecondPage = getCountryPagination(1, PAGE_SIZE);
        assertThat(countryPaginationFirstPage.getData().size(), equalTo(PAGE_SIZE));
        assertThat(countryPaginationSecondPage.getData().size(), equalTo(PAGE_SIZE));
        assertThat(countryPaginationFirstPage.getData().containsAll(countryPaginationSecondPage.getData()), is(false));

        int sizeOfLastPage = countryPaginationFirstPage.getTotal() % PAGE_SIZE;
        int lastPage = countryPaginationFirstPage.getTotal() / PAGE_SIZE;
        if (sizeOfLastPage > 0) {
            lastPage++;
        } else if (sizeOfLastPage == 0) {
            sizeOfLastPage = PAGE_SIZE;
        }
        CountryPagination countryPaginationLastPage = getCountryPagination(lastPage, PAGE_SIZE);
        assertThat(countryPaginationLastPage.getData().size(), equalTo(sizeOfLastPage));

        CountryPagination countryPaginationLastPagePlus = getCountryPagination(lastPage + 1, PAGE_SIZE);
        assertThat(countryPaginationLastPagePlus.getData().size(), equalTo(0));
        //there might be a problem where number of page is 1-2, so sometimes we have to reduce the size to get more page
    }

    @Test
    void verifyGetCountriesPrivate() {
        String actualResponse = RestAssured.given().log().all()
                .header("api-key", "private")
                .get(GET_COUNTRIES_PATH_V5).asString();
        String expect = GetCountriesData.ALL_COUNTRIES_V5_PRIVATE;
        assertThat(actualResponse, jsonEquals(expect).when(Option.IGNORING_ARRAY_ORDER));
    }
}
