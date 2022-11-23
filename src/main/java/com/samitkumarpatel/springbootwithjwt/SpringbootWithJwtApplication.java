package com.samitkumarpatel.springbootwithjwt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import reactor.core.publisher.Mono;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@SpringBootApplication
@EnableCaching
@EnableScheduling
@Slf4j
@RequiredArgsConstructor
public class SpringbootWithJwtApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringbootWithJwtApplication.class, args);
	}
	private final Db db;
	private final CacheManager cacheManager;


	@Bean
	public RouterFunction router() {
		return RouterFunctions
				.route(GET("/one"), request -> ok().body(Mono.just("SUCCESS"), String.class))
				.andRoute(POST("/two"), request -> ok().body(fetch(request.queryParam("number").get()), String.class));
	}

	public Mono<String> fetch(String number) {
		log.info("self method for {}", number);
		Number n = Number.valueOf(number);
		return db.db(n);
	}

	@Scheduled(fixedRate = 60000)
	public void evictAllcachesAtIntervals() {
		cacheManager.getCacheNames().stream()
				.forEach(cacheName -> {
					log.info("Cache Name: {} clear", cacheName);
					//cacheManager.getCache(cacheName).evictIfPresent(cacheName);
					cacheManager.getCache(cacheName).clear();
				});
	}
}

@Configuration
class Config {
	@Bean
	public WebClient tokenWebClient() {
		return WebClient.builder().baseUrl("http://localhost:8111").build();
	}
}

@Service
@Slf4j
class Db {
	@Cacheable(cacheNames = "db")
	public Mono<String> db(Number number) {
		log.info("db method for {}",number);
		return switch (number) {
			case ONE -> Mono.just("Number One");
			case TWO -> Mono.just("Number Two");
			case THREE -> Mono.just("Number Three");
			default -> Mono.just("No Number");
		};
	}
}

enum Number {
	ONE,TWO,THREE
}