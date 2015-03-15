package jgestion.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.*;

/**
 *
 * @author Administrador
 */
@Entity
@Table(name = "usuario", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"nick"})})
@NamedQueries({
    @NamedQuery(name = "Usuario.findAll", query = "SELECT u FROM Usuario u ORDER BY u.nick"),
    @NamedQuery(name = "Usuario.findById", query = "SELECT u FROM Usuario u WHERE u.id = :id"),
    @NamedQuery(name = "Usuario.findByNick", query = "SELECT u FROM Usuario u WHERE u.nick = :nick"),
    @NamedQuery(name = "Usuario.findByEstado", query = "SELECT u FROM Usuario u WHERE u.estado = :estado")
})
public class Usuario implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Basic(optional = false)
    @Column(name = "nick", nullable = false, length = 50)
    private String nick;
    @Basic(optional = false)
    @Column(name = "pass", nullable = false, length = 50)
    private String pass;
    @Basic(optional = false)
    @Column(name = "activo", nullable = false)
    private boolean activo;
    @Basic(optional = false)
    @Column(name = "fechaalta", nullable = false, insertable = false, updatable = false, columnDefinition = "timestamp with time zone NOT NULL DEFAULT now()")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaalta;
    @JoinColumn(name = "permisos", referencedColumnName = "id")
    @OneToOne
    private Permisos permisos;
    @OneToMany(mappedBy = "usuario")
    private List<Stock> stockList;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "usuario")
    private List<PermisosCaja> permisosCajaList;
//    @JoinColumn(name="sucursal_id") pedazo de mierda!!
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PermisosSucursal> sucursales;
    @OneToMany(mappedBy = "usuario")
    private List<UsuarioAcciones> usuarioAccionesList;

    public Usuario() {
    }

    public Usuario(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public boolean getActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public Date getFechaalta() {
        return fechaalta;
    }

    public void setFechaalta(Date fechaalta) {
        this.fechaalta = fechaalta;
    }

    public Permisos getPermisos() {
        return permisos;
    }

    public void setPermisos(Permisos permisos) {
        this.permisos = permisos;
    }

    public List<Stock> getStockList() {
        return stockList;
    }

    public void setStockList(List<Stock> stockList) {
        this.stockList = stockList;
    }

    public List<PermisosCaja> getPermisosCajaList() {
        if (permisos == null) {
            permisosCajaList = new ArrayList<PermisosCaja>(5);
        }
        return permisosCajaList;
    }

    public void setPermisosCajaList(List<PermisosCaja> permisosCajaList) {
        this.permisosCajaList = permisosCajaList;
    }

    public List<PermisosSucursal> getSucursales() {
        if (sucursales == null) {
            sucursales = new ArrayList<PermisosSucursal>(5);
        }
        return sucursales;
    }

    public void setSucursales(List<PermisosSucursal> sucursales) {
        this.sucursales = sucursales;
    }

    public List<UsuarioAcciones> getUsuarioAccionesList() {
        return usuarioAccionesList;
    }

    public void setUsuarioAccionesList(List<UsuarioAcciones> usuarioAccionesList) {
        this.usuarioAccionesList = usuarioAccionesList;
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
        if (!(object instanceof Usuario)) {
            return false;
        }
        Usuario other = (Usuario) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return this.getNick();
    }
}
