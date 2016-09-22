<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%--

    Licensed to Jasig under one or more contributor license
    agreements. See the NOTICE file distributed with this work
    for additional information regarding copyright ownership.
    Jasig licenses this file to you under the Apache License,
    Version 2.0 (the "License"); you may not use this file
    except in compliance with the License.  You may obtain a
    copy of the License at the following location:

      http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.

--%>

<head>
    <meta charset="UTF-8">
    <link href="<c:url value='/app/app.css'/>" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css?family=Open+Sans:400,600,700" rel="stylesheet">
    <title>Webpack App</title>
</head>

<body data-openid="${sessionScope.openIdLocalId}"
      data-executionKey="${flowExecutionKey}"
      data-loginTicket="${loginTicket}"
      data-hakaUrl="${hakaUrl}"
      data-loginError="${loginError}">
<div id="app"></div>
<script src="<c:url value='/app/app.js' />"></script>

<!-- Piwik -->
<script type="text/javascript">

    var siteDomain = document.domain;
    var piwikSiteId = 2;
    if(siteDomain=='opintopolku.fi'){
        piwikSiteId = 4;
    }else if(siteDomain=='virkailija.opintopolku.fi'){
        piwikSiteId = 3;
    }else if(siteDomain=='testi.opintopolku.fi'){
        piwikSiteId = 1;
    }else if(siteDomain=='testi.virkailija.opintopolku.fi'){
        piwikSiteId = 5;
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
</script>
<!-- End Piwik Code -->
</body>

