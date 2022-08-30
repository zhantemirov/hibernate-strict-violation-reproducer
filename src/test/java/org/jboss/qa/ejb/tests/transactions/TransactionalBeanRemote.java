package org.jboss.qa.ejb.tests.transactions;

import java.util.List;
import jakarta.ejb.Remote;

@Remote
public interface TransactionalBeanRemote {

    void createPerson();

    List<Person> getPersonList();

    void clean();

    String getNode();
}
