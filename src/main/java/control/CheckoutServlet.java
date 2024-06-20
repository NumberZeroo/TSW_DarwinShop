package control;

import com.tswproject.tswproj.RuntimeSQLException;
import com.tswproject.tswproj.SessionFacade;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.infoConsegna.InfoConsegnaBean;
import model.infoConsegna.InfoConsegnaDAO;
import model.orderItem.OrderItemBean;
import model.orderItem.OrderItemDAO;
import model.ordine.OrdineBean;
import model.ordine.OrdineDAO;
import model.prodotto.ProdottoBean;
import model.prodotto.ProdottoDAO;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@WebServlet(value="/checkout")
public class CheckoutServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // TODO 1:  Inserisci i dati di "infoConsegna" e "ordine"
        // TODO 2:  Implementa indirizzo di fatturazione
        // TODO 3:  Implementa creazione di fattura
        // TODO 4:  Rimanda a pagina di esito
        SessionFacade session = new SessionFacade(req.getSession());
        if (!session.isLoggedIn()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        Map<Long, Integer> cartProducts = session.getCartProducts();
        if (cartProducts == null || cartProducts.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        String city = req.getParameter("city");
        int cap = Integer.parseInt(req.getParameter("cap"));
        String address = req.getParameter("address");
        String other = req.getParameter("other");
        String receiver = req.getParameter("receiver");

        // 1. Crea un infoConsegna
        InfoConsegnaBean infoConsegnaBean = new InfoConsegnaBean();
        infoConsegnaBean.setCitta(city);
        infoConsegnaBean.setCap(cap);
        infoConsegnaBean.setVia(address);
        infoConsegnaBean.setAltro(other);
        infoConsegnaBean.setDestinatario(receiver);
        infoConsegnaBean.setIdUtente(session.getUserId());

        long idInfoConsegna;
        try(InfoConsegnaDAO infoConsegnaDAO = new InfoConsegnaDAO()) {
            idInfoConsegna = infoConsegnaDAO.doSave(infoConsegnaBean);
        } catch (SQLException e) {
            throw new RuntimeSQLException("Errore durante la creazione delle informazioni di consegna", e);
        }

        // 2. Crea un ordine
        OrdineBean ordine = new OrdineBean();
        ordine.setIdInfoConsegna(idInfoConsegna);
        ordine.setIdUtente(session.getUserId());
        ordine.setPathFattura("/not/yet/implemented");

        long idOrdine;
        try(OrdineDAO ordineDAO = new OrdineDAO()) {
            idOrdine = ordineDAO.doSave(ordine);
        } catch (SQLException e) {
            throw new RuntimeSQLException("Errore durante la creazione dell'ordine", e);
        }

        // 3. Crea gli orderItem
        Map<ProdottoBean, Integer> toBuy;
        try(ProdottoDAO prodottoDAO = new ProdottoDAO()) {
            toBuy = prodottoDAO.doRetrieveByKeys(cartProducts);
        } catch (SQLException e) {
            try(InfoConsegnaDAO infoConsegnaDAO = new InfoConsegnaDAO()) {
                infoConsegnaDAO.doDelete(idInfoConsegna);
            } catch (SQLException ex) {
                throw new RuntimeSQLException("Errore durante la procedura di free", e);
            }
            throw new RuntimeSQLException("Errore durante la determinazione degli articoli", e);
        }

        List<OrderItemBean> itemsToBuy = new LinkedList<>();
        for (ProdottoBean prodottoBean : toBuy.keySet()) {
            OrderItemBean orderItemBean = new OrderItemBean();
            orderItemBean.setPrezzo(prodottoBean.getPrezzo());
            orderItemBean.setQuantita(toBuy.get(prodottoBean));
            orderItemBean.setIdOrdine(idOrdine);
            orderItemBean.setIdProdotto(prodottoBean.getId());
            itemsToBuy.add(orderItemBean);
        }

        try(OrderItemDAO orderItemDAO = new OrderItemDAO()){
            orderItemDAO.doSaveAll(itemsToBuy);
        } catch (SQLException e) {
            try(InfoConsegnaDAO infoConsegnaDAO = new InfoConsegnaDAO()) {
                infoConsegnaDAO.doDelete(idInfoConsegna);
            } catch (SQLException ex) {
                throw new RuntimeSQLException("Errore durante la procedura di free", e);
            }

            try(OrdineDAO ordineDAO = new OrdineDAO()) {
                ordineDAO.doDelete(idOrdine);
            } catch (SQLException ex) {
                throw new RuntimeSQLException("Errore durante la procedura di free", e);
            }
            throw new RuntimeSQLException("Errore nel salvataggio dei prodotti dell'ordine", e);
        }

        // 4. Svuota carrello
        session.removeCartProducts();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }
}
