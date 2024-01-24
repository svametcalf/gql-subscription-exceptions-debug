package me.schuylermetcalf.debug;

import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Controller
public class GreetingController {

    @SubscriptionMapping
    Flux<String> greetings(){
        return Flux.just("one", "two", "three", "boom")
                .filterWhen(s -> s.equals("boom") ? Mono.error(new IllegalStateException("It went boom")) : Mono.just(true));
    }
}
