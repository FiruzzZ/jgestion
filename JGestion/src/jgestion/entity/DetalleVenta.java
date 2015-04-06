package jgestion.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import javax.persistence.*;

/**
 *
 * @author Administrador
 */
@Entity
@Table(name = "detalle_venta")
@NamedQueries({
    @NamedQuery(name = "DetalleVenta.findAll", query = "SELECT d FROM DetalleVenta d"),
    @NamedQuery(name = "DetalleVenta.findById", query = "SELECT d FROM DetalleVenta d WHERE d.id = :id")
})
public class DetalleVenta implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Basic(optional = false)
    @Column(name = "cantidad", nullable = false)
    private int cantidad;
    @Basic(optional = false)
    @Column(name = "precio_unitario", nullable = false, precision = 12, scale = 4)
    private BigDecimal precioUnitario;
    @Basic(optional = false)
    @Column(name = "costo_compra", nullable = false, precision = 12, scale = 4)
    private BigDecimal costoCompra;
    @Basic(optional = false)
    @Column(name = "tipo_desc", nullable = false)
    private int tipoDesc;
    /**
     * Descuento ya está multiplicado por {@link DetalleVenta#cantidad}
     */
    @Basic(optional = false)
    @Column(name = "descuento", nullable = false, precision = 12, scale = 4)
    private BigDecimal descuento;
    @JoinColumn(name = "factura", nullable = false)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private FacturaVenta factura;
    @JoinColumn(name = "producto", referencedColumnName = "id", nullable = false)
    @ManyToOne(optional = false)
    private Producto producto;
    @JoinColumn(name = "oferta", referencedColumnName = "id")
    @ManyToOne
    private HistorialOfertas oferta;

    public DetalleVenta() {
    }

    public DetalleVenta(Integer id) {
        this.id = id;
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

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public int getTipoDesc() {
        return tipoDesc;
    }

    public void setTipoDesc(int tipoDesc) {
        this.tipoDesc = tipoDesc;
    }

    public BigDecimal getDescuento() {
        return descuento;
    }

    public void setDescuento(BigDecimal descuento) {
        this.descuento = descuento;
    }

    public FacturaVenta getFactura() {
        return factura;
    }

    public void setFactura(FacturaVenta factura) {
        this.factura = factura;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public HistorialOfertas getOferta() {
        return oferta;
    }

    public void setOferta(HistorialOfertas oferta) {
        this.oferta = oferta;
    }

    public BigDecimal getCostoCompra() {
        return costoCompra;
    }

    public void setCostoCompra(BigDecimal costoCompra) {
        this.costoCompra = costoCompra;
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
        if (!(object instanceof DetalleVenta)) {
            return false;
        }
        DetalleVenta other = (DetalleVenta) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "DetalleVenta{" + "id=" + id + ", cantidad=" + cantidad + ", precioUnitario=" + precioUnitario + ", tipoDesc=" + tipoDesc + ", descuento=" + descuento + ", factura=" + (factura.getId() != null ? factura.getId() : null) + ", producto=" + producto.getId() + ", oferta=" + oferta + '}';
    }
}
