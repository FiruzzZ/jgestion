package entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.persistence.*;

/**
 *
 * @author Administrador
 */
@Entity
@Table(name = "remito", uniqueConstraints =
@UniqueConstraint(columnNames = {"numero"}))
@NamedQueries({
    @NamedQuery(name = "Remito.findAll", query = "SELECT r FROM Remito r"),
    @NamedQuery(name = "Remito.findById", query = "SELECT r FROM Remito r WHERE r.id = :id"),
    @NamedQuery(name = "Remito.findByNumero", query = "SELECT r FROM Remito r WHERE r.numero = :numero")
})
public class Remito implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Basic(optional = false)
    @Column(name = "numero", nullable = false, unique = true, scale = 12, precision = 0)
    private long numero;
    @Basic(optional = false)
    @Column(name = "fecha_remito", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date fechaRemito;
    @Column(name = "fecha_creacion", nullable = false, insertable = false, updatable = false, columnDefinition = "timestamp with time zone NOT NULL DEFAULT now()")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCreacion;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "remito")
    private List<DetalleRemito> detalleRemitoList;
    @JoinColumn(name = "cliente", referencedColumnName = "id", nullable = false)
    @ManyToOne(optional = false)
    private Cliente cliente;
    @JoinColumn(name = "sucursal", referencedColumnName = "id", nullable = false)
    @ManyToOne(optional = false)
    private Sucursal sucursal;
    @JoinColumn(name = "usuario", referencedColumnName = "id", nullable = false)
    @ManyToOne(optional = false)
    private Usuario usuario;
    @JoinColumn(name = "factura_venta", referencedColumnName = "id")
    @OneToOne
    private FacturaVenta facturaVenta;

    public Remito() {
    }

    public Remito(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public long getNumero() {
        return numero;
    }

    public void setNumero(long numero) {
        this.numero = numero;
    }

    public Date getFechaRemito() {
        return fechaRemito;
    }

    public void setFechaRemito(Date fechaRemito) {
        this.fechaRemito = fechaRemito;
    }

    public Date getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public List<DetalleRemito> getDetalleRemitoList() {
        return detalleRemitoList;
    }

    public void setDetalleRemitoList(List<DetalleRemito> detalleRemitoList) {
        this.detalleRemitoList = detalleRemitoList;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
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

    public FacturaVenta getFacturaVenta() {
        return facturaVenta;
    }

    public void setFacturaVenta(FacturaVenta facturaVenta) {
        this.facturaVenta = facturaVenta;
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
        if (!(object instanceof Remito)) {
            return false;
        }
        Remito other = (Remito) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Remito{" + "id=" + id + ", numero=" + numero + ", fechaRemito=" + fechaRemito + ", fechaCreacion=" + fechaCreacion + ", detalleRemitoList=" + detalleRemitoList.size() + ", cliente=" + cliente + ", sucursal=" + sucursal + ", usuario=" + usuario + ", facturaVenta=" + facturaVenta + '}';
    }
}
