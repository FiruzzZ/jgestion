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
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

/**
 * Para saber si es una retención de un cliente o hecha por la empresa, ver las
 * tablas intermedias {@link ReciboPagos} y {@link RemesaPagos}.
 *
 * @author FiruzzZ
 */
@Entity
@Table(name = "comprobante_retencion", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"propio", "numero"})})
@NamedQueries({
    @NamedQuery(name = "ComprobanteRetencion.findAll", query = "SELECT c FROM ComprobanteRetencion c")})
public class ComprobanteRetencion implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(nullable = false)
    private Integer id;
    @Basic(optional = false)
    @Column(nullable = false)
    private Long numero;
    @Basic(optional = false)
    @Column(nullable = false)
    @Temporal(TemporalType.DATE)
    private Date fecha;
    @Basic(optional = false)
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal importe;
    /**
     * Si el comprobante es emitido por la propia empresa, o la retención
     * proviene de otro Cliente
     */
    @Column(nullable = false)
    private boolean propio;

    public ComprobanteRetencion() {
    }

    public ComprobanteRetencion(Integer id) {
        this.id = id;
    }

    public ComprobanteRetencion(Integer id, Long numero, Date fecha, BigDecimal importe, boolean propia) {
        this.id = id;
        this.numero = numero;
        this.fecha = fecha;
        this.importe = importe;
        this.propio = propia;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Long getNumero() {
        return numero;
    }

    public void setNumero(Long numero) {
        this.numero = numero;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public BigDecimal getImporte() {
        return importe;
    }

    public void setImporte(BigDecimal importe) {
        this.importe = importe;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    public boolean isPropio() {
        return propio;
    }

    public void setPropio(boolean propio) {
        this.propio = propio;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ComprobanteRetencion)) {
            return false;
        }
        ComprobanteRetencion other = (ComprobanteRetencion) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ComprobanteRetencion{" + "id=" + id + ", numero=" + numero + ", fecha=" + fecha + ", importe=" + importe + ", propia=" + propio + '}';
    }
}
