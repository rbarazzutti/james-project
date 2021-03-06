/****************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 ****************************************************************/

package org.apache.james.webadmin.routes;

import static com.jayway.restassured.RestAssured.when;
import static org.apache.james.webadmin.WebAdminServer.NO_CONFIGURATION;
import static org.apache.james.webadmin.routes.ErrorRoutes.INTERNAL_SERVER_ERROR;
import static org.apache.james.webadmin.routes.ErrorRoutes.JSON_EXTRACT_EXCEPTION;
import static org.apache.james.webadmin.utils.ErrorResponder.ErrorType.INVALID_ARGUMENT;
import static org.apache.james.webadmin.utils.ErrorResponder.ErrorType.SERVER_ERROR;
import static org.eclipse.jetty.http.HttpStatus.BAD_REQUEST_400;
import static org.eclipse.jetty.http.HttpStatus.INTERNAL_SERVER_ERROR_500;
import static org.eclipse.jetty.http.HttpStatus.NOT_FOUND_404;
import static org.hamcrest.Matchers.equalTo;

import org.apache.james.metrics.api.NoopMetricFactory;
import org.apache.james.webadmin.WebAdminServer;
import org.apache.james.webadmin.WebAdminUtils;
import org.apache.james.webadmin.utils.ErrorResponder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.jayway.restassured.RestAssured;

public class ErrorRoutesTest {
    private static final String NOT_FOUND = "notFound";

    private static WebAdminServer webAdminServer;

    @BeforeClass
    public static void setUp() throws Exception {
        webAdminServer = WebAdminUtils.createWebAdminServer(
                new NoopMetricFactory(),
                new ErrorRoutes());
        webAdminServer.configure(NO_CONFIGURATION);
        webAdminServer.await();

        RestAssured.requestSpecification = WebAdminUtils.buildRequestSpecification(webAdminServer)
                .setBasePath(ErrorRoutes.BASE_URL)
                .build();
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @AfterClass
    public static void tearDown() {
        webAdminServer.destroy();
    }

    @Test
    public void defineInternalErrorShouldReturnInternalErrorJsonFormat() {
        when()
            .get(INTERNAL_SERVER_ERROR)
        .then()
            .statusCode(INTERNAL_SERVER_ERROR_500)
            .body("statusCode", equalTo(INTERNAL_SERVER_ERROR_500))
            .body("type", equalTo(SERVER_ERROR.getType()))
            .body("message", equalTo("WebAdmin encountered an unexpected internal error"));
    }

    @Test
    public void defineNotFoundShouldReturnNotFoundJsonFormat() {
        when()
            .get(NOT_FOUND)
        .then()
            .statusCode(NOT_FOUND_404)
            .body("statusCode", equalTo(NOT_FOUND_404))
            .body("type", equalTo(ErrorResponder.ErrorType.NOT_FOUND.getType()))
            .body("message", equalTo("GET /errors/notFound can not be found"));
    }

    @Test
    public void defineJsonExtractExceptionShouldReturnBadRequestJsonFormat() throws InterruptedException {
        when()
            .get(JSON_EXTRACT_EXCEPTION)
        .then()
            .statusCode(BAD_REQUEST_400)
            .body("statusCode", equalTo(BAD_REQUEST_400))
            .body("type", equalTo(INVALID_ARGUMENT.getType()))
            .body("message", equalTo("JSON payload of the request is not valid"))
            .body("details", equalTo("Unrecognized token 'a': was expecting ('true', 'false' or 'null')\n at [Source: a non valid JSON; line: 1, column: 2]"));
    }
}
