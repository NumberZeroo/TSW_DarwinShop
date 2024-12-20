package control;

import com.tswproject.tswproj.OutOfStockException;
import com.tswproject.tswproj.RuntimeSQLException;
import com.tswproject.tswproj.SessionFacade;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.orderItem.OrderItemBean;
import model.orderItem.OrderItemDAO;
import model.ordine.OrdineBean;
import model.ordine.OrdineDAO;
import model.prodotto.ProdottoBean;
import model.prodotto.ProdottoDAO;

import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@WebServlet(value="/checkout")
public class CheckoutServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
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

        long idInfoConsegna = Long.parseLong(req.getParameter("selected-option"));
        if (idInfoConsegna < 0) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // 2. Crea un ordine
        OrdineBean ordine = new OrdineBean();
        ordine.setIdInfoConsegna(idInfoConsegna);
        ordine.setIdUtente(session.getUserId());

        long idOrdine;
        try(OrdineDAO ordineDAO = new OrdineDAO()) {
            idOrdine = ordineDAO.doSave(ordine);
        } catch (SQLException e) {
            System.out.println("Errore durante la creazione dell'ordine");
            e.printStackTrace();
            req.setAttribute("status", "error");
            return;
        }

        // 3. Crea gli orderItem
        Map<ProdottoBean, Integer> toBuy;
        try(ProdottoDAO prodottoDAO = new ProdottoDAO()) {
            toBuy = prodottoDAO.doRetrieveByKeys(cartProducts);
        } catch (SQLException e) {
            try(OrdineDAO ordineDAO = new OrdineDAO()) {
                ordineDAO.doDelete(idOrdine);
            }catch (SQLException s){
                System.out.println("Errore durante la procedura di free");
                s.printStackTrace();
                req.setAttribute("status", "error");
                return;
            }
            System.out.println("Errore durante la determinazione degli articoli");
            e.printStackTrace();
            req.setAttribute("status", "error");
            return;
        }

        List<OrderItemBean> itemsToBuy = new LinkedList<>();
        for (ProdottoBean prodottoBean : toBuy.keySet()) {
            OrderItemBean orderItemBean = new OrderItemBean();
            orderItemBean.setPrezzo(prodottoBean.getPrezzo());
            orderItemBean.setQuantita(toBuy.get(prodottoBean));
            orderItemBean.setIdOrdine(idOrdine);
            orderItemBean.setIdProdotto(prodottoBean.getId());
            orderItemBean.setIva(Integer.parseInt(prodottoBean.getIva()));
            orderItemBean.setNome(prodottoBean.getNome());
            itemsToBuy.add(orderItemBean);
        }


        try(OrderItemDAO orderItemDAO = new OrderItemDAO()){
            orderItemDAO.doSaveAll(itemsToBuy);
        } catch (SQLException e) {
            try(OrdineDAO ordineDAO = new OrdineDAO()) {
                ordineDAO.doDelete(idOrdine);
            } catch (SQLException ex) {
                System.out.println("Errore durante la procedura di free");
                ex.printStackTrace();
                req.setAttribute("status", "error");
                return;
            }
            System.out.println("Errore nel salvataggio dei prodotti dell'ordine");
            e.printStackTrace();
            req.setAttribute("status", "error");
            return;
        }

        // 4. Decrementa disponibilità

        try(ProdottoDAO prodottoDAO = new ProdottoDAO()){
            prodottoDAO.doUpdateQuantities(toBuy);
        } catch (OutOfStockException | SQLException e) {
            try(OrdineDAO ordineDAO = new OrdineDAO()) {
                ordineDAO.doDelete(idOrdine); // OrderItems eliminati per CASCADE
            } catch (SQLException ex) {
                System.out.println("Errore durante la procedura di free");
                ex.printStackTrace();
                req.setAttribute("status", "error");
                return;
            }
            System.out.println("Errore nel salvataggio dei prodotti dell'ordine");
            e.printStackTrace();
            req.setAttribute("status", "error");
            return;
        }

        // 5. Svuota carrello
        session.removeAllCartProducts();

        // resp.sendRedirect(req.getContextPath() + "/greetings.jsp");
        req.setAttribute("status", "success");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }
}
