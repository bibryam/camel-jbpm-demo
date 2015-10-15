Camel-jBPM Component Demo
=====================================
This is a [JBoss Fuse](http://www.jboss.org/products/fuse/overview/) and [JBoss BPM Suite](http://www.jboss.org/products/bpmsuite/overview/) integration demo. It shows how Camel errors are handled and passed to BPMS for Complex Event Processing and Human Task creation using [camel-jbpm](http://camel.apache.org/jbpm.html) connector.
You can read a more detailed blog post with instructions about this demo [here](http://www.ofbizian.com/)

####Supported Operations
To see the full list of supported operations by the component check the official [Camel documentations](http://camel.apache.org/jbpm.html). In this demo we do only start processes and pass process parameters.

####Running the Demo with JBoss Fuse 6.2 and JBoss BPMS
The Demo connects Camel Routes running on Fuse with Business Processes running on BPMS. It consists of two distinct parts and to run the demo end to end, you will need to setup both applications. 

- **Run BPMS and deploy a Business Process:** Download and run BPMS. I use my [Docker image](https://github.com/bibryam/dockerfiles/tree/master/eap-bpms) for this purpose. Then you have to create and deploy a BPMN process to handle errors. For this purpose I've created another project with the process definition. The easiest way is to clone the [project](https://github.com/bibryam/camel-human-task-cep-jbpm-repo.git) directly from BPMS web console and follow the instructions to deploy it. The project evaluates the error coming Fuse using CEP and if there are more than 5 errors in 10 seconds, it marks the error as critical and creates Human Tasks for someone review the errors. If the errors doesn't qualify as critical, they are logged and no Human Task is created.


- **Run Fuse and deploy a Camel Route:** Download and run Fuse 6.2. Then build and deploy the Camel route from this project with the commands below. The route has an error handler that catches exceptions and starts business process in BPMS by passing all the necessary details about the exception:  contextId,  routeId, endpointId, exchangeId, breadcrumbId, exceptionType, errorMessage. Once cloned and build. the Camel route can be deployed with the following commands:

    JBossFuse:admin@root> features:addurl mvn:org.apache.camel.karaf/apache-camel/2.16.0/xml/features  
    JBossFuse:admin@root> features:install camel-jbpm  
    JBossFuse:admin@root> install -s mvn:com.ofbizian/camel-jbpm-demo/1.0.0  
    JBossFuse:admin@root> log:tail  

 
####Example Route

    <route id="START_PROCESS_ROUTE">
        <from uri="timer://foo?fixedRate=true&amp;period=10000"/>
        <log loggingLevel="INFO" message="Starting a jBPM process"/>
        <setHeader headerName="CamelJBPMProcessId">
            <constant>project1.camel.demo</constant>
        </setHeader>
        <to uri="jbpm:http://127.0.0.1:8080/business-central?userName=bpmsAdmin&amp;password=pa$word1&amp;deploymentId=org.kie.example:camel-process:1.0.0-SNAPSHOT"/>
        <convertBodyTo type="java.lang.String"/>
        <to uri="log:com.ofbizian.jbpm.after?showAll=true&amp;multiline=true"/>
    </route>

####License
ASLv2
