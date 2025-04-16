package io.vladprotchenko.authapi.config;

import feign.Response;
import feign.codec.ErrorDecoder;
import io.vladprotchenko.authapi.exception.CustomFeignException;

public class CustomFeignErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        int statusCode = response.status();
        String errorMessage = "Unknown error occurred";
        if (response.body() != null) {
            try {
                errorMessage = response.body().toString();
            } catch (Exception e) {
                errorMessage = "Failed to extract error message";
            }
        }
        return new CustomFeignException(errorMessage, statusCode);
    }
}
