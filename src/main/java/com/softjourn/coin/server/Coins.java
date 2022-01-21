package com.softjourn.coin.server;

import com.softjourn.common.spring.aspects.logging.EnableLoggingAspect;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;

@SpringBootApplication
@ComponentScan("com.softjourn.coin.server")
@EnableResourceServer
@EnableAspectJAutoProxy
@EnableLoggingAspect
@EnableJpaRepositories(basePackages = "com.softjourn.coin.server.repository")
@EntityScan(basePackages = "com.softjourn.coin.server.entity")
@PropertySources(
		@PropertySource(value = "file:${user.home}/.coins/application.properties", ignoreResourceNotFound = true) // TODO: Do we need this property source. If yes - add variable for path.
)
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
