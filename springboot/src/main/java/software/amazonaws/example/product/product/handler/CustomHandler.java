package software.amazonaws.example.product.product.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.appdynamics.serverless.tracers.aws.api.AppDynamics;
import org.springframework.cloud.function.adapter.aws.SpringBootStreamHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class CustomHandler extends SpringBootStreamHandler {
  public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
    AppDynamics.getTracer(context);
    super.handleRequest(input, output, context);
  }
}
