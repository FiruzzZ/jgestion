/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

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
import javax.persistence.Table;

/**
 *
 * @author Administrador
 */
@Entity
@Table(name = "detalle_remesa")
@NamedQueries({
    @NamedQuery(name = "DetalleRemesa.findAll", query = "SELECT d FROM DetalleRemesa d"),
    @NamedQuery(name = "DetalleRemesa.findById", query = "SELECT d FROM DetalleRemesa d WHERE d.id = :id"),
    @NamedQuery(name = "DetalleRemesa.findByMontoEntrega", query = "SELECT d FROM DetalleRemesa d WHERE d.montoEntrega = :montoEntrega"),
    @NamedQuery(name = "DetalleRemesa.findByObservacion", query = "SELECT d FROM DetalleRemesa d WHERE d.observacion = :observacion")})
public class DetalleRemesa implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Basic(optional = false)
    @Column(name = "monto_entrega", nullable = false)
    private double montoEntrega;
    @Column(name = "observacion", length = 200)
    private String observacion;
    @JoinColumn(name = "factura_compra", referencedColumnName = "id", nullable = false)
    @ManyToOne(optional = false)
    private FacturaCompra facturaCompra;
    @JoinColumn(name = "remesa", referencedColumnName = "id", nullable = false)
    @ManyToOne(optional = false)
    private Remesa remesa;

    public DetalleRemesa() {
    }

    public DetalleRemesa(Integer id) {
        this.id = id;
    }

    public DetalleRemesa(Integer id, double montoEntrega) {
        this.id = id;
        this.montoEntrega = montoEntrega;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public double getMontoEntrega() {
        return montoEntrega;
    }

    public void setMontoEntrega(double montoEntrega) {
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
        return "entity.DetalleRemesa[id=" + id + "]";
    }

}
