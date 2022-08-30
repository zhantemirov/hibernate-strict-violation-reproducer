package org.jboss.qa.ejb.tests.transactions;

import java.net.UnknownHostException;

public class Containers {

    public static final Container NODE1;

    static {
        try {
            NODE1 = new Container("node1",
                    System.getProperty("node1.address"),
                    Integer.parseInt(System.getProperty("node1.application-port")),
                    Integer.parseInt(System.getProperty("node1.management-port")),
                    System.getProperty("node1.jbossHome"),
                    "standalone.xml");

        } catch (Exception e) {
            e.printStackTrace(); // log it so we can see it in the output
            throw new RuntimeException(e);
        }
    }

    public static class Container {

        public final String nodeName;
        public final String bindAddress;
        public final Integer applicationPort;
        public final Integer managementPort;
        public final String homeDirectory;
        public final String urlOfHttpRemotingConnector;
        public final String configurationXmlFile;

        Container(String nodeName, String bindAddress, Integer applicationPort, Integer managementPort,
                  String homeDirectory, String configuration) throws UnknownHostException {
            this.nodeName = nodeName;
            this.bindAddress = bindAddress;
            this.applicationPort = applicationPort;
            this.managementPort = managementPort;
            this.homeDirectory = homeDirectory;
            this.urlOfHttpRemotingConnector = "remote+http://" + bindAddress + ":" + applicationPort;
            this.configurationXmlFile = configuration;
        }

        @Override
        public String toString() {
            return nodeName;
        }
    }
}
