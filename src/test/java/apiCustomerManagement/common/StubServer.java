package apiCustomerManagement.common;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

public class StubServer {
    private static WireMockServer wireMockServer;

    public static WireMockServer getWireMockServer() {
        if (wireMockServer == null) {
            wireMockServer = new WireMockServer(options().port(8080).notifier(new ConsoleNotifier(true)));
        }
        return wireMockServer;
    }

    public static void startStubServer() {
        if (!getWireMockServer().isRunning()) {
            getWireMockServer().start();
        }
    }
}


//try apply singleton pattern