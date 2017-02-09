package com.softjourn.coin.server;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;

@SpringBootApplication
@ComponentScan(basePackages = "com.softjourn.coin.server")
@EnableResourceServer
@EnableAspectJAutoProxy
@EnableJpaRepositories(basePackages = "com.softjourn.coin.server.repository")
@EntityScan(basePackages = "com.softjourn.coin.server.entity")
public class Coins  {

	public static void main(String[] args) {
		SpringApplication.run(Coins.class, args);
	}

	public static class ServletInit extends SpringBootServletInitializer {

		@Override
		protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
			return application.sources(Coins.class);
		}
	}
}
