package entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.persistence.*;
import utilities.general.UTIL;

/**
 *
 * @author Administrador
 */
@Entity
@Table(name = "recibo", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"numero", "sucursal"})
})
@NamedQueries({
    @NamedQuery(name = "Recibo.findAll", query = "SELECT r FROM Recibo r"),
    @NamedQuery(name = "Recibo.findById", query = "SELECT r FROM Recibo r WHERE r.id = :id"),
    @NamedQuery(name = "Recibo.findByEstado", query = "SELECT r FROM Recibo r WHERE r.estado = :estado")
})
public class Recibo implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Basic(optional = false)
    @Column(nullable = false)
    private Integer numero;
    @Basic(optional = false)
    @Column(name = "fecha_carga", nullable = false, insertable = false, updatable = false, columnDefinition = "timestamp with time zone NOT NULL DEFAULT now()")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCarga;
    @Basic(optional = false)
    @Column(name = "monto", nullable = false)
    private double monto;
    @Basic(optional = false)
    @Column(name = "fecha_recibo", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date fechaRecibo;
    @Basic(optional = false)
    @Column(name = "estado", nullable = false)
    private boolean estado;
    @JoinColumn(name = "caja", referencedColumnName = "id", nullable = false)
    @ManyToOne(optional = false)
    private Caja caja;
    @JoinColumn(name = "sucursal", referencedColumnName = "id", nullable = false)
    @ManyToOne(optional = false)
    private Sucursal sucursal;
    @JoinColumn(name = "usuario", referencedColumnName = "id", nullable = false)
    @ManyToOne(optional = false)
    private Usuario usuario;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "recibo")
    private List<DetalleRecibo> detalleReciboList;

    public Recibo() {
    }

    public Recibo(Integer numero, Date fechaCarga, double monto, Date fechaRecibo, boolean estado, Caja caja, Sucursal sucursal, Usuario usuario, List<DetalleRecibo> detalleReciboList) {
        this.numero = numero;
        this.fechaCarga = fechaCarga;
        this.monto = monto;
        this.fechaRecibo = fechaRecibo;
        this.estado = estado;
        this.caja = caja;
        this.sucursal = sucursal;
        this.usuario = usuario;
        this.detalleReciboList = detalleReciboList;
    }

    public Integer getNumero() {
        return numero;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    public Date getFechaCarga() {
        return fechaCarga;
    }

    public double getMonto() {
        return monto;
    }

    public void setMonto(double monto) {
        this.monto = monto;
    }

    public Date getFechaRecibo() {
        return fechaRecibo;
    }

    public void setFechaRecibo(Date fechaRecibo) {
        this.fechaRecibo = fechaRecibo;
    }

    public boolean getEstado() {
        return estado;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Caja getCaja() {
        return caja;
    }

    public void setCaja(Caja caja) {
        this.caja = caja;
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

    public List<DetalleRecibo> getDetalleReciboList() {
        return detalleReciboList;
    }

    public void setDetalleReciboList(List<DetalleRecibo> detalleReciboList) {
        this.detalleReciboList = detalleReciboList;
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
        if (!(object instanceof Recibo)) {
            return false;
        }
        Recibo other = (Recibo) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Recibo{" + "id=" + id + ", numero=" + numero + ", fechaCarga=" + fechaCarga + ", monto=" + monto + ", fechaRecibo=" + fechaRecibo + ", estado=" + estado + ", caja=" + caja + ", sucursal=" + sucursal + ", usuario=" + usuario + ", detalleReciboList=" + detalleReciboList.size() + '}';
    }
}
