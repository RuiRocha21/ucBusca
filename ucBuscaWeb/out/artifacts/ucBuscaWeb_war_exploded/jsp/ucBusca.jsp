<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
         pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <title>ucBusca - pagina de utilizador registado</title>
    <style><%@include file="/css/ucBusca.css"%></style>
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
<body width="100%">
    <s:url action="logout" var="logoutUrl" />
    <s:url action="irParaIndexar" var="irParaIndexar" />
    <s:url action="irParaPaginaPesquisa" var="irParaPaginaPesquisaComRegisto" />
    <s:url action="pesquisaHistorico" var="irParaPaginaHistorico" />
    <s:url action="irParaPaginaPesquisaLinks" var="irParaPaginaPesquisaLinks" />
    <s:url action="pesquisaServidores" var="irParaPaginaEstadoSistema" />
    <s:url action="irParaPaginaPromoverUtilizadores" var="irParaPaginaPromoverUtilizadores" />
    <s:url action="ucBusca" var="ucBuscaUrl" />
    <div>
        <img src="ucBusca.jpg" id="logo">
    </div>
    <c:choose>
        <c:when test="${session.ADMIN == true}">
            <div>
                <button id = "btn_ubBuscaAdmin">
                    <a <s:a href="%{ucBuscaUrl}" > Pagina Principal </s:a>
                </button>

                <button id = "btn_indexar">
                    <a <s:a href="%{irParaIndexar}" > Indexar Url </s:a>
                </button>

                <button id = "btn_pesquisaAdmin">
                    <a <s:a href="%{irParaPaginaPesquisaComRegisto}" > Fazer Pesquisas </s:a>
                </button>

                <button id = "btn_consultaLinksAdmin">
                    <a <s:a href="%{irParaPaginaPesquisaLinks}" > Pesquisar Links para uma pagina </s:a>
                </button>

                <button id = "btn_consultaHistoricoAdmin">
                    <a <s:a href="%{irParaPaginaHistorico}" > Pesquisar Historico de pesquisas </s:a>
                </button>

                <button id = "btn_promoverUtilizador">
                    <a <s:a href="%{irParaPaginaPromoverUtilizadores}" > Promover Utilizador a Admnistrador </s:a>
                </button>

                <button id = "btn_estadoSistema">
                    <a <s:a href="%{irParaPaginaEstadoSistema}" > Consultar estado dos Servidores </s:a>
                </button>

                <button id = "btn_logoutAdmin">
                    <a <s:a href="%{logoutUrl}" > Sair </s:a>
                </button>
            </div>
            <div>
                <c:choose>
                    <c:when test="${session.LOGGED_IN == true}">
                        <h2 style="color: dimgrey;text-align: center">Bem vindo ao ucBusca, ${session.UTILIZADOR}.</h2>
                    </c:when>
                </c:choose>
            </div>
            <div id = "notificacoes">
                <c:forEach items="${session.NOTIFICACAO}">
                    <h2 style="color: dimgrey;text-align: center">${session.NOTIFICACAO}</h2>
                </c:forEach>
            </div>
        </c:when>
        <c:otherwise>
            <div>
                <button id = "btn_ubBuscaUtil">
                    <a <s:a href="%{ucBuscaUrl}" > Pagina Principal </s:a>
                </button>
                <button id = "btn_pesquisaUtil">
                    <a <s:a href="%{irParaPaginaPesquisaComRegisto}" > Fazer Pesquisas </s:a>
                </button>

                <button id = "btn_pesquisaLinksUtil">
                    <a <s:a href="%{irParaPaginaPesquisaLinks}" > Pesquisar Links para outras paginas </s:a>
                </button>

                <button id = "btn_pesquisaHistoricoUtil">
                    <a <s:a href="%{irParaPaginaHistorico}" > Pesquisar Historico de pesquisas </s:a>
                </button>

                <button id = "btn_logoutUtil">
                    <a <s:a href="%{logoutUrl}" > Sair </s:a>
                </button>
            </div>

            <div>
                <c:choose>
                    <c:when test="${session.LOGGED_IN == true}">
                        <h2 style="color: dimgrey;text-align: center">Bem vindo ao ucBusca, ${session.UTILIZADOR}.</h2>
                    </c:when>
                </c:choose>
            </div>
            <div id = "notificacoes">
                <c:forEach items="${session.NOTIFICACAO}">
                    <h2 style="color: dimgrey;text-align: center">${session.NOTIFICACAO}</h2>
                </c:forEach>
            </div>

        </c:otherwise>
    </c:choose>

</body>
</html>
