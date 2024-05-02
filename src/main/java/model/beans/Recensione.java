package model.beans;

import java.io.Serializable;
import java.util.Date;

public class Recensione implements Serializable {
    private long id;
    private long idUtente;
    private String titolo;
    private String commento;
    private long valutazione;
    private Date data;
    private long idProdotto;

    public Recensione() {}

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getIdUtente() {
        return idUtente;
    }

    public void setIdUtente(long idUtente) {
        this.idUtente = idUtente;
    }

    public String getTitolo() {
        return titolo;
    }

    public void setTitolo(String titolo) {
        this.titolo = titolo;
    }

    public String getCommento() {
        return commento;
    }

    public void setCommento(String commento) {
        this.commento = commento;
    }

    public long getValutazione() {
        return valutazione;
    }

    public void setValutazione(long valutazione) {
        this.valutazione = valutazione;
    }

    public Date getData() {
        return data;
    }

    public void setData(Date data) {
        this.data = data;
    }

    public long getIdProdotto() {
        return idProdotto;
    }

    public void setIdProdotto(long idProdotto) {
        this.idProdotto = idProdotto;
    }

    @Override
    public String toString() {
        return "Recensione{" +
                "id=" + id +
                ", idUtente=" + idUtente +
                ", titolo='" + titolo + '\'' +
                ", commento='" + commento + '\'' +
                ", valutazione=" + valutazione +
                ", data=" + data +
                ", idProdotto=" + idProdotto +
                '}';
    }
}