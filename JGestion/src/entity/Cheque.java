package entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Basic model of a Check.
 * @author FiruzzZ
 */
@MappedSuperclass
@Inheritance(strategy = InheritanceType.JOINED)
@Access(AccessType.FIELD)
public abstract class Cheque implements Serializable {

    private static final long serialVersionUID = 1L;
    @Column(nullable = false, precision = 8)
    protected Long numero;
    @Basic(optional = false)
    @Column(name = "importe", nullable = false, precision = 10, scale = 2)
    protected BigDecimal importe;
    @Basic(optional = false)
    @Column(name = "fecha_cheque", nullable = false)
    @Temporal(TemporalType.DATE)
    protected Date fechaCheque;
    @Basic(optional = false)
    @Column(name = "cruzado", nullable = false)
    protected boolean cruzado;
    @Column(name = "observacion", length = 300)
    protected String observacion;
    @Basic(optional = false)
    @Column(name = "fecha_cobro", nullable = false)
    @Temporal(TemporalType.DATE)
    protected Date fechaCobro;
    @Basic(optional = false)
    @Column(name = "fecha_creacion", nullable = false, insertable = false, updatable = false, columnDefinition = "timestamp with time zone NOT NULL DEFAULT now()")
    @Temporal(TemporalType.TIMESTAMP)
    protected Date fechaCreacion;

    protected Cheque() {
    }

    /**
     * Get the value of numero
     *
     * @return the value of numero
     */
    public Long getNumero() {
        return numero;
    }

    /**
     * Set the value of numero
     *
     * @param numero new value of numero
     */
    public void setNumero(Long numero) {
        this.numero = numero;
    }

    public boolean isCruzado() {
        return cruzado;
    }

    public void setCruzado(boolean cruzado) {
        this.cruzado = cruzado;
    }

    public Date getFechaCheque() {
        return fechaCheque;
    }

    public void setFechaCheque(Date fechaCheque) {
        this.fechaCheque = fechaCheque;
    }

    public Date getFechaCobro() {
        return fechaCobro;
    }

    public void setFechaCobro(Date fechaCobro) {
        this.fechaCobro = fechaCobro;
    }

    public Date getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public BigDecimal getImporte() {
        return importe;
    }

    public void setImporte(BigDecimal importe) {
        this.importe = importe;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Cheque other = (Cheque) obj;
        if (this.numero != other.numero && (this.numero == null || !this.numero.equals(other.numero))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 53 * hash + (this.numero != null ? this.numero.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "Cheque{" + "numero=" + numero + ", importe=" + importe + ", fechaCheque=" + fechaCheque + ", cruzado=" + cruzado + ", observacion=" + observacion + ", fechaCobro=" + fechaCobro + ", fechaCreacion=" + fechaCreacion + '}';
    }
}
