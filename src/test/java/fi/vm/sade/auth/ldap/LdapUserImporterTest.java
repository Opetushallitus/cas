package fi.vm.sade.auth.ldap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Antti Salonen
 */
@ContextConfiguration("classpath:test-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class LdapUserImporterTest {

    @Autowired
    LdapUserImporter ldapUserImporter;
    LdapServerMain ldap;
    int doneCount = 0;
    List<Exception> errors = new ArrayList<Exception>();
    int threads = 10;

    @Before
    public void start() throws Exception {
        ldap = new LdapServerMain("target/data/ldap_test_" + System.currentTimeMillis(), 10399, "secret", "dev");
        ldap.startServer();
    }

    @After
    public void stop() throws Exception {
        ldap.stopServer();
    }

    @Test
    public void testConcurrentSave() throws Exception {
        String name = "test";
        final LdapUser user = new LdapUser(name, name, name, name, name + "@oph.fi", name, null, new String[]{name}, "fi");
        for (int i = 0; i < threads; i++) {
            new Thread() {
                @Override
                public void run() {
                    try {
                        ldapUserImporter.save(user);
                    } catch (Exception e) {
                        //e.printStackTrace();
                        errors.add(e);
                    } finally {
                        doneCount++;
                    }
                }
            }.start();
        }
        while (doneCount < threads) {
            Thread.sleep(100);
        }
        if (errors.size() > 0) {
            throw errors.get(0);
        }
    }

}
