package entity;

import java.io.Serializable;
import java.math.BigDecimal;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 *
 * @author FiruzzZ
 */
@Entity
@Table(name = "detalle_nota_debito")
@NamedQueries({
    @NamedQuery(name = "DetalleNotaDebito.findAll", query = "SELECT d FROM DetalleNotaDebito d")})
public class DetalleNotaDebito implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(nullable = false)
    private Integer id;
    @Basic(optional = false)
    @Column(nullable = false, length = 200)
    private String concepto;
    @Basic(optional = false)
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal importe;
    /**
     * Si la nota de débito es tipo "B", no se discriminan IVA's por lo tanto va
     * ser
     * <code>null</code>
     */
    @JoinColumn(name = "iva_id")
    @ManyToOne
    private Iva iva;
    @JoinColumn(name = "nota_debito_id", nullable = false)
    @ManyToOne(optional = false)
    private NotaDebito notaDebito;

    public DetalleNotaDebito() {
    }

    public DetalleNotaDebito(Integer id) {
        this.id = id;
    }

    public DetalleNotaDebito(Integer id, String concepto, BigDecimal importe, Iva iva) {
        this.id = id;
        this.concepto = concepto;
        this.importe = importe;
        this.iva = iva;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getConcepto() {
        return concepto;
    }

    public void setConcepto(String concepto) {
        this.concepto = concepto;
    }

    public BigDecimal getImporte() {
        return importe;
    }

    public void setImporte(BigDecimal importe) {
        this.importe = importe;
    }

    public Iva getIva() {
        return iva;
    }

    public void setIva(Iva iva) {
        this.iva = iva;
    }

    public NotaDebito getNotaDebito() {
        return notaDebito;
    }

    public void setNotaDebito(NotaDebito notaDebito) {
        this.notaDebito = notaDebito;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof DetalleNotaDebito)) {
            return false;
        }
        DetalleNotaDebito other = (DetalleNotaDebito) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "DetalleNotaDebito{" + "id=" + id + ", concepto=" + concepto + ", importe=" + importe + ", iva=" + iva + '}';
    }
}
