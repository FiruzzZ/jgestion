package jgestion.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
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
 *
 * @author FiruzzZ
 */
@Entity
@Table(name = "detalle_nota_credito_proveedor")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "DetalleNotaCreditoProveedor.findAll", query = "SELECT d FROM DetalleNotaCreditoProveedor d")})
public class DetalleNotaCreditoProveedor implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(nullable = false)
    private Integer id;
    @Basic(optional = false)
    @Column(nullable = false)
    private int cantidad;
    @Basic(optional = false)
    @Column(name = "precio_unitario", nullable = false, precision = 12, scale = 4)
    private BigDecimal precioUnitario;
    @JoinColumn(name = "producto", nullable = false)
    @ManyToOne(optional = false, fetch = FetchType.EAGER, cascade = {CascadeType.REFRESH})
    private Producto producto;
    @JoinColumn(name = "nota_credito_proveedor", nullable = false)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private NotaCreditoProveedor notaCreditoProveedor;

    public DetalleNotaCreditoProveedor() {
    }

    public DetalleNotaCreditoProveedor(Integer id) {
        this.id = id;
    }

    public DetalleNotaCreditoProveedor(Integer id, int cantidad, BigDecimal precioUnitario, Producto producto, NotaCreditoProveedor notaCreditoProveedor) {
        this.id = id;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.producto = producto;
        this.notaCreditoProveedor = notaCreditoProveedor;
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

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public NotaCreditoProveedor getNotaCreditoProveedor() {
        return notaCreditoProveedor;
    }

    public void setNotaCreditoProveedor(NotaCreditoProveedor notaCreditoProveedor) {
        this.notaCreditoProveedor = notaCreditoProveedor;
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
        if (!(object instanceof DetalleNotaCreditoProveedor)) {
            return false;
        }
        DetalleNotaCreditoProveedor other = (DetalleNotaCreditoProveedor) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "DetalleNotaCreditoProveedor{" + "id=" + id + ", cantidad=" + cantidad + ", precioUnitario=" + precioUnitario + ", producto=" + producto + '}';
    }
}
