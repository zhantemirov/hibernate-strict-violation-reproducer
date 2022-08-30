package org.jboss.qa.ejb.tests.transactions;

import java.io.File;
import java.io.IOException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import com.google.common.base.Strings;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.impl.base.exporter.zip.ZipExporterImpl;
import org.wildfly.extras.creaper.commands.deployments.Deploy;
import org.wildfly.extras.creaper.commands.deployments.Undeploy;
import org.wildfly.extras.creaper.commands.logging.ChangeConsoleLogHandler;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineOptions;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.OnlineOptions;

public class Utils {

	public static void deploy(Archive<?> shrinkWrapArchive, OnlineManagementClient client)
			throws CommandFailedException {
		Deploy deployCommand = new Deploy.Builder(
				new ZipExporterImpl(shrinkWrapArchive).exportAsInputStream(),
				shrinkWrapArchive.getName(),
				true).build();
		try {
			client.apply(deployCommand);
		} catch(CommandFailedException cfe) {
			if(!cfe.getMessage().toLowerCase().contains("duplicate resource"))
				throw cfe;
		}
	}

	public static void undeploy(String archive, OnlineManagementClient client) throws CommandFailedException {
		Undeploy undeployCommand = new Undeploy.Builder(archive).build();
		try {
			client.apply(undeployCommand);
		} catch(CommandFailedException cfe) {
			if(!cfe.getMessage().toLowerCase().contains("not found"))
				throw cfe;
		}
	}

	public static OnlineManagementClient createCreaper(String nodeHostname, Integer port) {
		return ManagementClient
				.onlineLazy(OnlineOptions.standalone().hostAndPort(nodeHostname, port).build());
	}

	public static void safeCloseEjbClientContext(InitialContext ejbCtx) {
		try {
			Context ejbContext = (Context) ejbCtx.lookup("ejb:");
			if ( ejbContext != null ) {
				ejbContext.close();
			}
		} catch (NamingException e) {
			e.printStackTrace();
		}
		try {
			ejbCtx.close();
		} catch (NamingException e) {
			e.printStackTrace();
		}
	}

	public static void setLoggerPrefix(String prefix, String jbossHome, String configurationFile)
			throws IOException, CommandFailedException {
		final OfflineManagementClient offlineCreaper = ManagementClient.offline(
				OfflineOptions.standalone().baseDirectory(new File(jbossHome + File.separator + "standalone"))
						.configurationFile(configurationFile).build());
		String actualPrefix = Strings.isNullOrEmpty(prefix) ? "" : (prefix + ": ");
		offlineCreaper.apply(
				new ChangeConsoleLogHandler.Builder("CONSOLE")
						.patternFormatter(actualPrefix + "%d{HH:mm:ss,SSS} %-5p [%c] (%t) %s%e%n").build()
		);
	}
}
