<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="model.utente.*" %>
<%@ page import="java.util.List" %>
<%@ page import="java.sql.SQLException" %>
<%@ page import="com.tswproject.tswproj.RuntimeSQLException" %>
<%@ page import="model.prodotto.*" %>
<html>
<head>
    <title>Profilo Utente</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/style/adminPage.css">
</head>
<body>
<%@ include file="navbar.jsp" %>

<div class="content profile-container">
    <% SessionFacade sessionFacade = new SessionFacade(request.getSession()); %>
    <div class="menu-row">
        <button id="infoButton">Elenco utenti</button>
        <%--        <button id="ordersButton">Disponibiltà</button>--%>
        <button id="availabilityButton">Disponibilità</button>

        <form action="logout" method="post">
            <input type="submit" value="Logout" id="logoutButton">
        </form>
    </div>

    <div class="profile-content">
        <div class="profile-column">
            <div id="infoSection" style="display: none;">
                <div class="utenti">
                    <div class="utente-header">
                        <h1>Elenco utenti</h1>
                    </div>
                    <% try (UtenteDAO utenteDAO = new UtenteDAO()) { %>
                    <% List<UtenteBean> utenti = (List<UtenteBean>) utenteDAO.doRetrieveAll("ASC"); %>
                    <% if (!utenti.isEmpty()) { %>
                    <table>
                        <thead>
                        <tr>
                            <th></th> <!-- Colonna per l'immagine -->
                            <th>Username</th>
                            <th>Email</th>
                            <th>Admin</th>
                            <th>Storico</th>
                        </tr>
                        </thead>
                        <tbody>
                        <% for (UtenteBean utente : utenti) {
                            if (utente.getId() != sessionFacade.getUserId()) {%>
                        <tr>
                            <td><img class="utente-image" src="<%= utente.getImgPath() %>"
                                     alt="<%= utente.getUsername() %>">
                            </td> <!-- Immagine dell'utente -->
                            <td><%= utente.getUsername() %>
                            </td>
                            <td><%= utente.getEmail() %>
                            </td>
                            <td>
                                <!-- button modifica admin -->
                                <form action="changeUserRoleServlet" method="post">
                                    <input type="hidden" name="userId" value="<%= utente.getId() %>">
                                    <input type="hidden" name="makeAdmin" value="<%= !utente.getIsAdmin() %>">
                                    <input type="submit" class="changeAdminButton"
                                           value="<%= utente.getIsAdmin() ? "Rimuovi admin" : "Rendi admin" %>"
                                           onclick="confirmChangeAdmin(event, '<%= utente.getId() %>', '<%= !utente.getIsAdmin() %>')">
                                </form>
                            </td>
                            <td>
                                <!-- Form che porta alla pagina di storico ordini dell'utente cliccato -->
                                <form action="orderHistory" method="post">
                                    <input type="hidden" name="userId" value="<%= utente.getId() %>">
                                    <input type="submit" class="orderHistoryButton" value="Storico ordini">
                                </form>
                            </td>
                        </tr>
                        <% }
                        } %>
                        </tbody>
                    </table>
                    <% }
                    } catch (SQLException e) {
                        throw new RuntimeSQLException("Errore durante il recupero delle informazioni degli utenti", e);
                    } %>
                </div>
            </div>
            <div id="availabilitySection" class="dispSection" style="display: none;">
                <h1>Disponibilità</h1>
                <% try (ProdottoDAO prodottoDAO = new ProdottoDAO()) { %>
                <% List<ProdottoBean> prodotti = (List<ProdottoBean>) prodottoDAO.doRetrieveAll("ASC"); %>
                <% if (!prodotti.isEmpty()) { %>
                <% for (ProdottoBean prodotto : prodotti) { %>
                <div class="item-box">
                    <div class="item-image">
                        <img src="<%= prodotto.getImgPath() %>" alt="<%= prodotto.getNome() %>">
                    </div>
                    <div class="item-details">
                        <h2 class="item-name"><%= prodotto.getNome() %>
                        </h2>
                        <p class="item-price"><%= prodotto.getPrezzo() %>
                        </p>
                        <p class="item-quantity">Disponibilità: <%= prodotto.getDisponibilita() %>
                    </div>
                    <button class="refillButton <%= prodotto.getDisponibilita() < 5 ? "lowStock" : "" %>"
                            data-id="<%= prodotto.getId() %>" onclick="showForm('<%= prodotto.getId() %>')">Refill
                    </button>
                    <form id="refillForm<%= prodotto.getId() %>" class="refillForm" action="refillProductServlet" method="post"
                          style="display: none;">
                        <input type="hidden" name="productId" value="<%= prodotto.getId() %>">
                        <input type="number" name="quantity" min="1" placeholder="Quantità">
                        <input type="submit" value="Conferma" class="refillFormButton">
                    </form>
                </div>
                <% } %>
                <% } %>
                <% } catch (SQLException e) {
                    throw new RuntimeSQLException("Errore durante il recupero delle informazioni dei prodotti", e);
                } %>
            </div>
        </div>
    </div>
</div>

<%@ include file="footer.jsp" %>

<script>
    function showForm(productId) {
        var form = document.getElementById('refillForm' + productId);
        var button = document.querySelector('.refillButton[data-id="' + productId + '"]');
        if (form.style.display === 'none') {
            form.style.display = 'block';
            button.style.display = 'none';
        } else {
            form.style.display = 'none';
            button.style.display = 'block';
        }
    }
</script>

<script>
    // Seleziona i pulsanti e le sezioni
    let infoButton = document.getElementById('infoButton');
    // let ordersButton = document.getElementById('ordersButton');

    let infoSection = document.getElementById('infoSection');
    // let ordersSection = document.getElementById('ordersSection');

    let availabilityButton = document.getElementById('availabilityButton');
    let availabilitySection = document.getElementById('availabilitySection');

    availabilityButton.addEventListener('click', function () {
        showSection('availabilitySection');
    });

    // Funzione per nascondere tutte le sezioni
    function hideAllSections() {
        infoSection.style.display = 'none';
        availabilitySection.style.display = 'none';
        // ordersSection.style.display = 'none';
    }

    // Funzione per mostrare una sezione specifica
    function showSection(sectionId) {
        hideAllSections();
        let section = document.getElementById(sectionId);
        section.style.display = 'block';
    }

    // Aggiungi gestori di eventi di click ai pulsanti
    infoButton.addEventListener('click', function () {
        showSection('infoSection');
    });

    // ordersButton.addEventListener('click', function () {
    //     showSection('ordersSection');
    // });

    showSection('infoSection');
</script>

<script src="https://unpkg.com/sweetalert/dist/sweetalert.min.js"></script>
<script>
    function confirmChangeAdmin(event, userId, makeAdmin) {
        event.preventDefault();  // Previene l'invio del form
        var form = event.target.form;  // Ottiene il form

        swal({
            title: "Sei sicuro?",
            text: "Vuoi davvero cambiare il ruolo di questo utente?",
            icon: "warning",
            buttons: true,
            dangerMode: true,
        })
            .then((willChange) => {
                if (willChange) {
                    form.submit();  // Invia il form
                }
            });
    }
</script>

</body>
</html>