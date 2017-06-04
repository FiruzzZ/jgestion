package jgestion.entity;

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
import javax.persistence.Table;

/**
 *
 * @author FiruzzZ
 */
@Entity
@Table(name = "detalle_nota_debito_proveedor")
public class DetalleNotaDebitoProveedor implements Serializable {

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
    @JoinColumn(name = "iva_id", nullable = false)
    @ManyToOne(optional = false)
    private Iva iva;
    @JoinColumn(name = "nota_debito_proveedor_id", nullable = false)
    @ManyToOne(optional = false)
    private NotaDebitoProveedor notaDebitoProveedor;

    public DetalleNotaDebitoProveedor() {
    }

    public DetalleNotaDebitoProveedor(Integer id, String concepto, BigDecimal importe, Iva iva) {
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

    public NotaDebitoProveedor getNotaDebitoProveedor() {
        return notaDebitoProveedor;
    }

    public void setNotaDebitoProveedor(NotaDebitoProveedor notaDebitoProveedor) {
        this.notaDebitoProveedor = notaDebitoProveedor;
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
        if (!(object instanceof DetalleNotaDebitoProveedor)) {
            return false;
        }
        DetalleNotaDebitoProveedor other = (DetalleNotaDebitoProveedor) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entity.DetalleNotaDebitoProveedor[ id=" + id + " ]";
    }
}
