
package entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author Administrador
 */
@Entity
@Table(name = "remesa")
@NamedQueries({
    @NamedQuery(name = "Remesa.findAll", query = "SELECT r FROM Remesa r"),
    @NamedQuery(name = "Remesa.findById", query = "SELECT r FROM Remesa r WHERE r.id = :id"),
    @NamedQuery(name = "Remesa.findByFechaCarga", query = "SELECT r FROM Remesa r WHERE r.fechaCarga = :fechaCarga"),
    @NamedQuery(name = "Remesa.findByHoraCarga", query = "SELECT r FROM Remesa r WHERE r.horaCarga = :horaCarga"),
    @NamedQuery(name = "Remesa.findByMontoEntrega", query = "SELECT r FROM Remesa r WHERE r.montoEntrega = :montoEntrega"),
    @NamedQuery(name = "Remesa.findByFechaRemesa", query = "SELECT r FROM Remesa r WHERE r.fechaRemesa = :fechaRemesa"),
    @NamedQuery(name = "Remesa.findByEstado", query = "SELECT r FROM Remesa r WHERE r.estado = :estado")})
public class Remesa implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    private Long id;
    @Basic(optional = false)
    @Column(name = "fecha_carga", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date fechaCarga;
    @Basic(optional = false)
    @Column(name = "hora_carga", nullable = false)
    @Temporal(TemporalType.TIME)
    private Date horaCarga;
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
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "remesa")
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

    public Remesa() {
    }

    public Remesa(Long id) {
        this.id = id;
    }

    public Remesa(Long id, Date fechaCarga, Date horaCarga, double montoEntrega, Date fechaRemesa, boolean estado) {
        this.id = id;
        this.fechaCarga = fechaCarga;
        this.horaCarga = horaCarga;
        this.montoEntrega = montoEntrega;
        this.fechaRemesa = fechaRemesa;
        this.estado = estado;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getFechaCarga() {
        return fechaCarga;
    }

    public void setFechaCarga(Date fechaCarga) {
        this.fechaCarga = fechaCarga;
    }

    public Date getHoraCarga() {
        return horaCarga;
    }

    public void setHoraCarga(Date horaCarga) {
        this.horaCarga = horaCarga;
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

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Remesa)) {
            return false;
        }
        Remesa other = (Remesa) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return UTIL.AGREGAR_CEROS(this.getId(), 12);
    }

}
