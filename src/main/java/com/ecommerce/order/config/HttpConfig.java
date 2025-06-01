package com.ecommerce.order.config;

import com.ecommerce.order.client.ProductHttpInterface;
import com.ecommerce.order.client.UserHttpInterface;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.client.support.RestTemplateAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import org.springframework.web.util.DefaultUriBuilderFactory;

@Configuration
public class HttpConfig {
    @Bean
    @LoadBalanced
    public RestClient.Builder getRestClientBuilder(){
        return RestClient.builder();
    }

    @Bean
    public ProductHttpInterface restClient(RestClient.Builder restClientBuilder){
        RestClient restClient = restClientBuilder.baseUrl("http://product-service").build();
        RestClientAdapter adapter = RestClientAdapter.create(restClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
        return factory.createClient(ProductHttpInterface.class);
    }

    @Bean
    public UserHttpInterface restTemplate(RestClient.Builder restClientBuilder){
        RestClient restClient = restClientBuilder.baseUrl("http://user-service").build();
        RestClientAdapter adapter = RestClientAdapter.create(restClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
        return factory.createClient(UserHttpInterface.class);
    }

}
