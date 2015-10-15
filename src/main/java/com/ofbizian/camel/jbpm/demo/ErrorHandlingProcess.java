/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ofbizian.camel.jbpm.demo;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;

/**
 * User: bibryam
 * Date: 14/10/15
 */
public class ErrorHandlingProcess extends RouteBuilder {

    public void configure() {

        onException(Exception.class)
                .handled(true)
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        final Map map = new HashMap();
                        map.putAll(exchange.getIn().getHeaders());
                        map.put("contextId", exchange.getContext().getName());
                        map.put("routeId", exchange.getProperty(Exchange.FAILURE_ROUTE_ID));
                        map.put("endpointId", exchange.getProperty(Exchange.FAILURE_ENDPOINT));
                        map.put("exchangeId", exchange.getExchangeId());
                        map.put("breadcrumbId", exchange.getIn().getHeader(Exchange.BREADCRUMB_ID));
                        map.put("exceptionType", exchange.getProperty(Exchange.EXCEPTION_CAUGHT).getClass());
                        map.put("errorMessage", exchange.getProperty(Exchange.EXCEPTION_CAUGHT).toString());
                        exchange.getOut().setHeader("CamelJBPMParameters", map);
                    }
                })
                .to("jbpm:http://localhost:8080/business-central?userName=bpmsAdmin&password=pa$word1&deploymentId=org.kie.example:camel-process:1.0.0-SNAPSHOT&processId=project1.camel.demo")
                .convertBodyTo(String.class)
                .to("log:com.ofbizian.jbpm.handled?showAll=true&multiline=true");

        from("timer://foo?fixedRate=true&period=500&repeatCount=10").routeId("mainRoute")
                .to("log:com.ofbizian.jbpm.before?showAll=true&multiline=true")
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        throw new RuntimeException("Something went wrong");
                    }
                })
                .to("log:com.ofbizian.jbpm.after?showAll=true&multiline=true")
        ;
    }
}
