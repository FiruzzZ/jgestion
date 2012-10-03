package entity;

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
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author FiruzzZ
 */
@Entity
@Table(name = "recibo_pagos")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "ReciboPagos.findAll", query = "SELECT r FROM ReciboPagos r"),
    @NamedQuery(name = "ReciboPagos.findById", query = "SELECT r FROM ReciboPagos r WHERE r.id = :id")
})
public class ReciboPagos implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;
    @Basic(optional = false)
    @Column(name = "forma_pago", nullable = false)
    private int formaPago;
    @Basic(optional = false)
    @Column(name = "comprobante_id", nullable = false)
    private int comprobanteId;
    @JoinColumn(nullable = false)
    @ManyToOne(optional = false)
    private Recibo recibo;

    public ReciboPagos() {
    }

    public ReciboPagos(Integer id) {
        this.id = id;
    }

    public ReciboPagos(Integer id, int formaPago, int comprobanteId, Recibo recibo) {
        this.id = id;
        this.formaPago = formaPago;
        this.comprobanteId = comprobanteId;
        this.recibo = recibo;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getFormaPago() {
        return formaPago;
    }

    public void setFormaPago(int formaPago) {
        this.formaPago = formaPago;
    }

    public int getComprobanteId() {
        return comprobanteId;
    }

    public void setComprobanteId(int comprobanteId) {
        this.comprobanteId = comprobanteId;
    }

    public Recibo getRecibo() {
        return recibo;
    }

    public void setRecibo(Recibo recibo) {
        this.recibo = recibo;
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
        if (!(object instanceof ReciboPagos)) {
            return false;
        }
        ReciboPagos other = (ReciboPagos) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entity.ReciboPagos[ id=" + id + " ]";
    }
}
