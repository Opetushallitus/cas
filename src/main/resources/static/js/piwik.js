var siteDomain = document.domain;
var piwikSiteId;
switch (siteDomain) {
    case "opintopolku.fi":
        piwikSiteId = 4;
        break;
    case "studieinfo.fi":
        piwikSiteId = 13;
        break;
    case "studyinfo.fi":
        piwikSiteId = 14;
        break;
    case "virkailija.opintopolku.fi":
        piwikSiteId = 3;
        break;
    case "testi.opintopolku.fi":
    case "testi.studieinfo.fi":
    case "testi.studyinfo.fi":
        piwikSiteId = 1;
        break;
    case "testi.virkailija.opintopolku.fi":
        piwikSiteId = 5;
        break;
    case "demo.opintopolku.fi":
    case "demo.studieinfo.fi":
    case "demo.studyinfo.fi":
        piwikSiteId = 15;
        break;
    default:
        piwikSiteId = 2; // Kehitys
}
var _paq = _paq || [];
_paq.push(["setDocumentTitle", document.domain + "/" + document.title]);
_paq.push(["trackPageView"]);
_paq.push(["enableLinkTracking"]);
if (piwikSiteId != 2) {
    (function() {
        var u=(("https:" == document.location.protocol) ? "https" : "http") + "://analytiikka.opintopolku.fi/piwik/";
        _paq.push(["setTrackerUrl", u+"piwik.php"]);
        _paq.push(["setSiteId", piwikSiteId]);
        var d=document, g=d.createElement("script"), s=d.getElementsByTagName("script")[0]; g.type="text/javascript";
        g.defer=true; g.async=true; g.src=u+"piwik.js"; s.parentNode.insertBefore(g,s);
    })();
}
