package org.jboss.qa.ejb.tests.transactions;

import static org.jboss.qa.ejb.tests.transactions.Containers.NODE1;
import static org.jboss.qa.ejb.tests.transactions.Utils.createCreaper;
import static org.jboss.qa.ejb.tests.transactions.Utils.deploy;
import static org.jboss.qa.ejb.tests.transactions.Utils.safeCloseEjbClientContext;
import static org.jboss.qa.ejb.tests.transactions.Utils.setLoggerPrefix;
import static org.jboss.qa.ejb.tests.transactions.Utils.undeploy;
import java.util.Properties;
import javax.naming.Context;
import javax.naming.InitialContext;
import jakarta.transaction.UserTransaction;
import org.jboss.arquillian.container.test.api.ContainerController;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.naming.client.WildFlyInitialContextFactory;

@SuppressWarnings({"ArquillianDeploymentAbsent"})
@RunWith(Arquillian.class)
@RunAsClient
public class TransactionsStatefulBeanTestCase {
	private static final JavaArchive deployment = createDeployment();
	private static OnlineManagementClient managementClient;

	@ArquillianResource
	private ContainerController containerController;

	public static final String TRANSACTIONAL_BEAN_LOOKUP =
			"ejb:/transactions/" + TransactionalBeanStateful.class.getSimpleName() + "!"
					+ TransactionalBeanRemote.class.getName() + "?stateful";


	public static JavaArchive createDeployment() {
		final JavaArchive jar = ShrinkWrap.create(JavaArchive.class, "transactions.jar");
		jar.addClasses(TransactionalBeanStateful.class, TransactionalBeanRemote.class, Person.class);
		jar.addAsManifestResource(ClassLoader.getSystemResource(
				"org/jboss/qa/ejb/tests/transactions/persistence.xml"), "persistence.xml");
		return jar;
	}

	@Before
	public void before() throws Exception {
		setLoggerPrefix("NODE1", NODE1.homeDirectory, NODE1.configurationXmlFile);
		containerController.start(NODE1.nodeName);

		managementClient = createCreaper(NODE1.bindAddress, NODE1.managementPort);
		deploy(deployment, managementClient);
	}

	@Test
	public void manyCallsPerTransactionAndCommit() throws Exception {
		final Properties properties = new Properties();
		properties.put(Context.INITIAL_CONTEXT_FACTORY, WildFlyInitialContextFactory.class.getName());
		properties.put(Context.PROVIDER_URL, NODE1.urlOfHttpRemotingConnector);

		final InitialContext ejbCtx = new InitialContext(properties);
		try {
			final TransactionalBeanRemote bean = (TransactionalBeanRemote) ejbCtx
					.lookup(TRANSACTIONAL_BEAN_LOOKUP);
			final UserTransaction tx = (UserTransaction) ejbCtx.lookup("txn:UserTransaction");

			try {
				tx.begin();
				Assert.assertEquals("Correct node affinity should be followed",
						NODE1.nodeName, bean.getNode());
				for (int i = 0; i < 100; i++) {
					bean.createPerson();
				}
			} finally {
				tx.commit();
			}

			Assert.assertEquals(100, bean.getPersonList().size());
			bean.clean();
			Assert.assertEquals(0, bean.getPersonList().size());
		} finally {
			safeCloseEjbClientContext(ejbCtx);
		}
	}

	@After
	public void cleanup() throws Exception {
		containerController.start(NODE1.nodeName);
		undeploy(deployment.getName(), managementClient);

		containerController.stop(NODE1.nodeName);
		setLoggerPrefix("", NODE1.homeDirectory, NODE1.configurationXmlFile);
	}
}
