package be.jkin.q2service;

import be.jkin.q2service.model.Kudos;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class Q2serviceApplicationTests {

	private static final Logger log = LoggerFactory.getLogger(Q2serviceApplicationTests.class);

	@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
	@Autowired
	private EntityManager entityManager;

	@Test
	public void contextLoads() {
	}


	@Test
	public void testQueryIndex(){
		FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);

		org.hibernate.search.query.dsl.QueryBuilder queryBuilder = fullTextEntityManager.getSearchFactory()
				.buildQueryBuilder()
				.forEntity(Kudos.class)
				.get();

		org.apache.lucene.search.Query query = queryBuilder.keyword()
				.onField("texto").matching("anton")
				.createQuery();

		org.hibernate.search.jpa.FullTextQuery fullTextQuery = fullTextEntityManager.createFullTextQuery(query, Kudos.class);

		List<Kudos> kudos = fullTextQuery.getResultList();
		log.info("Found kudos:"+kudos);

	}


}
