# hibernate-strict-violation-reproducer

## About

A reproducer for `org.hibernate.query.sqm.StrictJpaComplianceViolation` error message showing up while trying to perform a `from Person p` HQL clause. Its goal is to indicate the changed behavior of WildFly with regards to Hibernate (a strict JPQL compliance presense).

## How to run the reproducer

```
wget https://github.com/wildfly/wildfly/releases/download/27.0.0.Alpha4/wildfly-27.0.0.Alpha4.zip
mvn -Dserver.zip=wildfly-27.0.0.Alpha4.zip test
```