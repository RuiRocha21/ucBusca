
<%--
  Created by IntelliJ IDEA.
  User: ruirocha
  Date: 25/11/2019
  Time: 15:04
  To change this template use File | Settings | File Templates.
--%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
         pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <title>Pesquisar Historico de Utilizador</title>
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">
        <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.3.1/jquery.min.js"></script>
        <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js"></script>
        <style><%@include file="/css/pesquisaHistorico.css"%></style>
        <script type="text/javascript">

            var websocket = null;

            window.onload = function() { // URI = ws://10.16.0.165:8080/WebSocket/ws
                connect('ws://' + window.location.host + '/ucBusca/ws');
            }

            function connect(host) { // connect to the host websocket
                if ('WebSocket' in window)
                    websocket = new WebSocket(host);
                else if ('MozWebSocket' in window)
                    websocket = new MozWebSocket(host);
                else {
                    writeToHistory('Get a real browser which supports WebSocket.');
                    return;
                }

                websocket.onopen    = onOpen; // set the event listeners below
                websocket.onclose   = onClose;
                websocket.onmessage = onMessage;
                websocket.onerror   = onError;
            }

            function onOpen(event) {
                console.log('Connected to ' + window.location.host + '.');
            }

            function onClose(event) {
                console.log('WebSocket closed.');
            }

            function onMessage(message) { // print the received message
                alert(message.data);
            }

            function onError(event) {
                console.log('WebSocket error (' + event.data + ').');
            }

            function writeToHistory(text) {
                var history = document.getElementById('history');
                var line = document.createElement('p');
                line.style.wordWrap = 'break-word';
                line.innerHTML = text;
                history.appendChild(line);
                history.scrollTop = history.scrollHeight;
            }

        </script>
    </head>
    <body>
        <noscript>JavaScript must be enabled for WebSockets to work.</noscript>
        <s:if test="hasActionErrors()">
            <s:actionerror/>
        </s:if>
        <s:if test="hasActionMessages()">
            <s:actionmessage/>
        </s:if>
        <s:url action="ucBusca" var="ucBuscaUrl" />
        <div>
            <img src="ucBusca.jpg" id="logo">
        </div>
        <div>
            <button id = "btn_retroceder">
                <a <s:a href="%{ucBuscaUrl}" > Pagina Principal </s:a>
            </button>
        </div>
        <div id="resultados">
            <c:forEach items="${resultado}" var="res">
                <c:choose>
                    <c:when test="${res.gettermosPesquisa() !=null}">
                        <p>${res.termosPesquisa}</p>
                    </c:when>
                    <c:otherwise>
                        nao existe resultado de pesquisa
                    </c:otherwise>
                </c:choose>
            </c:forEach>
        </div>
    </body>
</html>
