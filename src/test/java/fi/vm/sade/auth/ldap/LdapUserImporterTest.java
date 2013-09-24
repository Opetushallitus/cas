package fi.vm.sade.auth.ldap;

import org.jasig.cas.authentication.handler.PlainTextPasswordEncoder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.naming.directory.DirContext;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Antti Salonen
 */
@ContextConfiguration("classpath:test-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class LdapUserImporterTest {

    @Autowired
    private LdapContextSource contextSource;
    @Autowired
    LdapUserImporter ldapUserImporter;
    LdapServerMain ldap;
    int doneCount = 0;
    List<Exception> errors = new ArrayList<Exception>();
    int threads = 10;
    LdapUser user = new LdapUser("test", "test", "test", "test", "test" + "@oph.fi", "test", null, new String[]{"test"}, "fi");

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
    public void testEncryptionAndBind() throws Exception {
        ldapUserImporter.save(user);
        String dn = LdapUserImporter.buildDn("people", user.getDepartment(), user.getUid(), "uid").toString();
        String pwEncoded = new PlainTextPasswordEncoder().encode(user.getPassword());
        DirContext ctx = contextSource.getContext(dn, pwEncoded);
        ctx.close();
    }

    @Test
    public void testConcurrentLdapImportSave() throws Exception {
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

    @Test
    public void testAdminPass() throws Exception {
        String pwEncoded = new PlainTextPasswordEncoder().encode("secret");
        DirContext ctx = contextSource.getContext("uid=admin,ou=system", pwEncoded);
        ctx.close();
    }

}
