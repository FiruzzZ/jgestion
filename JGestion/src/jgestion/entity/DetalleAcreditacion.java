/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jgestion.entity;

import java.io.Serializable;
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
 * Administra la utilización de una {@link NotaCredito} como forma de pago de un
 * {@link Recibo}, permite sabes que {@link DetalleRecibo} fue acreditado y de
 * que {@link NotaCredito} se desacreditó dicho monto.
 * @author FiruzzZ
 */
@Entity
@Table(name = "detalle_acreditacion", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"nota_credito", "detalle_recibo"})})
@NamedQueries({
    @NamedQuery(name = "DetalleAcreditacion.findAll", query = "SELECT d FROM DetalleAcreditacion d"),
    @NamedQuery(name = "DetalleAcreditacion.findById", query = "SELECT d FROM DetalleAcreditacion d WHERE d.id = :id")
})
public class DetalleAcreditacion implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Basic(optional = false)
    @Column(name = "monto", nullable = false)
    private double monto;
    @JoinColumn(name = "nota_credito", referencedColumnName = "id", nullable = false)
    @ManyToOne(optional = false)
    private NotaCredito notaCredito;
    @JoinColumn(name = "detalle_recibo", referencedColumnName = "id", nullable = false)
    @ManyToOne(optional = false)
    private DetalleRecibo detalleRecibo;
    @Column(name = "anulado", nullable = false)
    private boolean anulado;

    public DetalleAcreditacion() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public double getMonto() {
        return monto;
    }

    public void setMonto(double monto) {
        this.monto = monto;
    }

    public DetalleRecibo getDetalleRecibo() {
        return detalleRecibo;
    }

    public void setDetalleRecibo(DetalleRecibo detalleRecibo) {
        this.detalleRecibo = detalleRecibo;
    }

    public NotaCredito getNotaCredito() {
        return notaCredito;
    }

    public void setNotaCredito(NotaCredito notaCredito) {
        this.notaCredito = notaCredito;
    }

    public boolean isAnulado() {
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
        if (!(object instanceof DetalleAcreditacion)) {
            return false;
        }
        DetalleAcreditacion other = (DetalleAcreditacion) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entity.DetalleAcreditacion[ id=" + id + " ]";
    }
}
