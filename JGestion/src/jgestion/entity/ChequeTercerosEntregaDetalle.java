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
 *
 * @author FiruzzZ
 */
@Entity
@Table(name = "cheque_terceros_entrega_detalle", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"cheque_terceros_entrega_id", "cheque_terceros_id"})
})
public class ChequeTercerosEntregaDetalle implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(nullable = false)
    private Integer id;
    @JoinColumn(name = "cheque_terceros_id", nullable = false)
    @ManyToOne(optional = false)
    private ChequeTerceros chequeTerceros;
    @JoinColumn(name = "cheque_terceros_entrega_id", nullable = false)
    @ManyToOne(optional = false)
    private ChequeTercerosEntrega chequeTercerosEntrega;

    public ChequeTercerosEntregaDetalle() {
    }

    public ChequeTercerosEntregaDetalle(Integer id) {
        this.id = id;
    }

    public ChequeTercerosEntregaDetalle(ChequeTerceros chequeTerceros, ChequeTercerosEntrega chequeTercerosEntrega) {
        this.chequeTerceros = chequeTerceros;
        this.chequeTercerosEntrega = chequeTercerosEntrega;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public ChequeTerceros getChequeTerceros() {
        return chequeTerceros;
    }

    public void setChequeTerceros(ChequeTerceros chequeTerceros) {
        this.chequeTerceros = chequeTerceros;
    }

    public ChequeTercerosEntrega getChequeTercerosEntrega() {
        return chequeTercerosEntrega;
    }

    public void setChequeTercerosEntrega(ChequeTercerosEntrega chequeTercerosEntrega) {
        this.chequeTercerosEntrega = chequeTercerosEntrega;
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
        if (!(object instanceof ChequeTercerosEntregaDetalle)) {
            return false;
        }
        ChequeTercerosEntregaDetalle other = (ChequeTercerosEntregaDetalle) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entity.ChequeTercerosEntregaDetalle[ id=" + id + " ]";
    }
}
