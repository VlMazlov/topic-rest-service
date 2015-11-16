package ru.vlmazlov.test;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * Created by vlmazlov on 14.11.15.
 */
@Configuration
@EnableAutoConfiguration
@EnableWebMvc
@ComponentScan("ru.vlmazlov.test")
@PropertySource(value = "topic-rest-service.properties", ignoreResourceNotFound = true)
public class TopicServiceRestConfiguration
{
    @Value("${ru.vlmazlov.test.restPort:2000}")
    private int port;

    @Bean
    public static PropertySourcesPlaceholderConfigurer placeholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public EmbeddedServletContainerFactory servletContainer() {
        TomcatEmbeddedServletContainerFactory factory = new TomcatEmbeddedServletContainerFactory();
        factory.setPort(port);
        return factory;
    }

    public static void main(String[] args)
    {
        SpringApplication.run(TopicServiceRestConfiguration.class, args);
    }
}
