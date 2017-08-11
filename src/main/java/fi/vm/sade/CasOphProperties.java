package fi.vm.sade;

import fi.vm.sade.properties.OphProperties;
import java.nio.file.Paths;

public class CasOphProperties extends OphProperties {

    public CasOphProperties() {
        addFiles("/cas-oph.properties");
        addOptionalFiles(Paths.get(System.getProperties().getProperty("user.home"), "/oph-configuration/common.properties").toString());
        addOptionalFiles(Paths.get(System.getProperties().getProperty("user.home"), "/oph-configuration/cas.properties").toString());
    }

}
