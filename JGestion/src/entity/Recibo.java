package entity;

import controller.ReciboController;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import javax.persistence.*;

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
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Basic(optional = false)
    @Column(name = "numero", nullable = false, length = 8)
    private Integer numero;
    @Basic(optional = false)
    @Column(name = "fecha_carga", nullable = false, insertable = false, updatable = false, columnDefinition = "timestamp with time zone NOT NULL DEFAULT now()")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCarga;
    @Basic(optional = false)
    @Column(name = "monto", nullable = false, precision = 12, scale = 2)
    private double monto;
    @Column(name = "retencion", precision = 10, scale = 2)
    @Deprecated
    private BigDecimal retencion;
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
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "recibo", orphanRemoval = true)
    private List<DetalleRecibo> detalleReciboList;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "recibo", orphanRemoval = true)
    private List<ReciboPagos> pagos;
    @Transient
    private transient List<Object> pagosEntities;

    public Recibo() {
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

    @Deprecated
    public BigDecimal getRetencion() {
        return retencion;
    }

    @Deprecated
    public void setRetencion(BigDecimal retencion) {
        this.retencion = retencion;
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

    public List<ReciboPagos> getPagos() {
        return pagos;
    }

    public void setPagos(List<ReciboPagos> pagos) {
        this.pagos = pagos;
    }

    /**
     * Usar {@link ReciboController#loadPagos(entity.Recibo) } para recuperar
     * las entidades pertinentes, antes de obtener esta collection.
     *
     * @return
     */
    public List<Object> getPagosEntities() {
        return pagosEntities;
    }

    public void setPagosEntities(List<Object> pagosEntities) {
        this.pagosEntities = pagosEntities;
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
        return "Recibo{" + "id=" + id + ", numero=" + numero + ", fechaCarga=" + fechaCarga + ", monto=" + monto + ", retencion=" + retencion + ", fechaRecibo=" + fechaRecibo + ", estado=" + estado + ", caja=" + caja + ", sucursal=" + sucursal + ", usuario=" + usuario + ", detalleReciboList=" + detalleReciboList.size() + '}';
    }
}
