<jsp:directive.include file="includes/top.jsp" />
<%
    int loginDelay = request.getIntHeader("LoginDelay");

%>
<div id="loginDelay">
    <h2>Kirjautuminen estetty</h2>

    <p>
        Olet yritt&auml;nyt kirjautumista liian monta kertaa. Ole hyv&auml; ja kokeile kirjautumista <%= loginDelay %> sekunnin p&auml;&auml;st&auml; uudelleen!
    </p>
</div>
<jsp:directive.include file="includes/bottom.jsp" />
