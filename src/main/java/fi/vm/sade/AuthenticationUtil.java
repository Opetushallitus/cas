package fi.vm.sade;

import java.util.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.gson.Gson;
import fi.vm.sade.auth.clients.KayttooikeusRestClient;
import fi.vm.sade.auth.clients.OppijanumerorekisteriRestClient;
import fi.vm.sade.auth.dto.HenkiloOmattiedotDto;
import fi.vm.sade.auth.dto.KayttooikeusOikeudetDto;
import fi.vm.sade.auth.dto.KayttooikeusOmatTiedotDto;
import fi.vm.sade.auth.ldap.LdapUser;

public class AuthenticationUtil {
    private OppijanumerorekisteriRestClient oppijanumerorekisteriRestClient;

    private KayttooikeusRestClient kayttooikeusRestClient;

    private Gson gson;

    public String getUserRoles(String uid) {
        KayttooikeusOmatTiedotDto kayttooikeusOmatTiedotDto = this.kayttooikeusRestClient.getOmattiedot(uid)
                .orElseThrow(() -> new RuntimeException("Username not found"));
        HenkiloOmattiedotDto henkiloOmattiedotDto = this.oppijanumerorekisteriRestClient
                .getOmattiedot(kayttooikeusOmatTiedotDto.getOidHenkilo());

        return this.mapToRolesJson(kayttooikeusOmatTiedotDto, henkiloOmattiedotDto);
    }

    private String mapToRolesJson(KayttooikeusOmatTiedotDto kayttooikeusOmatTiedotDto, HenkiloOmattiedotDto henkiloOmattiedotDto) {
        List<String> roles = this.getGroups(kayttooikeusOmatTiedotDto);
        roles.addAll(Arrays.asList(
                "LANG_" + henkiloOmattiedotDto.getAsiointikieli(),
                "USER_" + kayttooikeusOmatTiedotDto.getUsername(),
                kayttooikeusOmatTiedotDto.getKayttajaTyyppi()));
        roles = roles.stream().filter(Objects::nonNull).collect(Collectors.toList());
        return this.gson.toJson(roles);
    }

    private List<String> getGroups(KayttooikeusOmatTiedotDto kayttooikeusOmatTiedotDto) {
        return kayttooikeusOmatTiedotDto.getOrganisaatiot().stream()
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
    }

    public LdapUser getUser(String uid) {
        KayttooikeusOmatTiedotDto kayttooikeusOmatTiedotDto = this.kayttooikeusRestClient.getOmattiedot(uid)
                .orElseThrow(() -> new RuntimeException("Username not found"));
        HenkiloOmattiedotDto henkiloOmattiedotDto = this.oppijanumerorekisteriRestClient
                .getOmattiedot(kayttooikeusOmatTiedotDto.getOidHenkilo());
        LdapUser ldapUser = new LdapUser();
        ldapUser.setOid(kayttooikeusOmatTiedotDto.getOidHenkilo());
        ldapUser.setFirstName(henkiloOmattiedotDto.getKutsumanimi());
        ldapUser.setLastName(henkiloOmattiedotDto.getSukunimi());
        ldapUser.setLang(henkiloOmattiedotDto.getAsiointikieli());
        ldapUser.setUid(kayttooikeusOmatTiedotDto.getUsername());
        ldapUser.setRoles(this.mapToRolesJson(kayttooikeusOmatTiedotDto, henkiloOmattiedotDto));
        List<String> groups = this.getGroups(kayttooikeusOmatTiedotDto);
        groups.addAll(Arrays.asList(
                "LANG_" + henkiloOmattiedotDto.getAsiointikieli(),
                kayttooikeusOmatTiedotDto.getKayttajaTyyppi()));
        ldapUser.setGroups(groups.toArray(new String[0]));
        return ldapUser;
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
