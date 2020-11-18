<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
         pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.3.1/jquery.min.js"></script>
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js"></script>
    <style><%@include file="/css/resgistar.css"%></style>
    <title>registar </title>
</head>
<body>
    <s:url action="index" var="retroceder"/>
    <div>
        <img src="ucBusca.jpg" id="logo">
    </div>
    <div id="dados_utilizador">
        <s:form action="fazerRegisto" method="POST">
            <tr>
                <td>Nickname: </td>
                <td>
                    <s:textfield name="utilizador" label="utilizador" />
                </td>
            </tr>
            <tr>
                <td>Password: </td>
                <td>
                    <s:password name="password" label="password" />
                </td>
            </tr>
            <tr><td> <s:submit value="registar" /></td></tr>
        </s:form>
    </div>
    <div id="btn_retroceder">
        <button>
            <a <s:a href="%{retroceder}" > voltar a pagina anteriror </s:a>
        </button>
    </div>
</body>
</html>
