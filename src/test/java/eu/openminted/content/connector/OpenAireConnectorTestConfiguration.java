package eu.openminted.content.connector;

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

@Configuration
public class OpenAireConnectorTestConfiguration {
    @Bean
    public PropertyPlaceholderConfigurer propertyPlaceholderConfigurer() throws IOException {
        final PropertyPlaceholderConfigurer ppc = new PropertyPlaceholderConfigurer();
        ppc.setLocation(new ClassPathResource("application.properties"));
//        ppc.setLocations((Resource[]) ArrayUtils.addAll(
//                new PathMatchingResourcePatternResolver().getResources("classpath:application.properties"),
//                new PathMatchingResourcePatternResolver().getResources("classpath:test.properties")
//                )
//        );

        return ppc;
    }

}
