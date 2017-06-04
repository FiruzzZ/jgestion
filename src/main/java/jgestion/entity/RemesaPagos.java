package jgestion.entity;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 0=Efectivo ({@link DetalleCajaMovimientos}), 1=Cheque Propio, 2=Cheque
 * Tercero, 3=Nota de Crédito
 * {@link NotaCreditoProveedor}, 4= {@link ComprobanteRetencion}
 *
 * @author FiruzzZ
 */
@Entity
@Table(name = "remesa_pagos")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "RemesaPagos.findAll", query = "SELECT r FROM RemesaPagos r")})
public class RemesaPagos implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(nullable = false)
    private Integer id;
    @Basic(optional = false)
    @Column(name = "comprobante_id", nullable = false)
    private int comprobanteId;
    /**
     * 0=Efectivo ({@link DetalleCajaMovimientos}), 1=Cheque Propio, 2=Cheque
     * Tercero, 3=Nota de Crédito
     * {@link NotaCreditoProveedor}, 4= {@link ComprobanteRetencion}, 5= {@link CuentabancariaMovimientos}
     */
    @Basic(optional = false)
    @Column(name = "forma_pago", nullable = false)
    private int formaPago;
    @JoinColumn(nullable = false)
    @ManyToOne(optional = false)
    private Remesa remesa;

    public RemesaPagos() {
    }

    public RemesaPagos(Integer id) {
        this.id = id;
    }

    public RemesaPagos(Integer id, int formaPago, int comprobanteId, Remesa remesa) {
        this.id = id;
        this.comprobanteId = comprobanteId;
        this.formaPago = formaPago;
        this.remesa = remesa;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getComprobanteId() {
        return comprobanteId;
    }

    public void setComprobanteId(int comprobanteId) {
        this.comprobanteId = comprobanteId;
    }

    /**
     *
     * @return value of formaPago
     * @see #formaPago
     */
    public int getFormaPago() {
        return formaPago;
    }

    /**
     *
     * @param formaPago 
     * @see #formaPago
     */
    public void setFormaPago(int formaPago) {
        this.formaPago = formaPago;
    }

    public Remesa getRemesa() {
        return remesa;
    }

    public void setRemesa(Remesa remesa) {
        this.remesa = remesa;
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
        if (!(object instanceof RemesaPagos)) {
            return false;
        }
        RemesaPagos other = (RemesaPagos) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "RemesaPagos{" + "id=" + id + ", comprobanteId=" + comprobanteId + ", formaPago=" + formaPago + ", remesa=" + remesa + '}';
    }
}
