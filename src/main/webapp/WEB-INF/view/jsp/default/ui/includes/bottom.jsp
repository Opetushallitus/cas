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
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

</div>
            

            <!--
                <div id="footer" class="fl-panel fl-note fl-bevel-white fl-font-size-80">
                	<a id="jasig" href="http://www.jasig.org" title="go to Jasig home page"></a>
                    <div id="copyright">
                        <p>Copyright &copy; 2005 - 2012 Jasig, Inc. All rights reserved.</p>
                        <p>Powered by <a href="http://www.jasig.org/cas">Jasig Central Authentication Service <%=org.jasig.cas.CasVersion.getVersion()%></a></p>
                    </div>
                </div>
            -->
            </div>
            <footer id="footer" class="offset-left-16-1 grid16-14">
                <div class="offset-left-16-2 grid16-3 padding-vertical-5">
                    <img src="<c:url value='/img/OPH_logo.svg' />" />
                </div>
                <div class="grid16-3 padding-vertical-5">
                    <address class="address">
                        Opetushallitus <br />
                        Hakaniemenranta 6 <br />
                        PL 380, 00531 Helsinki <br />
                        puhelin 029 533 1000 
                    </address>
                </div>
                <div class="grid16-3 padding-vertical-5">
                    <img src="<c:url value='/img/OKM_logo.png' />" />
                </div>
                <div class="grid16-3 padding-vertical-5">
                    <address class="address">
                        Opetus- ja kulttuuriministeri&ouml; <br />
                        PL 29, 00023 valtioneuvosto <br />
                        puhelin 0295 3 30004
                    </address>
                </div>
                

            </footer>
            <div class="clear"></div>

        </div>
        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.4.2/jquery.min.js"></script>
        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jqueryui/1.8.5/jquery-ui.min.js"></script>
        <script type="text/javascript" src="<c:url value="/js/cas.js" />"></script>
		
		<!-- Piwik -->
        <script type="text/javascript">

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
                case "virkailija.studieinfo.fi":
                case "virkailija.studyinfo.fi":
                    piwikSiteId = 3;
                    break;
                case "testi.opintopolku.fi":
                case "testi.studieinfo.fi":
                case "testi.studyinfo.fi":
                    piwikSiteId = 1;
                    break;
                case "testi.virkailija.opintopolku.fi":
                case "testi.virkailija.studieinfo.fi":
                case "testi.virkailija.studyinfo.fi":
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

			//console.log("siteDomain:"+siteDomain+", piwikSiteId:"+piwikSiteId);

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
</html>





 