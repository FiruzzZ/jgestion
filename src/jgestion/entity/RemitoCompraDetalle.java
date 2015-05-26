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
import javax.persistence.Table;

/**
 *
 * @author FiruzzZ
 */
@Entity
@Table(name = "remito_compra_detalle")
public class RemitoCompraDetalle implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    private Integer id;
    @Basic(optional = false)
    @Column(name = "cantidad", nullable = false)
    private int cantidad;
    @Basic(optional = false)
    @Column(name = "bonificado", nullable = false)
    private boolean bonificado;
    @JoinColumn(name = "producto_id", nullable = false)
    @ManyToOne(optional = false)
    private Producto producto;
    @JoinColumn(name = "remito_compra_id", nullable = false)
    @ManyToOne(optional = false)
    private RemitoCompra remitoCompra;

    public RemitoCompraDetalle() {
    }

    public RemitoCompraDetalle(Producto producto, int cantidad, boolean bonificado) {
        this.producto = producto;
        this.cantidad = cantidad;
        this.bonificado = bonificado;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public boolean isBonificado() {
        return bonificado;
    }

    public void setBonificado(boolean bonificado) {
        this.bonificado = bonificado;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public RemitoCompra getRemitoCompra() {
        return remitoCompra;
    }

    public void setRemitoCompra(RemitoCompra remitoCompra) {
        this.remitoCompra = remitoCompra;
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
        if (!(object instanceof RemitoCompraDetalle)) {
            return false;
        }
        RemitoCompraDetalle other = (RemitoCompraDetalle) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "RemitoCompraDetalle{" + "id=" + id + ", cantidad=" + cantidad + ", bonificado=" + bonificado + ", producto=" + producto + '}';
    }

}
