<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
         pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.3.1/jquery.min.js"></script>
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js"></script>
    <style><%@include file="/css/login.css"%></style>
    <title>Login</title>
</head>
<body>
    <s:url action="fazerRegisto" var="fazerRegistoUrl" />
    <div>
        <img src="ucBusca.jpg" id="logo">
    </div>
    <div id="dados_utilizador">
        <s:form action="fazerLogin" method="POST">
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
            <tr><td> <s:submit value="login" /></td></tr>
        </s:form>
    </div>
    <button id="btn_registar"><a <s:a href="%{fazerRegistoUrl}"><span ></span> Registar</s:a></button>>
    <div id = "loginFB">
        <p><a href="https://www.facebook.com/v2.2/dialog/oauth?client_id=543271353070225&redirect_uri=http://127.0.0.1:8080/loginFacebook&scope=publish_actions">Login Facebook</a></p><br>
    </div>





</body>
</html>
