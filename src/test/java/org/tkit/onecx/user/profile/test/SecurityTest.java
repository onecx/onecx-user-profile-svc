package org.tkit.onecx.user.profile.test;

import java.util.List;

import org.tkit.quarkus.security.test.AbstractSecurityTest;
import org.tkit.quarkus.security.test.SecurityTestConfig;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class SecurityTest extends AbstractSecurityTest {
    @Override
    public SecurityTestConfig getConfig() {
        SecurityTestConfig config = new SecurityTestConfig();
        config.addConfig("read", "/internal/userProfiles/id", 404, List.of("ocx-up:read"), "get");
        config.addConfig("write", "/internal/userProfiles", 400, List.of("ocx-up:write"), "post");
        return config;
    }
}
