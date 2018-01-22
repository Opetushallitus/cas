package org.jasig.cas;

import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = StrongIdentificationRequiringCentralAuthenticationServiceImpl.class)
public class StrongIdentificationRequiringCentralAuthenticationServiceImplTest {
    @Autowired
    StrongIdentificationRequiringCentralAuthenticationServiceImpl strongIdentificationRequiringCentralAuthenticationService;

    private UsernamePasswordCredentials usernamePasswordCredentials;

    @Before
    public void setup() {
        this.usernamePasswordCredentials = new UsernamePasswordCredentials();
        this.usernamePasswordCredentials.setUsername("username");
        this.usernamePasswordCredentials.setPassword("password");
    }

    @Test
    public void test() throws Exception {
        this.strongIdentificationRequiringCentralAuthenticationService.checkStrongIdentificationHook(this.usernamePasswordCredentials);
    }
}
