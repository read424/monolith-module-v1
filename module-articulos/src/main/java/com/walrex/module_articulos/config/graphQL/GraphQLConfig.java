package com.walrex.module_articulos.config.graphQL;

import graphql.schema.Coercing;
import graphql.schema.GraphQLScalarType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Configuration
public class GraphQLConfig {

    @Bean
    public RuntimeWiringConfigurer runtimeWiringConfigurer() {
        return wiringBuilder -> wiringBuilder
            .scalar(GraphQLScalarType.newScalar()
                .name("Date")
                .description("Una fecha en formato ISO (YYYY-MM-DD)")
                .coercing(new Coercing<Object, Object>() {
                    @Override
                    public String serialize(Object dataFetcherResult) {
                        if (dataFetcherResult instanceof LocalDate) {
                            return ((LocalDate) dataFetcherResult).format(DateTimeFormatter.ISO_LOCAL_DATE);
                        }
                        return null;
                    }

                    @Override
                    public LocalDate parseValue(Object input) {
                        return LocalDate.parse((String) input, DateTimeFormatter.ISO_LOCAL_DATE);
                    }

                    @Override
                    public LocalDate parseLiteral(Object input) {
                        return LocalDate.parse(input.toString(), DateTimeFormatter.ISO_LOCAL_DATE);
                    }
                })
                .build());
    }
}
