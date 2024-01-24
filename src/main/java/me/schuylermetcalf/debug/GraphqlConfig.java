package me.schuylermetcalf.debug;

import graphql.ErrorType;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import org.springframework.boot.autoconfigure.graphql.GraphQlSourceBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.SubscriptionExceptionResolverAdapter;

import java.util.List;
import java.util.Optional;

@Configuration
public class GraphqlConfig {

    @Bean
    public GraphQlSourceBuilderCustomizer gql(){
        return builder -> builder.subscriptionExceptionResolvers(List.of(new SubscriptionExceptionResolverAdapter(){

            @Override
            protected GraphQLError resolveToSingleError(Throwable exception) {
                var result = switch (exception){
                    case IllegalStateException lse -> Optional.of(GraphqlErrorBuilder.newError().errorType(ErrorType.ValidationError).message("Something went wrong, contact support").build());
                    default -> Optional.<GraphQLError>empty();
                };
                return result.orElse(null);
            }
        }));
    }
}
