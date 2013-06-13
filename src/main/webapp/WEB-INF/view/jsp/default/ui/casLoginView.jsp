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
<jsp:directive.include file="includes/top.jsp" />

<header id="siteheader" class="width-100">
    <div class="header-content">
        <img class="margin-left-2" src="<c:url value='/img/opintopolkufi.png' /> "/>

        <a class="float-right margin-right-2" href="#">P&aring; svenska</a>
        <span class="float-right margin-right-1">|</span>
        <a class="bold float-right margin-right-1" href="#">Suomeksi</a>

    </div>
</header>


<div class="grid16-11 offset-left-16-2 margin-vertical-5">
   
</div>


<div class="clear margin-bottom-3"></div>

<div class="offset-left-16-2 grid16-6">
    <h1 class="margin-bottom-3 margin-top-0">
        Opintopolku - Yll&auml;pidon ty&ouml;p&ouml;yt&auml;
    </h1>
    <p class="margin-bottom-3">
        Yll&auml;pidon s&auml;hk&ouml;isell&auml; ty&ouml;p&ouml;yd&auml;ll&auml; voit hallinnoida Opintopolku.fi:ss&auml; n&auml;ytett&auml;vi&auml; koulutuksia ja organisaatiosi tietoja sek&auml; k&auml;sitell&auml; hakemuksia ja paikan vastaanottajia. Julkinen, kaikille tarkoitettu palvelu l&ouml;ytyy syyskuusta 2013 alkaen osoitteesta
        <a href="www.opintopolku.fi">www.opintopolku.fi</a>
    </p>
    <p class="margin-bottom-3">
        Tunnukset yll&auml;pidon ty&ouml;p&ouml;yd&auml;lle saat tarvittaessa organisaatiosi p&auml;&auml;k&auml;ytt&auml;j&auml;lt&auml;. Eri oppilaitosten     yll&auml;pit&auml;j&auml;t n&auml;kev&auml;t ty&ouml;p&ouml;yd&auml;ll&auml; osin eri palveluita k&auml;ytt&ouml;oikeuksiensa mukaan.
    </p>
    <p>
        Yll&auml;pidon ty&ouml;p&ouml;yt&auml; on kehittyv&auml; palvelu. Ensimm&auml;isess&auml; vaiheessa ty&ouml;p&ouml;yd&auml;lt&auml; l&ouml;ytyv&auml;t opiskelijaksi ottamisen kannalta v&auml;ltt&auml;m&auml;tt&ouml;m&auml;t palvelut ja ohjeet koulutustarjonnan sy&ouml;tt&auml;misest&auml; hakemusten k&auml;sittelyyn. My&ouml;hemmin ty&ouml;p&ouml;yd&auml;lle saadaan my&ouml;s opetussuunnitelmien ja tutkintojen perusteet s&auml;hk&ouml;isess&auml; muodossa, ryhm&auml;ty&ouml;tiloja sek&auml; tiedonl&auml;hteit&auml;.
    </p>


</div>
<div class="offset-left-16-2 grid16-4" id="login">
    <form:form method="post" cssClass="fm-v clearfix" commandName="${commandName}" htmlEscape="true">
        <form:errors path="*" id="msg" cssClass="notification warning" element="div" />

        <div class="form-item">
            <label for="username" class="form-label"><spring:message code="screen.welcome.label.netid" /></label>
            <c:if test="${not empty sessionScope.openIdLocalId}">
                <strong>${sessionScope.openIdLocalId}</strong>
                <input type="hidden" id="username" name="username" value="${sessionScope.openIdLocalId}" />
            </c:if>

            <c:if test="${empty sessionScope.openIdLocalId}">
                <spring:message code="screen.welcome.label.netid.accesskey" var="userNameAccessKey" />
                <div class="form-item-content">
                    <form:input cssClass="required" cssErrorClass="error" id="username" size="25" tabindex="1" accesskey="${userNameAccessKey}" path="username" autocomplete="false" htmlEscape="true" />
                </div>
            </c:if>
        </div>

        <div class="form-item">
            <label for="password" class="form-label"><spring:message code="screen.welcome.label.password" /></label>
            <%--
                           NOTE: Certain browsers will offer the option of caching passwords for a user.  There is a non-standard attribute,
                           "autocomplete" that when set to "off" will tell certain browsers not to prompt to cache credentials.  For more
                           information, see the following web page:
                           http://www.geocities.com/technofundo/tech/web/ie_autocomplete.html
                           --%>
            <spring:message code="screen.welcome.label.password.accesskey" var="passwordAccessKey" />
                <div class="form-item-content">
                    <form:password cssClass="required" cssErrorClass="error" id="password" size="25" tabindex="2" path="password"  accesskey="${passwordAccessKey}" htmlEscape="true"  />
                </div>
            <%--autocomplete="off"--%>
        </div>

        <div class="row btn-row">
            <input type="hidden" name="lt" value="${loginTicket}" />
            <input type="hidden" name="execution" value="${flowExecutionKey}" />
            <input type="hidden" name="_eventId" value="submit" />

            <input class="button small primary" name="submit" accesskey="l" value="<spring:message code="screen.welcome.button.login" />" tabindex="4" type="submit" />
        </div>

    </form:form>



</div>

<div class="clear margin-bottom-4"></div>


<jsp:directive.include file="includes/bottom.jsp" />

