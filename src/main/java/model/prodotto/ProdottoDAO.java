package model.prodotto;

import com.tswproject.tswproj.EmptyPoolException;
import com.tswproject.tswproj.OutOfStockException;
import model.AbstractDAO;
import model.DAOInterface;
import model.pet.*;

import java.sql.*;
import java.util.*;

public class ProdottoDAO extends AbstractDAO implements DAOInterface<ProdottoBean, Long> {
    public ProdottoDAO() throws EmptyPoolException {
        super();
    }

    @Override
    public ProdottoBean doRetrieveByKey(long id) throws SQLException {
        String query = "SELECT * FROM Prodotto WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return getProdotto(resultSet);
                }
            }
        }
        return null;
    }

    // Prende come parametri una mappa <id_prodotto, quantità> e ritorna una mappa <ProdottoBean, quantità>
    // Non posso fare un prepared statement contenente tutti gli id perché le query non vengono fatte in modo ordinato
    // TODO: Questo metodo fa tante query...c'è un modo migliore?
    public Map<ProdottoBean, Integer> doRetrieveByKeys(Map<Long, Integer> productsWithQuantity) throws SQLException {
        Map<ProdottoBean, Integer> prodottoBeans = new HashMap<>();
        for (Map.Entry<Long, Integer> entry : productsWithQuantity.entrySet()) {
            ProdottoBean p = doRetrieveByKey(entry.getKey());
            if (p != null) {
                prodottoBeans.put(p, entry.getValue());
            }
        }
        return prodottoBeans;
    }


   public List<ProdottoBean> doRetrieveByName(String name) throws SQLException {
        List<ProdottoBean> prodottoBeans = new ArrayList<>();
        String query = "SELECT * FROM Prodotto WHERE LOWER(Nome) LIKE LOWER(?) LIMIT 5";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, "%" + name + "%");
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    prodottoBeans.add(getProdotto(resultSet));
                }
            }
        }
        return prodottoBeans;
    }

    public Collection<ProdottoBean> doRetrieveFiltered(String price, String size, String category, String animalRace, String sterilized, String petId) throws SQLException {

      List<ProdottoBean> prodotti = new ArrayList<>();
      StringBuilder query = new StringBuilder("SELECT * FROM Prodotto WHERE 1=1");

      List<String> parameters = new ArrayList<>();

      if (petId != null && !petId.equals("all") ) {
          // Get the pet characteristics
          try(PetDAO petDAO = new PetDAO()){
              PetBean pet = petDAO.doRetrieveByKey(Long.parseLong(petId));
              if (pet != null) {
                  size = pet.getTaglia();
                  animalRace = pet.getTipo();
                  sterilized = pet.getSterilizzato() ? "1" : "0";
              }else{
                  throw new RuntimeException("Errore retrieve pet by key");
              }
          }catch (Exception e) {
              e.printStackTrace();
          }
      }

      if (!"all".equals(price)) {
          query.append(" AND Prezzo <= ?");
          parameters.add(price);
      }

      if (!"all".equals(size)) {
          query.append(" AND Taglia = ?");
          parameters.add(size);
      }

      if (!"all".equals(category)) {
          query.append(" AND Categoria = ?");
          parameters.add(category);
      }

      if (!"all".equals(animalRace)) {
          query.append(" AND TipoAnimale = ?");
          parameters.add(animalRace);
      }

      if (!"all".equals(sterilized)) {
          query.append(" AND Sterilizzati = ?");
          parameters.add(sterilized);
      }

      try (PreparedStatement statement = connection.prepareStatement(query.toString())) {
          for (int i = 0; i < parameters.size(); i++) {
              statement.setString(i + 1, parameters.get(i));
          }

          try (ResultSet resultSet = statement.executeQuery()) {
              while (resultSet.next()) {
                  ProdottoBean prodotto = getProdotto(resultSet);
                  prodotti.add(prodotto);
              }
          }
      }
      return prodotti;
  }

    @Override
    public Collection<ProdottoBean> doRetrieveAll(String order) throws SQLException {
        List<ProdottoBean> prodotti = new ArrayList<>();
        String query = "SELECT * FROM Prodotto";
        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                ProdottoBean prodotto = getProdotto(resultSet);
                prodotti.add(prodotto);
            }
        }
        return prodotti;
    }

    public Collection<ProdottoBean> doRetrieveAllByCategory(String category) throws SQLException {
        List<ProdottoBean> prodotti = new ArrayList<>();
        String query = "SELECT * FROM Prodotto WHERE Categoria = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, category);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    ProdottoBean prodotto = getProdotto(resultSet);
                    prodotti.add(prodotto);
                }
            }
        }

        return prodotti;
    }

    @Override
    public long doSave(ProdottoBean prodotto) throws SQLException {
        String query = "INSERT INTO Prodotto (Nome, Disponibilità, Taglia, Categoria, MinEta, MaxEta, IVA, Prezzo, Sterilizzati, imgPath, descrizione, TipoAnimale, visibile) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        long generatedKey = -1;
        try (PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, prodotto.getNome());
            statement.setInt(2, prodotto.getDisponibilita());
            statement.setString(3, prodotto.getTaglia());
            statement.setString(4, prodotto.getCategoria());
            statement.setInt(5, prodotto.getMinEta());
            statement.setInt(6, prodotto.getMaxEta());
            statement.setString(7, prodotto.getIva());
            statement.setDouble(8, prodotto.getPrezzo());
            statement.setBoolean(9, prodotto.getSterilizzati());
            statement.setString(10, prodotto.getImgPath());
            statement.setString(11, prodotto.getDescrizione());
            statement.setString(12, String.valueOf(prodotto.getTipoAnimale()));
            statement.setInt(13, prodotto.isVisibile() ? 1 : 0);
            if (statement.executeUpdate() > 0){
                ResultSet rs = statement.getGeneratedKeys();
                if (rs.next()){
                    generatedKey = rs.getLong(1);
                }
            }
        }
        return generatedKey;
    }

    @Override
    public void doUpdate(ProdottoBean prodotto) throws SQLException {

        String query = "UPDATE Prodotto SET Nome = ?, Disponibilità = ?, Taglia = ?, Categoria = ?, MinEta = ?, MaxEta = ?, IVA = ?, Prezzo = ?, Sterilizzati = ?, imgPath = ?, descrizione = ?, visibile = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, prodotto.getNome());
            statement.setInt(2, prodotto.getDisponibilita());
            statement.setString(3, prodotto.getTaglia());
            statement.setString(4, prodotto.getCategoria());
            statement.setInt(5, prodotto.getMinEta());
            statement.setInt(6, prodotto.getMaxEta());
            statement.setString(7, String.valueOf(prodotto.getIva()));
            statement.setDouble(8, prodotto.getPrezzo());
            statement.setBoolean(9, prodotto.getSterilizzati());
            statement.setString(10, prodotto.getImgPath());
            statement.setString(11, prodotto.getDescrizione());
            statement.setInt(12, prodotto.isVisibile() ? 1 : 0);
            statement.setLong(13, prodotto.getId());
            statement.executeUpdate();
        }
    }

    /**
     * Questo metodo controlla per ogni prodotto nella mappa se c'è disponibilità (in termini di quantità) ed eventualmente fa update
     * Usa una transaction per migliorare le prestazioni.
     * Ritorna l'esito
     * @param toUpdate mappa con chiave = id prodotto e valore = quantitò da sottrarre
     * @return isUpdated
     * @throws SQLException
     */
    public void doUpdateQuantities(Map<ProdottoBean, Integer> toUpdate) throws SQLException, OutOfStockException {
        connection.setAutoCommit(false);
        for (Map.Entry<ProdottoBean, Integer> entry : toUpdate.entrySet()) {
            ProdottoBean prodotto = doRetrieveByKey(entry.getKey().getId());
            if (prodotto == null || prodotto.getDisponibilita() < entry.getValue()) {
                connection.rollback();
                connection.setAutoCommit(true);
                throw new OutOfStockException("Disponibilità per il prodotto con id " + entry.getKey().getId() + " terminata");
            }
            prodotto.setDisponibilita(prodotto.getDisponibilita() - entry.getValue());
            doUpdate(prodotto);
        }
        connection.commit();
        connection.setAutoCommit(true);
    }

    @Override
    public boolean doDelete(Long id) throws SQLException {
        String query = "DELETE FROM Prodotto WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, id);
            int rowsDeleted = statement.executeUpdate();
            return rowsDeleted > 0;
        }
    }

    private ProdottoBean getProdotto(ResultSet resultSet) throws SQLException {
        ProdottoBean prodotto = new ProdottoBean();
        prodotto.setId(resultSet.getLong("id"));
        prodotto.setNome(resultSet.getString("Nome"));
        prodotto.setDisponibilita(resultSet.getInt("Disponibilità"));
        prodotto.setTaglia(resultSet.getString("Taglia"));
        prodotto.setCategoria(resultSet.getString("Categoria"));
        prodotto.setMinEta(resultSet.getInt("MinEta"));
        prodotto.setMaxEta(resultSet.getInt("MaxEta"));
        prodotto.setIva(resultSet.getString("IVA"));
        prodotto.setPrezzo(resultSet.getLong("Prezzo"));
        prodotto.setSterilizzati(resultSet.getBoolean("Sterilizzati"));
        prodotto.setImgPath(resultSet.getString("imgPath"));
        prodotto.setTipoAnimale(resultSet.getInt("TipoAnimale"));
        prodotto.setDescrizione(resultSet.getString("descrizione"));
        prodotto.setVisibile(resultSet.getInt("visibile") == 1);
        return prodotto;
    }

    // Metodo per la ricerca di prodotti tramite query, todo: da implementare ajax
    public List<ProdottoBean> doRetrieveByQuery(String query) throws SQLException {
        List<ProdottoBean> prodotti = new ArrayList<>();
        String sql = "SELECT * FROM Prodotto WHERE Nome LIKE ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, "%" + query + "%");

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    ProdottoBean prodotto = getProdotto(resultSet);
                    prodotti.add(prodotto);
                }
            }
        }
        return prodotti;
    }

    public void changeVisibility(long id, boolean isVisible) throws SQLException {
        String query = "UPDATE Prodotto SET visibile = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, isVisible ? 1 : 0);
            statement.setLong(2, id);
            statement.executeUpdate();
        }
    }
}