package apiCustomerManagement.common;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

public class StubServer {
    public static void startStubServer() {
        WireMockServer wireMockServer = new WireMockServer(options().port(8089)
                .notifier(new ConsoleNotifier(true)));
        wireMockServer.start();
    }
    public static void main(String[] args) {
        startStubServer();
    }
}
