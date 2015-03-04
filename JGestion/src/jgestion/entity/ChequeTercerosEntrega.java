package jgestion.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.persistence.*;

/**
 *
 * @author FiruzzZ
 */
@Entity
@Table(name = "cheque_terceros_entrega")
@NamedQueries({
    @NamedQuery(name = "ChequeTercerosEntrega.findAll", query = "SELECT c FROM ChequeTercerosEntrega c")})
public class ChequeTercerosEntrega implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(nullable = false)
    private Integer id;
    @Basic(optional = false)
    @Column(name = "fecha_creacion", nullable = false, insertable = false, updatable = false, columnDefinition = "timestamp with time zone NOT NULL DEFAULT now()")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCreacion;
    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_emisor_id", nullable = false)
    private Usuario emisor;
    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_receptor_id", nullable = false)
    private Usuario receptor;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "chequeTercerosEntrega")
    private List<ChequeTercerosEntregaDetalle> detalle;

    public ChequeTercerosEntrega() {
    }

    public ChequeTercerosEntrega(Integer id) {
        this.id = id;
    }

    public ChequeTercerosEntrega(Usuario emisor, Usuario receptor, List<ChequeTercerosEntregaDetalle> detalle) {
        this.emisor = emisor;
        this.receptor = receptor;
        this.detalle = detalle;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Usuario getReceptor() {
        return receptor;
    }

    public void setReceptor(Usuario receptor) {
        this.receptor = receptor;
    }

    public Usuario getEmisor() {
        return emisor;
    }

    public void setEmisor(Usuario emisor) {
        this.emisor = emisor;
    }

    public List<ChequeTercerosEntregaDetalle> getDetalle() {
        return detalle;
    }

    public void setDetalle(List<ChequeTercerosEntregaDetalle> detalle) {
        this.detalle = detalle;
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
        if (!(object instanceof ChequeTercerosEntrega)) {
            return false;
        }
        ChequeTercerosEntrega other = (ChequeTercerosEntrega) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entity.ChequeTercerosEntrega[ id=" + id + " ]";
    }
}
