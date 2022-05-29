# Serverless Spring Boot Application Demo - Modifications

## AppDynamics

Deploy the demo to your AWS account using [AWS SAM](https://aws.amazon.com/serverless/sam/).

### Dependency

```xml
    <dependency>
      <groupId>com.appdynamics</groupId>
      <artifactId>lambda-tracer</artifactId>
      <version>20.03.1391</version>
    </dependency>
```

### Handler Changes

AppDynamics requires access to the `Context` from the Lambda handler. Extending the SpringBootStreamHandler allows for access to the `Context`.

```java
public class CustomHandler extends SpringBootStreamHandler {
  public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
    AppDynamics.getTracer(context);
    super.handleRequest(input, output, context);
  }
}
```

### ENV Variables

```yaml
APPDYNAMICS_ACCOUNT_NAME: redacted
APPDYNAMICS_AGENT_ACCOUNT_ACCESS_KEY: redacted
APPDYNAMICS_APPLICATION_NAME: demo
APPDYNAMICS_CONTROLLER_HOST: redacted.saas.appdynamics.com
APPDYNAMICS_SERVERLESS_API_ENDPOINT: https://fra-sls-agent-api.saas.appdynamics.com
#Stops it catching an exception
APPDYNAMICS_CONTROLLER_PORT: 443 
```

### Additional dependency

Java 11+ removed several EE modules. AppDynamics lambda tracer requires access to the jaxb api. 

```text
[AppDynamics Tracer] [ERROR]: ERROR registering tracer => java.lang.NoClassDefFoundError: javax/xml/bind/DatatypeConverter
```

To resolve you can include the below as a dependancy.

```xml
<dependency>
    <groupId>javax.xml.bind</groupId>
    <artifactId>jaxb-api</artifactId>
    <version>2.3.0</version>
</dependency>

```

### Marketplace Subscription

In order to use Lambda with AppDynamics via this method you need the following:

- Existing AWS Lambda functions implemented in Java
- Active Serverless APM for AWS Lambda subscription
- AppDynamics SaaS Controller version 4.5.11 or later
- An active AppDynamics Pro account.
- Your Controller access key. This requires you to be an AppDynamics Account Owner or have Administrator privileges.
- AWS Identity and Access Management (IAM) role with permission to one of the following policies:
  - AWSMarketplaceManageSubscriptions
  - AWSMarketplaceFullAccess

Active Serverless APM for AWS Lambda subscription is available as a Marketplace subscription: https://aws.amazon.com/marketplace/pp/prodview-o2wpz2p6wtyzw?qid=1574216250690&sr=0-1&ref_=srh_res_product_title

## Vault

### Hashicorp Cloud Vault

When using Vault in Hashicorp vault you will get an access denied message.
This issue is replicated on the forums but with no responses yet: https://discuss.hashicorp.com/t/try-to-use-vault-lambda-extension-access-vault-cloud-hcp-got-access-denied/30478

### Vault Layer Extension

Docs: https://www.vaultproject.io/docs/platform/aws/lambda-extension

#### Requirements & Installation

> - ARN of the role your Lambda runs as
> - An instance of Vault accessible from AWS Lambda
> - An authenticated vault client
> - A secret in Vault that you want your Lambda to access, and a policy giving read access to it
> - Your Lambda function must use one of the [supported runtimes][https://docs.aws.amazon.com/lambda/latest/dg/runtimes-extensions-api.html] for extensions
> - AWS Auth Method enabled [docs](https://www.vaultproject.io/docs/auth/aws)

**Example SAM config**
```diff
Globals:
  Function:
    Tracing: Active
    Runtime: java11
    Timeout: 30
    ...
    Layers:
+      - arn:aws:lambda:eu-west-1:634166935893:layer:vault-lambda-extension:13
```

#### Configuration

```yaml
VLE_VAULT_ADDR: https://xxx
VAULT_AUTH_ROLE: sam-app-GetProductsFunctionRole-6IWQETJTQC86
VAULT_AUTH_PROVIDER: aws
VAULT_SECRET_PATH_DB: database/creds/lambda-function
VAULT_LOG_LEVEL: debug
AWS_STS_REGIONAL_ENDPOINTS: regional
VAULT_SECRET_FILE_DB: /tmp/vault_secret.json
VAULT_STS_ENDPOINT_REGION: eu-west-1
```
Details: https://www.vaultproject.io/docs/platform/aws/lambda-extension#configuration

**Cache**

If you are using the local extension as a proxy then you can configure caching so you do not forward every request to Vault.

```text
VAULT_DEFAULT_CACHE_TTL: 15m
VAULT_DEFAULT_CACHE_ENABLED: true
```

#### STS Configuration

> You may need to configure the extension's STS client to also use the regional STS endpoint by setting AWS_STS_REGIONAL_ENDPOINTS=regional, because both the AWS Golang SDK and Vault IAM auth method default to using the global endpoint in many regions.

Source: https://www.vaultproject.io/docs/platform/aws/lambda-extension#aws-sts-client-configuration
#### Fail fast behaviour

When the vault extension fails to connect, authenticate, or fetch secrets at the configured path it fails to execute successfully.

### Spring Cloud Vault

You can use Spring Cloud Vault as normal.

#### Dependency

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-vault-config</artifactId>
</dependency>
```

#### Configuration

```yaml
spring:
  cloud:
    vault:
      enabled: true
      uri: ${vaultUrl}
      connection-timeout: 5000
      read-timeout: 15000
      fail-fast: true
      config:
        order: -10
      authentication: AWS_IAM
      aws-iam:
        role: vault-lambda-role
        aws-path: aws
        endpoint-uri: https://sts.eu-west-1.amazonaws.com
      kv:
        enabled: true
        backend: database
        profile-separator: '/'
        application-name: creds/lambda-function
  config:
    import: vault://
```

#### Fail fast behaviour
Only when the Spring Cloud fails to connect it fails to execute successfully.

It will continue to start and bring up a spring context when auth fails, or it can not fetch the required path. 
This can result in a polluted context that exists for multiple executions.

### Vault Extension & Spring Cloud Vault 

You can use the Vault Extension as a proxy, then utilise Spring Cloud Vault in order to access variables from within your application yaml files as normal.
This way it takes the authentication concerns away from the Java function.

#### Configuration
```yaml
spring:
  cloud:
    vault:
      enabled: true
      uri: http://127.0.0.1:8200
      connection-timeout: 5000
      read-timeout: 15000
      fail-fast: true
      config:
        order: -10
      kv:
        enabled: true
        backend: database
        profile-separator: '/'
        application-name: creds/lambda-function
      authentication: none
  config:
    import: vault://
```

#### Fail fast behaviour
When the vault extension fails to connect, authenticate, or fetch secrets at the configured path it fails to execute successfully.

Spring will fail only if it fails to connect to 

## GraalVM

### Spring Cloud Vault

At the moment this is not supported. See GitHub issue: https://github.com/spring-projects-experimental/spring-native/issues/1366

### Vault Lambda Layer

#### Setting variables from API requests

To be explored

#### Setting

To be explored
