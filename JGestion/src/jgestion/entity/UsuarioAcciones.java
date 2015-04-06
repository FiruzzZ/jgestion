package jgestion.entity;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
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
public class UsuarioAcciones implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(nullable = false)
    private Integer id;
    /**
     * i = insert, u = update, d =....
     */
    @Basic(optional = false)
    @Column(nullable = false, updatable = false)
    private char accion;
    @Basic(optional = false)
    @Column(nullable = false, length = 200)
    private String descripcion;
    @Column(length = 2000)
    private String detalle;
    @Basic(optional = false)
    @Column(nullable = false, insertable = false, updatable = false, columnDefinition = "timestamp NOT NULL DEFAULT now()")
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
    @Column(name = "owner_entity", length = 100)
    private String owner;
    @Column(name = "owner_id", length = 50)
    private String ownerID;

    public UsuarioAcciones() {
    }

    public UsuarioAcciones(char accion, String descripcion, String detalle, String entidad, Integer entidadId, Usuario usuario) {
        this(accion, descripcion, detalle, entidad, entidadId, usuario, null, null);
    }

    public UsuarioAcciones(char accion, String descripcion, String detalle, String entidad, Integer entidadId, Usuario usuario, String owner, String ownerID) {
        this.accion = accion;
        this.descripcion = descripcion;
        this.detalle = detalle;
        this.entidad = entidad;
        this.entidadId = entidadId;
        this.usuario = usuario;
        this.owner = owner;
        this.ownerID = ownerID;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public char getAccion() {
        return accion;
    }

    public void setAccion(char accion) {
        this.accion = accion;
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

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getOwnerID() {
        return ownerID;
    }

    public void setOwnerID(String ownerID) {
        this.ownerID = ownerID;
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
        return "UsuarioAcciones{" + "id=" + id + ", accion=" + accion + ", descripcion=" + descripcion + ", detalle=" + detalle + ", fechasistema=" + fechasistema + ", ip=" + ip + ", hostname=" + hostname + ", entidad=" + entidad + ", entidadId=" + entidadId + ", usuario=" + usuario + ", owner=" + owner + ", ownerID=" + ownerID + '}';
    }

}
