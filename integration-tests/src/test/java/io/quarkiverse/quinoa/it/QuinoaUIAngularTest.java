package io.quarkiverse.quinoa.it;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import java.net.URL;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;

import io.quarkiverse.quinoa.testing.QuarkusPlaywrightManager;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@TestProfile(TestProfiles.AngularTests.class)
@QuarkusTestResource(QuarkusPlaywrightManager.class)
public class QuinoaUIAngularTest {

    @QuarkusPlaywrightManager.InjectPlaywright
    BrowserContext context;

    @TestHTTPResource("/")
    URL index;

    @TestHTTPResource("/some-route")
    URL someRoute;

    @TestHTTPResource("/bar/foo/not-found")
    URL url404;

    @TestHTTPResource("/bar/foo/api/quinoa")
    URL api;

    @Test
    public void testUIIndex() {
        checkUrl(index);
        checkUrl(someRoute);
    }

    private void checkUrl(URL url) {
        final Page page = context.newPage();
        Response response = page.navigate(url.toString());
        Assertions.assertEquals("OK", response.statusText());

        page.waitForLoadState();

        String title = page.title();
        Assertions.assertEquals("QuinoaApp", title);

        // Make sure the component loaded and hits the backend
        String button = page.innerText(".content span");
        Assertions.assertEquals("quinoa-app app is running!", button);

        String greeting = page.innerText(".quinoa");
        Assertions.assertEquals("Hello Quinoa", greeting);
    }

    @Test
    public void test404Endpoint() {
        given()
                .when().get(url404)
                .then()
                .statusCode(404);
    }

    @Test
    public void testHelloEndpoint() {
        given()
                .when().get(api)
                .then()
                .statusCode(200)
                .body(is("Hello Quinoa"));
    }
}
