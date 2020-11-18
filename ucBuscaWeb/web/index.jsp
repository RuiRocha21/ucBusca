<%--
  Created by IntelliJ IDEA.
  User: ruirocha
  Date: 24/11/2019
  Time: 18:51
  To change this template use File | Settings | File Templates.
--%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri = "http://java.sun.com/jsp/jstl/functions" prefix = "fn" %>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
         pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
  <head>
    <title>ucBusca</title>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.3.1/jquery.min.js"></script>
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js"></script>
    <style><%@include file="/css/index.css"%></style>
  </head>
  <body>
    <div>
      <img src="ucBusca.jpg" width="100%" height="50%" id="logo">
    </div>



    <a href="<s:url action="paginalogin" />"><button type="button" class="btn btn-primary" id = btn_login>Login</button></a>

    <div id="pesquisa" >
      <s:form action="pesquisaSemRegisto" method="post">
        <s:textfield name="tokens" label="termos a pesquisar"/>
        <s:submit type="button" label="Pesquisar" />
      </s:form>
    </div>

    <div id = "resultados">
      <c:forEach items="${resultado}" var="res">
        <c:choose>
          <c:when test="${res.geInfoPesquisa() !=null}">
            <c:if test = "${fn:startsWith(res.geInfoPesquisa(), 'http')}">
              <a href = "<c:url value = "${res.geInfoPesquisa()}"/>">${res.geInfoPesquisa()}</a>
            </c:if>
            <c:if test = "${!fn:startsWith(res.geInfoPesquisa(), 'http')}">
              <p>${res.geInfoPesquisa()}</p>
            </c:if>

          </c:when>
          <c:otherwise>
            nao existe resultado de pesquisa
          </c:otherwise>
        </c:choose>
      </c:forEach>
    </div>


  </body>
</html>
