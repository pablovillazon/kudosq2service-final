package be.jkin.q2service.services;

import javax.persistence.EntityManagerFactory;

public class LuceneIndexConfig {
    public LuceneIndexServiceBean luceneIndexServiceBean(EntityManagerFactory entityManagerFactory)
    {
        LuceneIndexServiceBean luceneIndexServiceBean = new LuceneIndexServiceBean(entityManagerFactory);
        luceneIndexServiceBean.triggerIndexing();
        return luceneIndexServiceBean;
    }
}
