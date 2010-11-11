package entity;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;

/**
 *
 * @author FiruzzZ
 */

@Entity
@Table(name = "stock", uniqueConstraints = {@UniqueConstraint(columnNames = {"sucursal", "producto"})})
@NamedQueries({
    @NamedQuery(name = "Stock.findAll", query = "SELECT s FROM Stock s"),
    @NamedQuery(name = "Stock.findById", query = "SELECT s FROM Stock s WHERE s.id = :id"),
    @NamedQuery(name = "Stock.findByProducto", query = "SELECT s FROM Stock s WHERE s.producto.id = :producto")
})

public class Stock implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Integer id;
    @Basic(optional = false)
    @Column(name = "stock_sucu", nullable = false)
    private int stockSucu;
    @Basic(optional = false)
    @Column(name = "fecha_carga", nullable = false, columnDefinition="timestamp with time zone NOT NULL")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCarga;
    @JoinColumn(name = "producto", referencedColumnName = "id", nullable = false)
    @ManyToOne(optional = false)
    private Producto producto;
    @JoinColumn(name = "sucursal", referencedColumnName = "id", nullable = false)
    @ManyToOne(optional = false)
    private Sucursal sucursal;
    @JoinColumn(name = "usuario", referencedColumnName = "id")
    @ManyToOne
    private Usuario usuario;

    public Stock() {
    }

    public Stock(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getStockSucu() {
        return stockSucu;
    }

    public void setStockSucu(int stockSucu) {
        this.stockSucu = stockSucu;
    }

    public Date getFechaCarga() {
        return fechaCarga;
    }

    public void setFechaCarga(Date fechaCarga) {
        this.fechaCarga = fechaCarga;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public Sucursal getSucursal() {
        return sucursal;
    }

    public void setSucursal(Sucursal sucursal) {
        this.sucursal = sucursal;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
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
        if (!(object instanceof Stock)) {
            return false;
        }
        Stock other = (Stock) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return String.valueOf(this.getStockSucu());
    }

}
