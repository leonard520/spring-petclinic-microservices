/*
 * Copyright 2002-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.customers;

import com.netflix.discovery.EurekaClient;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.http.RestTemplateDiscoveryClientOptionalArgs;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
/**
 * @author Maciej Szarlinski
 */
@EnableDiscoveryClient
@SpringBootApplication
public class CustomersServiceApplication {
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private EurekaClient discoveryClient;

    @Bean
    public RestTemplateDiscoveryClientOptionalArgs discoveryClientOptionalArgs() throws Exception {
        RestTemplateDiscoveryClientOptionalArgs args = new RestTemplateDiscoveryClientOptionalArgs();
        args.setHostnameVerifier(NoopHostnameVerifier.INSTANCE);
        args.setTransportClientFactories(new CustomRestTemplateTransportClientFactories());
        return args;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
	public static void main(String[] args) {
		SpringApplication.run(CustomersServiceApplication.class, args);
	}
}
