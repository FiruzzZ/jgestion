package entity;

import java.io.Serializable;
import javax.persistence.*;

/**
 *
 * @author FiruzzzZ
 */
@Entity
@Table(name = "detalle_compra")
@NamedQueries({
    @NamedQuery(name = "DetalleCompra.findAll", query = "SELECT d FROM DetalleCompra d"),
    @NamedQuery(name = "DetalleCompra.findById", query = "SELECT d FROM DetalleCompra d WHERE d.id = :id")
})

public class DetalleCompra implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Integer id;
    @Basic(optional = false)
    @Column(name = "cantidad", nullable = false)
    private int cantidad;
    @Basic(optional = false)
    @Column(name = "precio_unitario", nullable = false, precision = 12, scale = 4)
    private Double precioUnitario;
    @JoinColumn(name = "factura", referencedColumnName = "id", nullable = false)
    @ManyToOne(optional = false)
    private FacturaCompra factura;
    @JoinColumn(name = "producto", referencedColumnName = "id", nullable = false)
    @ManyToOne(optional = false)
    private Producto producto;

    public DetalleCompra() {
    }

    public DetalleCompra(Integer id) {
        this.id = id;
    }

    public DetalleCompra(Integer id, int cantidad, Double precioUnitario) {
        this.id = id;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
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

    public Double getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(double precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public FacturaCompra getFactura() {
        return factura;
    }

    public void setFactura(FacturaCompra facturaCompra) {
        this.factura = facturaCompra;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
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
        if (!(object instanceof DetalleCompra)) {
            return false;
        }
        DetalleCompra other = (DetalleCompra) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return this.getId().toString() +"-"+ this.getProducto().getNombre();
    }

}
