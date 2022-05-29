// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package software.amazonaws.example.product.product.handler;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.http.HttpStatusCode;
import software.amazonaws.example.product.product.dao.ProductDao;

import java.util.function.Function;

@Component

public class GetAllProductsFunction implements Function<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
  ProductDao productDao;
  ObjectMapper objectMapper;

  Logger logger = LogManager.getLogger(GetAllProductsFunction.class);

  @Autowired
  public void setProductDao(ProductDao productDao) {
    this.productDao = productDao;
  }

  @Autowired
  public void setObjectMapper(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Value("${topSecretValue}")
  String secretValue;

  @Override
  public APIGatewayProxyResponseEvent apply(APIGatewayProxyRequestEvent requestEvent) {

    logger.info("TopSecret: " + secretValue);
    if (!requestEvent.getHttpMethod().equals(HttpMethod.GET.name())) {
      return new APIGatewayProxyResponseEvent()
        .withStatusCode(HttpStatusCode.METHOD_NOT_ALLOWED)
        .withBody("Only GET method is supported");
    }
    try {
      return new APIGatewayProxyResponseEvent()
        .withStatusCode(HttpStatusCode.OK)
        .withBody(objectMapper.writeValueAsString(secretValue));
    } catch (JsonProcessingException je) {
      je.printStackTrace();
      return new APIGatewayProxyResponseEvent()
        .withStatusCode(HttpStatusCode.INTERNAL_SERVER_ERROR)
        .withBody("Internal Server Error");
    }
  }
}
