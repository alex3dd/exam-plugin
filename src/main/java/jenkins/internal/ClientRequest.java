/**
 * Copyright (c) 2018 MicroNova AG
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *     1. Redistributions of source code must retain the above copyright notice, this
 *        list of conditions and the following disclaimer.
 *
 *     2. Redistributions in binary form must reproduce the above copyright notice, this
 *        list of conditions and the following disclaimer in the documentation and/or
 *        other materials provided with the distribution.
 *
 *     3. Neither the name of MicroNova AG nor the names of its
 *        contributors may be used to endorse or promote products derived from
 *        this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package jenkins.internal;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import hudson.model.BuildListener;
import hudson.model.Executor;
import jenkins.internal.data.ExamStatus;
import jenkins.internal.data.TestConfiguration;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.PrintStream;

public class ClientRequest {

    private static String baseUrl = "";
    private static PrintStream logger;
    private static Client client = null;

    private final static int OK = Response.ok().build().getStatus();

    public static String getBaseUrl() {
        return baseUrl;
    }

    public static void setBaseUrl(String baseUrl) {
        ClientRequest.baseUrl = baseUrl;
    }

    public static PrintStream getLogger() {
        return logger;
    }

    public static void setLogger(PrintStream logger) {
        ClientRequest.logger = logger;
    }

    public ClientRequest(PrintStream logger, String baseUrl) {
        ClientRequest.baseUrl = baseUrl;
        ClientRequest.logger = logger;
    }

    public static ExamStatus getStatus() {
        if(client == null){
            logger.println("WARNING: no EXAM connected");
            return null;
        }
        WebResource service = client.resource(baseUrl + "/testrun/status");
        ClientResponse response = service.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON)
                .get(ClientResponse.class);

        if (response.getStatus() != OK) {
            throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
        }

        return response.getEntity(ExamStatus.class);
    }

    public static boolean isApiAvailable(){
        boolean clientCreated = false;
        boolean isAvailable = true;
        if(client == null){
            clientCreated = true;
            createClient();
        }
        try {
            getStatus();
        }catch (Exception e){
            isAvailable = false;
        }

        if(clientCreated){
            destroyClient();
        }
        return isAvailable;
    }

    public static void startTestrun(TestConfiguration testConfig) {
        if(client == null){
            logger.println("WARNING: no EXAM connected");
            return;
        }
        logger.println("starting testrun");
        WebResource service = client.resource(baseUrl + "/testrun/start");

        ClientResponse response = service.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON)
                .post(ClientResponse.class, testConfig);

        if (response.getStatus() != OK) {
            throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
        }
    }

    public static void stopTestrun(){
        if(client == null){
            logger.println("WARNING: no EXAM connected");
            return;
        }
        logger.println("stopping testrun");
        WebResource service = client.resource(baseUrl + "/testrun/stop?timeout=300");

        ClientResponse response = service.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON)
                                         .post(ClientResponse.class);

        if (response.getStatus() != OK) {
            throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
        }
    }

    public static void clearWorkspace(String projectName) {
        if(client == null){
            logger.println("WARNING: no EXAM connected");
            return;
        }
        WebResource service = null;
        if (projectName == null || projectName.isEmpty()) {
            logger.println("deleting all projects and pcode from EXAM workspace");
            service = client.resource(baseUrl + "/workspace/delete");
        } else {
            logger.println("deleting project and pcode for project \"" + projectName + "\" from EXAM workspace");
            service = client.resource(baseUrl + "/workspace/delete?projectName=" + projectName);
        }

        ClientResponse response = service.get(ClientResponse.class);

        if (response.getStatus() != OK) {
            throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
        }
    }

    public static void shutdown() {
        if(client == null){
            logger.println("WARNING: no EXAM connected");
            return;
        }
        logger.println("closing EXAM");
        client.resource(baseUrl + "/workspace/shutdown");

    }

    public static boolean connectClient(int timeout) {
        logger.println("connecting to EXAM");
        createClient();

        long timeoutTime = System.currentTimeMillis() + timeout;
        while (timeoutTime > System.currentTimeMillis()){
            if(isApiAvailable()){
                return true;
            }
        }
        logger.println("ERROR: EXAM does not answer in " + timeout + "ms");
        return false;
    }

    private static void createClient(){
        if (client == null) {
            ClientConfig clientConfig = new DefaultClientConfig();
            clientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
            client = Client.create(clientConfig);
        } else {
            logger.println("Client already connected");
        }
    }

    private static void destroyClient(){
        if(client != null) {
            client.destroy();
        }
        client = null;
    }

    public static void disconnectClient(int timeout) {
        if (client == null) {
            logger.println("Client is not connected");
        } else {
            logger.println("disconnect from EXAM");

            WebResource service = client.resource(baseUrl + "/workspace/shutdown");
            try {
                ClientResponse responseShutdown = service.get(ClientResponse.class);
            }catch (Exception e){
                logger.println(e.getMessage());
            }

            long timeoutTime = System.currentTimeMillis() + timeout;
            boolean shutdownOK = false;
            while (timeoutTime > System.currentTimeMillis()){
                if(!isApiAvailable()){
                    shutdownOK = true;
                    break;
                }
            }
            if (!shutdownOK) {
                logger.println("ERROR: EXAM does not shutdown in " + timeout + "ms");
            }

            destroyClient();
        }
    }



    public static void waitForTestrunEnds(Executor executor){
        boolean testDetected = false;
        while(true){
            if(executor.isInterrupted()){
                ClientRequest.stopTestrun();
                return;
            }
            ExamStatus status = ClientRequest.getStatus();
            if(!testDetected) {
                testDetected = "TestRun".equalsIgnoreCase(status.getJobName());
            }else{
                if(!status.getJobRunning()){
                    break;
                }
            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                // nothing to do
            }
        }
    }
}