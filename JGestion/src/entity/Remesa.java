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
@Table(name = "remesa",
uniqueConstraints = {
    @UniqueConstraint(columnNames = {"sucursal", "numero"})
})
@NamedQueries({
    @NamedQuery(name = "Remesa.findAll", query = "SELECT r FROM Remesa r"),
    @NamedQuery(name = "Remesa.findById", query = "SELECT r FROM Remesa r WHERE r.id = :id"),
    @NamedQuery(name = "Remesa.findByEstado", query = "SELECT r FROM Remesa r WHERE r.estado = :estado")
})
public class Remesa implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Basic(optional = false)
    @Column(name = "numero", nullable = false, precision = 8)
    private Integer numero;
    @Basic(optional = false)
    @Column(name = "fecha_carga", nullable = false, insertable = false, updatable = false, columnDefinition = "timestamp with time zone DEFAULT now()")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCarga;
    @Basic(optional = false)
    @Column(name = "monto_entrega", nullable = false)
    private double montoEntrega;
    @Basic(optional = false)
    @Column(name = "fecha_remesa", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date fechaRemesa;
    @Basic(optional = false)
    @Column(name = "estado", nullable = false)
    private boolean estado;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "remesa", orphanRemoval = true)
    private List<DetalleRemesa> detalleRemesaList;
    @JoinColumn(name = "caja", referencedColumnName = "id", nullable = false)
    @ManyToOne(optional = false)
    private Caja caja;
    @JoinColumn(name = "sucursal", referencedColumnName = "id", nullable = false)
    @ManyToOne(optional = false)
    private Sucursal sucursal;
    @JoinColumn(name = "usuario", referencedColumnName = "id", nullable = false)
    @ManyToOne(optional = false)
    private Usuario usuario;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "remesa", orphanRemoval = true)
    private List<RemesaPagos> pagos;
    @Transient
    private transient List<Object> pagosEntities;

    public Remesa() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public void setFechaCarga(Date fechaCarga) {
        this.fechaCarga = fechaCarga;
    }

    public double getMonto() {
        return montoEntrega;
    }

    public void setMontoEntrega(double montoEntrega) {
        this.montoEntrega = montoEntrega;
    }

    public Date getFechaRemesa() {
        return fechaRemesa;
    }

    public void setFechaRemesa(Date fechaRemesa) {
        this.fechaRemesa = fechaRemesa;
    }

    public boolean getEstado() {
        return estado;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }

    public List<DetalleRemesa> getDetalleRemesaList() {
        return detalleRemesaList;
    }

    public void setDetalleRemesaList(List<DetalleRemesa> detalleRemesaList) {
        this.detalleRemesaList = detalleRemesaList;
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

    public List<RemesaPagos> getPagos() {
        return pagos;
    }

    public void setPagos(List<RemesaPagos> pagos) {
        this.pagos = pagos;
    }

    public List<Object> getPagosEntities() {
        return pagosEntities;
    }

    public void setPagosEntities(List<Object> pagosEntities) {
        this.pagosEntities = pagosEntities;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (numero != null ? numero.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Remesa)) {
            return false;
        }
        Remesa other = (Remesa) object;
        if ((this.numero == null && other.numero != null) || (this.numero != null && !this.numero.equals(other.numero))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Remesa{" + "id=" + id + ", numero=" + numero + ", fechaCarga=" + fechaCarga + ", montoEntrega=" + montoEntrega + ", fechaRemesa=" + fechaRemesa + ", estado=" + estado + ", detalleRemesaList=" + detalleRemesaList + ", caja=" + caja + ", sucursal=" + sucursal + ", usuario=" + usuario + ", pagos=" + pagos + ", pagosEntities=" + pagosEntities + '}';
    }
}
