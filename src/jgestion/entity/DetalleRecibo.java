package jgestion.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import javax.persistence.*;

/**
 *
 * @author Administrador
 */
@Entity
@Table(name = "detalle_recibo")
@NamedQueries({
    @NamedQuery(name = "DetalleRecibo.findAll", query = "SELECT d FROM DetalleRecibo d"),
    @NamedQuery(name = "DetalleRecibo.findById", query = "SELECT d FROM DetalleRecibo d WHERE d.id = :id"),
    @NamedQuery(name = "DetalleRecibo.findByFacturaVenta", query = "SELECT d FROM DetalleRecibo d WHERE d.facturaVenta = :facturaVenta")
})
public class DetalleRecibo implements Serializable {

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
    @JoinColumn(name = "factura_venta")
    @ManyToOne
    private FacturaVenta facturaVenta;
    @JoinColumn(name = "nota_debito_id")
    @ManyToOne
    private NotaDebito notaDebito;
    @JoinColumn(name = "recibo", nullable = false)
    @ManyToOne(optional = false)
    private Recibo recibo;
    @Column(nullable = false)
    private boolean anulado;
    @Column(name = "acreditado", insertable = true, updatable = false, nullable = false)
    @Deprecated
    private boolean acreditado;

    public DetalleRecibo() {
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

    public FacturaVenta getFacturaVenta() {
        return facturaVenta;
    }

    public void setFacturaVenta(FacturaVenta facturaVenta) {
        this.facturaVenta = facturaVenta;
    }

    public NotaDebito getNotaDebito() {
        return notaDebito;
    }

    public void setNotaDebito(NotaDebito notaDebito) {
        this.notaDebito = notaDebito;
    }

    public Recibo getRecibo() {
        return recibo;
    }

    public void setRecibo(Recibo recibo) {
        this.recibo = recibo;
    }

    public boolean isAnulado() {
        return anulado;
    }

    public void setAnulado(boolean anulado) {
        this.anulado = anulado;
    }

    @Deprecated
    public void setAcreditado(boolean acreditado) {
        this.acreditado = acreditado;
    }

    @Deprecated
    public boolean isAcreditado() {
        return acreditado;
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
        if (!(object instanceof DetalleRecibo)) {
            return false;
        }
        DetalleRecibo other = (DetalleRecibo) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entity.DetalleRecibo[id=" + id + ", monto=" + montoEntrega + ", acreditado=" + acreditado + "]";
    }
}
