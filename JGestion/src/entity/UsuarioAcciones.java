package entity;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author FiruzzZ
 */
@Entity
@Table(name = "usuario_acciones")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "UsuarioAcciones.findAll", query = "SELECT u FROM UsuarioAcciones u")})
public class UsuarioAcciones implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(nullable = false)
    private Integer id;
    @Basic(optional = false)
    @Column(nullable = false, length = 200)
    private String descripcion;
    @Column(length = 2000)
    private String detalle;
    @Basic(optional = false)
    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechasistema;
    @Column(length = 24)
    private String ip;
    @Column(length = 50)
    private String hostname;
    @Basic(optional = false)
    @Column(nullable = false, length = 100)
    private String entidad;
    @Basic(optional = false)
    @Column(name = "entidad_id", nullable = false)
    private Integer entidadId;
    @ManyToOne(optional = false)
    private Usuario usuario;

    public UsuarioAcciones() {
    }

    public UsuarioAcciones(Integer id) {
        this.id = id;
    }

    public UsuarioAcciones(Integer id, String descripcion, Date fechasistema, String entidad, int entidadId, Usuario usuario) {
        this.id = id;
        this.descripcion = descripcion;
        this.fechasistema = fechasistema;
        this.entidad = entidad;
        this.entidadId = entidadId;
        this.usuario = usuario;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDetalle() {
        return detalle;
    }

    public void setDetalle(String detalle) {
        this.detalle = detalle;
    }

    public Date getFechasistema() {
        return fechasistema;
    }

    public void setFechasistema(Date fechasistema) {
        this.fechasistema = fechasistema;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getEntidad() {
        return entidad;
    }

    public void setEntidad(String entidad) {
        this.entidad = entidad;
    }

    public Integer getEntidadId() {
        return entidadId;
    }

    public void setEntidadId(int entidadId) {
        this.entidadId = entidadId;
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
        if (!(object instanceof UsuarioAcciones)) {
            return false;
        }
        UsuarioAcciones other = (UsuarioAcciones) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entity.UsuarioAcciones[ id=" + id + " ]";
    }
}
