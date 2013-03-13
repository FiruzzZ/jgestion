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
import javax.persistence.UniqueConstraint;

/**
 *
 * @author FiruzzZ
 */
@Entity
@Table(name = "detalle_remesa")
@NamedQueries({
    @NamedQuery(name = "DetalleRemesa.findAll", query = "SELECT d FROM DetalleRemesa d"),
    @NamedQuery(name = "DetalleRemesa.findById", query = "SELECT d FROM DetalleRemesa d WHERE d.id = :id")
})
public class DetalleRemesa implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Basic(optional = false)
    @Column(name = "monto_entrega", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoEntrega;
    @Column(name = "observacion", length = 200)
    private String observacion;
    @JoinColumn(name = "factura_compra")
    @ManyToOne
    private FacturaCompra facturaCompra;
    @JoinColumn(name = "nota_debito_proveedor_id")
    @ManyToOne
    private NotaDebitoProveedor notaDebitoProveedor;
    @JoinColumn(name = "remesa", nullable = false)
    @ManyToOne(optional = false)
    private Remesa remesa;
    @Basic(optional = false)
    @Column(nullable = false)
    private boolean anulado;

    public DetalleRemesa() {
    }

    public DetalleRemesa(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public BigDecimal getMontoEntrega() {
        return montoEntrega;
    }

    public void setMontoEntrega(BigDecimal montoEntrega) {
        this.montoEntrega = montoEntrega;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public FacturaCompra getFacturaCompra() {
        return facturaCompra;
    }

    public void setFacturaCompra(FacturaCompra facturaCompra) {
        this.facturaCompra = facturaCompra;
    }

    public NotaDebitoProveedor getNotaDebitoProveedor() {
        return notaDebitoProveedor;
    }

    public void setNotaDebitoProveedor(NotaDebitoProveedor notaDebitoProveedor) {
        this.notaDebitoProveedor = notaDebitoProveedor;
    }

    public Remesa getRemesa() {
        return remesa;
    }

    public void setRemesa(Remesa remesa) {
        this.remesa = remesa;
    }

    public boolean getAnulado() {
        return anulado;
    }

    public void setAnulado(boolean anulado) {
        this.anulado = anulado;
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
        if (!(object instanceof DetalleRemesa)) {
            return false;
        }
        DetalleRemesa other = (DetalleRemesa) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "DetalleRemesa{" + "id=" + id + ", montoEntrega=" + montoEntrega + ", observacion=" + observacion + ", facturaCompra=" + facturaCompra + ", anulado=" + anulado + '}';
    }
}
