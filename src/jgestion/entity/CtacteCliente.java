package jgestion.entity;

import jgestion.controller.Valores;
import jgestion.controller.Valores.CtaCteEstado;
import jgestion.controller.Valores.FormaPago;
import java.io.Serializable;
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
import javax.persistence.PostPersist;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Representa una relación contable OneToOne entre {@link Cliente} y
 * {@link FacturaVenta} (resultado de una venta a {@link FormaPago#CTA_CTE}) o
 * {@link NotaDebito}
 *
 * @author FiruzzZ
 */
@Entity
@Table(name = "ctacte_cliente")
@NamedQueries({
    @NamedQuery(name = "CtacteCliente.findAll", query = "SELECT c FROM CtacteCliente c"),
    @NamedQuery(name = "CtacteCliente.findById", query = "SELECT c FROM CtacteCliente c WHERE c.id = :id")
})
public class CtacteCliente implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Basic(optional = false)
    @Column(name = "dias", nullable = false)
    private short dias;
    @Basic(optional = false)
    @Column(name = "importe", nullable = false, precision = 12, scale = 2)
    private double importe;
    @Basic(optional = false)
    @Column(name = "estado", nullable = false)
    private short estado;
    @Basic(optional = false)
    @Column(name = "entregado", nullable = false, precision = 12, scale = 2)
    private double entregado;
    @Basic(optional = false)
    @Column(name = "fecha_carga", nullable = false, updatable = false, columnDefinition = "timestamp with time zone NOT NULL DEFAULT now()")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCarga;
    @JoinColumn(name = "factura")
    @ManyToOne
    private FacturaVenta factura;
    @JoinColumn(name = "notadebito_id")
    @ManyToOne
    private NotaDebito notaDebito;

    public CtacteCliente() {
    }

    public CtacteCliente(Integer id) {
        this.id = id;
    }

    @PostPersist
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public short getDias() {
        return dias;
    }

    public void setDias(short dias) {
        this.dias = dias;
    }

    public double getImporte() {
        return importe;
    }

    public void setImporte(double importe) {
        this.importe = importe;
    }

    public short getEstado() {
        return estado;
    }

    /**
     * 1 = pendiente, 2 = pagada, 3 = anulada
     *
     * @param estado
     */
    public void setEstado(short estado) {
        this.estado = estado;
    }

    public double getEntregado() {
        return entregado;
    }

    public void setEntregado(double entregado) {
        this.entregado = entregado;
    }

    public Date getFechaCarga() {
        return fechaCarga;
    }

    public void setFechaCarga(Date fechaCarga) {
        this.fechaCarga = fechaCarga;
    }

    public FacturaVenta getFactura() {
        return factura;
    }

    public void setFactura(FacturaVenta factura) {
        this.factura = factura;
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
        if (!(object instanceof CtacteCliente)) {
            return false;
        }
        CtacteCliente other = (CtacteCliente) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "CtacteCliente{" + "id=" + id + ", dias=" + dias + ", importe=" + importe + ", estado=" + estado + ", entregado=" + entregado + ", fechaCarga=" + fechaCarga + ", factura=" + factura + ", notaDebito=" + notaDebito + '}';
    }

    public CtaCteEstado getEstadoEnum() {
        return Valores.CtaCteEstado.getCtaCteEstado(this.estado);
    }
}
