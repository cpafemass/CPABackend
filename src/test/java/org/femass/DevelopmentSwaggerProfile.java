package org.femass;

import io.quarkus.test.junit.QuarkusTestProfile;

import java.util.Map;

public class DevelopmentSwaggerProfile implements QuarkusTestProfile {

    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.of(
                "quarkus.smallrye-openapi.enabled", "true",
                "quarkus.smallrye-openapi.info-title", "CPA Backend API",
                "quarkus.smallrye-openapi.info-version", "1.0.0",
                "quarkus.swagger-ui.always-include", "true",
                "quarkus.swagger-ui.path", "/swagger-ui"
        );
    }
}
