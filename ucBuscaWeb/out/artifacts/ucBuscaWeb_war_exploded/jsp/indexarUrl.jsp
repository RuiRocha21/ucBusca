<%--
  Created by IntelliJ IDEA.
  User: ruirocha
  Date: 25/11/2019
  Time: 15:05
  To change this template use File | Settings | File Templates.
--%>
<%@taglib uri="/struts-tags" prefix="s" %>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
         pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>
    <title>Indexar URL</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.3.1/jquery.min.js"></script>
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js"></script>
    <style><%@include file="/css/indexarUrl.css"%></style>

</head>
<body>
    <s:url action="ucBusca" var="ucBuscaUrl" />
    <div>
        <img src="ucBusca.jpg" id="logo">
    </div>
    <div>
        <button id = "btn_retroceder">
            <a <s:a href="%{ucBuscaUrl}" > Voltar a Pagina Principal </s:a>
        </button>
    </div>
    <div id = "dados_indexar">
        <s:form action="indexarUrl" method="post">
            <tr>
                <td>Link a indexar: </td>
                <td>
                    <s:textfield name="url" label="url" />
                </td>
            </tr>
            <s:submit type="button" label="Indexar Url" />
        </s:form>

    </div>
</body>
</html>
