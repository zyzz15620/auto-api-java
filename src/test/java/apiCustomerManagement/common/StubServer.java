package apiCustomerManagement.common;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

public class StubServer {
    public static WireMockServer wireMockServer;

    public static void startStubServer() {
        if(wireMockServer == null) {
            wireMockServer = new WireMockServer(options().port(8080).notifier(new ConsoleNotifier(true)));
        }
        if (!wireMockServer.isRunning()) {
            wireMockServer.start();
        }
    }
    public static void main(String[] args) {
        startStubServer();
    }
}


//try apply singleton pattern