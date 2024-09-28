package apiCustomerManagement.test;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class WiremockTests {
    private static final WireMockServer wireMockServer = new WireMockServer(options().port(8080));

    @BeforeAll
    public static void setup(){
        wireMockServer.start();
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 8080;
    }

    public void setupStub() {
        stubFor(get(urlEqualTo("/api/v1/countries"))
                .willReturn(aResponse()
                        .withStatus(999)
                        .withBody("WIREMOCK TESTING 123456789")
                ));
    }

    @Test
    public void verifyGetCountryRequest(){
        setupStub();
        Response getRequest = RestAssured.given().log().all()
                .get("/api/v1/countries");
        assertThat(getRequest.statusCode(), equalTo(999));
        System.out.println(getRequest.asString());
    }

    @AfterAll
    public static void tearDown(){
        wireMockServer.stop();
    }
}
