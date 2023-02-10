# CAS integraatio

Opintopolun palveluiden autentikaatio pohjautuu [CAS](https://www.apereo.org/cas) [protokollaan](https://apereo.github.io/cas/6.6.x/protocol/CAS-Protocol.html). Ulkoiset järjestelmät voivat tunnistautua käyttäen CAS:n [rest-rajapintaa](https://apereo.github.io/cas/6.6.x/protocol/REST-Protocol.html).

Tunnistautumisen jälkeen palvelut huolehtivat sessionhallinnastaan itse. Uloskirjautumien tapahtuu CAS:in [SLO]() toiminnon avulla joka invalidoi session palvelimelta. Huom: Selaimeen asetettuja evästeitä yms. ei voida näin poistaa joten on palvelun vastuulla pitää huolta ettei selaimen välimuistiin jää arkaluonteiseksi katsottavaa informaatiota.

Palvelukokonaisuuden autentikaatioon kannattaa katsoa mallia referenssitoteutuksista joita löytyy useille eri ohjelmointikielille

| Ohjelmointikieli | Linkki                                                                   |
| ---------------- | ------------------------------------------------------------------------ |
| Java             | https://github.com/Opetushallitus/java-utils/tree/master/java-cas        |
| Scala (2.11)     | https://github.com/Opetushallitus/scala-utils/tree/master/scala-cas_2.11 |
| Scala (2.12)     | https://github.com/Opetushallitus/scala-utils/tree/master/scala-cas_2.12 |
| Clojure          | https://github.com/Opetushallitus/clj-util/tree/master/clj-cas           |

Eduuni wikistä löytyy lukuisia aihetta käsittelivä artikkeleita (huom. mahdollinen pääsynrajaus).

- https://wiki.eduuni.fi/display/ophpolku/CAS-Oppijaan+integroituminen
- https://wiki.eduuni.fi/display/ophpolku/Rajapintojen+autentikaatio
- https://wiki.eduuni.fi/display/OPHPALV/Autentikaatiopalvelu+CAS

## Kirjautuminen

Login (GET) eli session muodostaminen palveluun tehdään samaan kirjautumisrajapintaan kuin logout (POST). Kirjautumisrajapinnan osoite välitetään CAS:ille **service** request parametrin avulla.

### Sisäänkirjautuminen

Mikäli pyydetty resurssi vaatii kirjautumista ohjaa palvelu käyttäjän CAS:iin. CAS huolehtii SSO session olemassaolosta ja palauttaa käyttäjän palvelun kirjautumisrajapintaan HTTP GET pyynnöllä jonka mukana on tarvittava tiketti (ST) jonka validoinnin yhteydessä palvelu saa selvitettyä tarvittavat käyttäjätiedot.
Tämä tarvitsee tehdä vain kerran jonka jälkeen palvelu hoitaa sessionhallinan itse (alla olevassa sekvenssikaaviossa esimerkinomaisesti perinteinen JSESSIONID eväste).

```mermaid
sequenceDiagram
    actor Käyttäjä;
    participant Selain;
    participant CAS;
    participant Palvelu 1;
    participant Palvelu 2;

    alt SSO: Ei, Sessio: Ei

        Käyttäjä ->> Selain: Avaa palvelu;
        Selain ->> Palvelu 1: GET palvelu1;
        activate Palvelu 1;
            Note right of Palvelu 1: Autentikaatio puuttuu.<br>Uudelleenohjaus CAS:iin URL<br>enkoodatuilla service parametrilla
            Palvelu 1 ->> Selain: HTTP 302 location cas/login?service=palvelu1;
        deactivate Palvelu 1;

        Selain ->> CAS: GET cas/login?service=palveu1;
        activate CAS;
            note right of CAS: Ei SSO sessiota<br>Näytä kirjautumislomake
            CAS ->> Selain: CAS kirjautumissivu;
        deactivate CAS;

        Selain ->> Käyttäjä: Kirjautumislomake;
        activate Käyttäjä;
            note right of Käyttäjä: Tunnistaudu
            Käyttäjä -> Selain: Lähetä lomake
        deactivate Käyttäjä;

        Selain ->> CAS: POST cas/login?service=palvelu1;
        activate CAS;
            CAS ->> CAS: Autentikoi;
            CAS ->> Selain: Set-cookie: CASTGC=TGT-123<br>HTTP 302 location: palvelu1?ticket=ST-456
        deactivate CAS;

        Selain ->> Palvelu 1: GET palvelu1?ticket=ST-456;
        activate Palvelu 1;
            note right of Palvelu 1: Validoi tiketti;
            Palvelu 1 ->> CAS: GET cas/serviceValidate?service=palvelu1&ticket=ST-456;
            activate CAS;
                note right of CAS: Validoi tiketin ja palauttaa<br>käyttäjätiedot XML dokumenttina;
                CAS ->> Palvelu 1: HTTP 200 [XML];
            deactivate CAS;
            note right of Palvelu 1: Luo sessio ja uudelleen ohjaa palveluun<br>(piilottaa tiketin)<br>Huom: tässä esimerkinomaisesti JSESSIONID.<br>Pavelu hoitaa oman sessionhallintansa
            Palvelu 1 ->> Selain: Set-cookie: JSESSIONID=foo<br>HTTP 302 location palvelu1;
        deactivate Palvelu 1;

        Selain ->> Palvelu 1: Cookie: JSESSIONID=foo<br>GET palvelu1;
        activate Palvelu 1;
            Palvelu 1 ->> Palvelu 1: Validoi session;
            Palvelu 1 ->> Selain: HTTP 200 [content];
        deactivate Palvelu 1;

        Selain ->> Käyttäjä: Näytä palvelu 1;

    else SSO: Ok, Sessio: Ok

        Käyttäjä ->> Selain: Hae resurssi
        Selain ->> Palvelu 1: Cookie: JSESSIONID=foo<br>GET palvelu1/resurssi
        activate Palvelu 1;
            note right of Palvelu 1: Validoi pyynnön mukana tullut<br>sessioeväste;
            Palvelu 1 ->> Selain: HTTP 200 [resurssi]
        deactivate Palvelu 1;
        Selain ->> Käyttäjä: Näuytä resurssi;

    else SSO: Ok, Sessio: Ei

        Käyttäjä ->> Selain: Avaa palvelu 2;
        Selain ->> Palvelu 2: GET palvelu2;
        activate Palvelu 2;
            Note right of Palvelu 2: Autentikaatio puuttuu.<br>Uudelleenohjaus CAS:iin URL<br>enkoodatuilla service parametrilla
            Palvelu 2 ->> Selain: HTTP 302 location cas/login?service=palvelu2;
        deactivate Palvelu 2;

        Selain ->> CAS: Cookie: CASTGC=TGT-123<br>GET cas/login?service=palvelu2;
        activate CAS;
            note right of CAS: Valido TGT tiketti<br>SSO sessio OK<br>Ei uudelleenkirjautumista
            CAS ->> Selain: HTTP 302 location: palvelu2?ticket=ST-789
        deactivate CAS;

        Selain ->> Palvelu 2: GET palvelu2?ticket=ST-789;
        activate Palvelu 2;
            note right of Palvelu 2: Validoi tiketti;
            Palvelu 2 ->> CAS: GET cas/serviceValidate?service=palvelu2&ticket=ST-789;
            activate CAS;
                note right of CAS: Validoi tiketin ja palauttaa<br>käyttäjätiedot XML dokumenttina;
                CAS ->> Palvelu 2: HTTP 200 [XML];
            deactivate CAS;
            note right of Palvelu 2: Luo sessio ja uudelleen ohjaa palveluun<br>(piilottaa tiketin)
            Palvelu 2 ->> Selain: Set-cookie: JSESSIONID=bar<br>HTTP 302 location palvelu2;
        deactivate Palvelu 2;

        Selain ->> Palvelu 2: Cookie: JSESSIONID=bar<br>GET palvelu2;
        activate Palvelu 2;
            Palvelu 2 ->> Palvelu 2: Validoi session;
            Palvelu 2 ->> Selain: HTTP 200 [content];
        deactivate Palvelu 2;

        Selain ->> Käyttäjä: Näytä palvelu 2;
    end

```

### Uloskirjautuminen

Palvelussa **ei** ole käytössä frontend [logout](https://apereo.github.io/cas/6.6.x/installation/Logout-Single-Signout.html) strategiaa vaan uloskirjautuminen tapahtuu puhtaasti taustajärjestelmissä.

CAS pitää kirjaa kaikista palveluista joihin on kirjauduttu SSO session aikana. Logout pyynnön tapahtuessa CAS lähettää jokaisen palvelun kirjautumisrajapintaan HTTP POST pyynnön joka sisältää tarvittavat tiedot (ST) jotta palvelu kykenee invalidoimaan ko. session.

Huomionarvoista on että logout tapahtuu palveluiden välillä ja näin ollen selaimesta ei saada siivottua esim. palvelukohtaisia evästeitä. Toteuttajan vastuulla on pitää huolta ettei välimuistiin jää arkaluonteiseksi katsottavaa informaatiota.

```mermaid
sequenceDiagram
    actor Käyttäjä;
    participant Selain;
    participant CAS;
    participant Palvelu 1;
    participant Palvelu 2..N;

    Käyttäjä ->> Selain: Click logout;
    Selain ->> Palvelu 1: GET palvelu1/logout
    Palvelu 1 ->> Selain: HTTP 302 cas/logout
    Selain ->> CAS: GET cas/logout
    loop Palvelu 2..N
        CAS ->> Palvelu 2..N: POST logout [XML]
        note right of Palvelu 2..N: Palvelu invalidoi käyttäjän session.<br>Esim. sessioeväste ei anna enää pääsyä palveluun.
        Palvelu 2..N ->> CAS: HTTP 200
    end
    CAS ->> Selain: HTTP 200
    Selain ->> Käyttäjä: Logout näkymä (login)
```

## Huomioita

- Palvelun kirjautumisrajapinta välitetään CAS:ille **service** parametrissä
- Palveluiden välillä kulkeva **service** parametri tulee olla asianmukaisesti URL enkoodattu
- Palveluiden väliset pyynnöt tarvitsevat asianmukaiset **Caller-Id** ja **CSRF** headerit.
- CAS hyväksyy palvelupyyntöjä vain sallituista osoitteista
