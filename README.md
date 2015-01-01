README.txt
===

This is Rollarcus: my experimentatal fork of [Apache Roller](https://github.com/apache/roller).

This branch of Rollarcus __jaxrs_not_struts__ adds a REST API to Roller, 
authentication by Shiro and REST API testing via Arquillian and Embedded Tomcat.

Status
---

Currently, there is one end-point called /weblogs that is protected by HTTP
Basic Authentication. There is also one JUnit test that tests the end-point.
The test runs as a Failsafe Integration test. To run the test you need:

* Tomcat 7 installed on your system 
* CATALINA_HOME environment variable set to point to Tomcat directory
* Derby client, mail and activation jars in $CATALINA_HOME/lib

The test will be run when you run a normal build like this:

    mvn install

If you want to run only the REST test and nothing else then use this voodoo:

    mvn verify -Dit.test=org.apache.roller.weblogger.rest.WeblogsEndpointIT -Dtest=foo -DfailIfNoTests=false

That's all
---

Want to contribute? Shoot me a PR.