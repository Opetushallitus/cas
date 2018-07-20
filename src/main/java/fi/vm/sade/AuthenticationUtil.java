package fi.vm.sade;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.gson.Gson;
import fi.vm.sade.auth.clients.KayttooikeusRestClient;
import fi.vm.sade.auth.clients.OppijanumerorekisteriRestClient;
import fi.vm.sade.auth.dto.HenkiloOmattiedotDto;
import fi.vm.sade.auth.dto.KayttooikeusOikeudetDto;
import fi.vm.sade.auth.dto.KayttooikeusOmatTiedotDto;
import fi.vm.sade.auth.ldap.LdapUser;
import fi.vm.sade.auth.ldap.LdapUserImporter;

public class AuthenticationUtil {
    private OppijanumerorekisteriRestClient oppijanumerorekisteriRestClient;

    private KayttooikeusRestClient kayttooikeusRestClient;

    private LdapUserImporter ldapUserImporter;

    private Gson gson;

    public LdapUserImporter getLdapUserImporter() {
        return ldapUserImporter;
    }

    public void setLdapUserImporter(LdapUserImporter ldapUserImporter) {
        this.ldapUserImporter = ldapUserImporter;
    }

    public String getUserRoles(String uid) {
        KayttooikeusOmatTiedotDto kayttooikeusOmatTiedotDto = this.kayttooikeusRestClient.getOmattiedot(uid)
                .orElseThrow(() -> new RuntimeException("Username not found"));
        HenkiloOmattiedotDto henkiloOmattiedotDto = this.oppijanumerorekisteriRestClient
                .getOmattiedot(kayttooikeusOmatTiedotDto.getOidHenkilo());

        return this.mapToRolesJson(kayttooikeusOmatTiedotDto, henkiloOmattiedotDto);
    }

    private String mapToRolesJson(KayttooikeusOmatTiedotDto kayttooikeusOmatTiedotDto, HenkiloOmattiedotDto henkiloOmattiedotDto) {
        List<String> roles = kayttooikeusOmatTiedotDto.getOrganisaatiot().stream()
                .flatMap(organisaatio -> organisaatio.getKayttooikeudet().stream()
                        .sorted(Comparator.comparing(KayttooikeusOikeudetDto::getPalvelu)
                                .thenComparing(KayttooikeusOikeudetDto::getOikeus))
                        .flatMap(kayttooikeusDto -> Stream.of(
                                "APP_" + kayttooikeusDto.getPalvelu(),
                                "APP_" + kayttooikeusDto.getPalvelu()
                                        + "_" + kayttooikeusDto.getOikeus(),
                                "APP_" + kayttooikeusDto.getPalvelu()
                                        + "_" + kayttooikeusDto.getOikeus()
                                        + "_" + organisaatio.getOrganisaatioOid())
                        ))
                .distinct()
                .collect(Collectors.toList());
        roles.addAll(Arrays.asList(
                "LANG_" + henkiloOmattiedotDto.getAsiointikieli(),
                "USER_" + kayttooikeusOmatTiedotDto.getUsername(),
                kayttooikeusOmatTiedotDto.getKayttajaTyyppi()));
        return this.gson.toJson(roles);
    }
    
    public List<String> getRoles(String uid) {
        String member = ldapUserImporter.getMemberString(uid);
        return ldapUserImporter.getUserLdapGroups(member);
    }
    
    public LdapUser getUser(String uid) {
        return ldapUserImporter.getLdapUser(uid);
    }

    public OppijanumerorekisteriRestClient getOppijanumerorekisteriRestClient() {
        return oppijanumerorekisteriRestClient;
    }

    public void setOppijanumerorekisteriRestClient(OppijanumerorekisteriRestClient oppijanumerorekisteriRestClient) {
        this.oppijanumerorekisteriRestClient = oppijanumerorekisteriRestClient;
    }

    public KayttooikeusRestClient getKayttooikeusRestClient() {
        return kayttooikeusRestClient;
    }

    public void setKayttooikeusRestClient(KayttooikeusRestClient kayttooikeusRestClient) {
        this.kayttooikeusRestClient = kayttooikeusRestClient;
    }

    public Gson getGson() {
        return gson;
    }

    public void setGson(Gson gson) {
        this.gson = gson;
    }
}
