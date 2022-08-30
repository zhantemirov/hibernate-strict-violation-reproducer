package org.jboss.qa.ejb.tests.transactions;

import java.util.List;
import jakarta.ejb.Stateful;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import org.jboss.logging.Logger;

@Stateful
public class TransactionalBeanStateful implements TransactionalBeanRemote {

    private static final Logger logger = Logger.getLogger(TransactionalBeanStateful.class.getName());

    @PersistenceContext(unitName = "TestingPU")
    private EntityManager em;

    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void createPerson() {
        logger.info("Creating a new Person entity");
        em.persist(new Person());
    }

    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<Person> getPersonList() {
        //uncomment this following line to make the test pass
        //final TypedQuery<Person> allPersonsQuery = em.createQuery("select p from Person p", Person.class);
        final TypedQuery<Person> allPersonsQuery = em.createQuery("from Person p", Person.class);
        final List<Person> resultList = allPersonsQuery.getResultList();
        logger.info("Getting person list, size = " + resultList.size());
        return resultList;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void clean() {
        final Query deleteQuery = em.createQuery("delete from Person p");
        final int deleted = deleteQuery.executeUpdate();
        logger.info("Deleted " + deleted + " persons from db");
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public String getNode() {
        final String node = System.getProperty("jboss.node.name");
        logger.info("Method getNode invoked on node " + node);
        return node;
    }

}
