package apiCustomerManagement.test.refactor.stub;

import apiCustomerManagement.common.StubServer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class SecondTest {
    @BeforeAll
    static void setUp() {
        StubServer.startStubServer();
    }
    @Test
    void doSomething() {
        System.out.println("Hello World222");
    }
}
