package me.schuylermetcalf.debug;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.graphql.ResponseError;
import org.springframework.graphql.client.SubscriptionErrorException;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.graphql.test.tester.HttpGraphQlTester;
import org.springframework.graphql.test.tester.WebSocketGraphQlTester;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GqlSubscriptionExceptionsDebugApplicationTests {
    @LocalServerPort
    private int port;

    @Value("http://localhost:${local.server.port}/graphql")
    private String baseUrl;

    @Value("http://localhost:${local.server.port}/subscriptions")
    private String subcriptionUrl;

    private GraphQlTester graphQlTester;
    private WebTestClient client;
    private WebSocketGraphQlTester subcriptionTester;

    @BeforeEach
    void setup(){
        this.client = WebTestClient.bindToServer().baseUrl(baseUrl).build();
        this.graphQlTester = HttpGraphQlTester.create(this.client);

        this.subcriptionTester = WebSocketGraphQlTester.builder(URI.create(subcriptionUrl), new ReactorNettyWebSocketClient())
                .build();
    }


    @Test
    void contextLoads() {
    }

    @Test
    @DisplayName("it can handle errors on subscriptions")
    public void itCanHandleErrorsOnSubscriptions() {
        // language=GraphQL
        var op = """
            subscription ErrorsAtSomePoint{
                greetings
            }
        """;

        subcriptionTester.document(op)
                .executeSubscription()
                .toFlux()
                .as(StepVerifier::create)
                .expectNextCount(3)
                .consumeErrorWith(err -> assertThat(err).isInstanceOf(SubscriptionErrorException.class)
                        .extracting(SubscriptionErrorException.class::cast)
                        .satisfies(e -> assertThat(e.getErrors()).hasSize(1).map(ResponseError::getMessage).singleElement().isEqualTo("Something went wrong, contact support")))
                .verify();
    }

}
