package be.jkin.q2service;

import be.jkin.q2service.services.LuceneIndexConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Import;

@EntityScan({"be.jkin.q2service.model"})
@SpringBootApplication
@Import(LuceneIndexConfig.class)
public class Q2serviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(Q2serviceApplication.class, args);
	}

}
