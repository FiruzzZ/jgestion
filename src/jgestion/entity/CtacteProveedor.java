package jgestion.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author Administrador
 */
@Entity
@Table(name = "ctacte_proveedor")
@NamedQueries({
    @NamedQuery(name = "CtacteProveedor.findAll", query = "SELECT c FROM CtacteProveedor c"),
    @NamedQuery(name = "CtacteProveedor.findById", query = "SELECT c FROM CtacteProveedor c WHERE c.id = :id"),
    @NamedQuery(name = "CtacteProveedor.findByFactura", query = "SELECT c FROM CtacteProveedor c WHERE c.factura.id = :idFactura")
})
public class CtacteProveedor implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Basic(optional = false)
    @Column(name = "dias", nullable = false)
    private int dias;
    @Basic(optional = false)
    @Column(name = "importe", nullable = false, precision = 12, scale = 2)
    private BigDecimal importe;
    @Basic(optional = false)
    @Column(name = "estado", nullable = false)
    private short estado;
    @Basic(optional = false)
    @Column(name = "entregado", nullable = false, precision = 12, scale = 2)
    private BigDecimal entregado;
    @Basic(optional = false)
    @Column(name = "fecha_carga", nullable = false, updatable = false, columnDefinition = "timestamp with time zone NOT NULL DEFAULT now()")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCarga;
    @JoinColumn(name = "factura")
    @ManyToOne(optional = false)
    private FacturaCompra factura;
    @JoinColumn(name = "notadebito_id")
    @ManyToOne
    private NotaDebitoProveedor notaDebito;

    public CtacteProveedor() {
    }

    public CtacteProveedor(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getDias() {
        return dias;
    }

    public void setDias(int dias) {
        this.dias = dias;
    }

    public BigDecimal getImporte() {
        return importe;
    }

    public void setImporte(BigDecimal importe) {
        this.importe = importe;
    }

    public short getEstado() {
        return estado;
    }

    public void setEstado(short estado) {
        this.estado = estado;
    }

    public BigDecimal getEntregado() {
        return entregado;
    }

    public void setEntregado(BigDecimal entregado) {
        this.entregado = entregado;
    }

    public Date getFechaCarga() {
        return fechaCarga;
    }

    public void setFechaCarga(Date fechaCarga) {
        this.fechaCarga = fechaCarga;
    }

    public FacturaCompra getFactura() {
        return factura;
    }

    public void setFactura(FacturaCompra factura) {
        this.factura = factura;
    }

    public NotaDebitoProveedor getNotaDebito() {
        return notaDebito;
    }

    public void setNotaDebito(NotaDebitoProveedor notaDebito) {
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
        if (!(object instanceof CtacteProveedor)) {
            return false;
        }
        CtacteProveedor other = (CtacteProveedor) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "CtacteProveedor{" + "id=" + id + ", dias=" + dias + ", importe=" + importe + ", estado=" + estado + ", entregado=" + entregado + ", fechaCarga=" + fechaCarga + ", factura=" + factura + '}';
    }
}
