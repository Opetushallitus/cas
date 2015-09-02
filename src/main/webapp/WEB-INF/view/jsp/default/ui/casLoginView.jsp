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
    </div>
</header>


<div class="grid16-11 offset-left-16-2 margin-vertical-5">
   
</div>


<div class="clear margin-bottom-3"></div>

<div class="offset-left-16-2 grid16-6">
    <h1 class="margin-bottom-3 margin-top-0">
        Opintopolku - Virkailijan ty&ouml;p&ouml;yt&auml;
    </h1>
    <p class="margin-bottom-5">
Ty&ouml;p&ouml;yd&auml;lle kirjaudutaan tunnuksilla, jotka saa esimerkiksi oman organisaation Opintopolku-vastuu/p&auml;&auml;k&auml;ytt&auml;j&auml;lt&auml;. Ty&ouml;p&ouml;yd&auml;ll&auml; n&auml;kyv&auml;t virkailijapalvelut riippuvat k&auml;ytt&auml;j&auml;n k&auml;ytt&ouml;oikeuksista.   
   </p>
      <h1 class="margin-bottom-3 margin-top-1">
        Studieinfo - Administrat&ouml;rens arbetsbord
    </h1>

  <p class="margin-bottom-3">
P&aring; arbetsbordet loggar man in med anv&auml;ndarkoder, som man t.ex. f&aring;r av den egna organisationens ansvariga anv&auml;ndare eller huvudanv&auml;ndare. Anv&auml;ndarr&auml;ttigheterna avg&ouml;r vilka tj&auml;nster syns p&aring; arbetsbordet.  
 </p>

</div>
<div class="offset-left-16-2 grid16-4" id="login">
    <form:form method="post" cssClass="fm-v clearfix" commandName="${commandName}" htmlEscape="true">
        <form:errors path="*" id="msg" cssClass="notification warning" element="div" />

        <div class="form-item">
            <label for="username" class="form-label">K&auml;ytt&auml;j&auml;tunnus:</label>
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
            <label for="password" class="form-label">Salasana:</label>
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
            <a href="/registration-ui/html/index.html#/forgotPassword">Unohtuiko salasana / Har du gl&ouml;mt ditt l&ouml;senord?</a>
        </div>

    </form:form>

	<div class="margin-vertical-2">
	    <a href="<c:url value="${hakaUrl}" />"><img src="<c:url value='/img/haka_landscape_medium.gif' /> "/></a>
	</div>

	<div class="grid16-11 offset-right-16-1 margin-vertical-2">
		<div><a href="<c:url value="https://opintopolku.fi/wp/fi/rekisteriseloste/" />">Tietosuojaseloste /</a> <a href="<c:url value="https://studieinfo.fi/wp/dataskyddsbeskrivning/" />">Dataskyddsbeskrivning</a></div>
	</div>

</div>

<div class="clear margin-bottom-4"></div>


<jsp:directive.include file="includes/bottom.jsp" />

