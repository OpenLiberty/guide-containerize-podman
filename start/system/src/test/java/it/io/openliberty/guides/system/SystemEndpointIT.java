package it.io.openliberty.guides.system;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.provider.jsrjsonp.JsrJsonpProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SystemEndpointIT {

    private static String clusterUrl;

    private Client client;

    @BeforeAll
    public static void oneTimeSetup() {
        String nodePort = System.getProperty("system.http.port");
        clusterUrl = "http://localhost:" + nodePort + "/system/properties/";
    }

    @BeforeEach
    public void setup() {
        client = ClientBuilder.newBuilder()
                    .hostnameVerifier(new HostnameVerifier() {
                        public boolean verify(String hostname, SSLSession session) {
                            return true;
                        }
                    })
                    .build();
    }

    @AfterEach
    public void teardown() {
        client.close();
    }

    @Test
    public void testGetProperties() {
        Client client = ClientBuilder.newClient();
        client.register(JsrJsonpProvider.class);

        WebTarget target = client.target(clusterUrl);
        Response response = target.request().get();

        assertEquals(200, response.getStatus(),
            "Incorrect response code from " + clusterUrl);
        response.close();
    }

}