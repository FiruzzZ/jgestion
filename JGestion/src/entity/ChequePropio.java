package entity;

import entity.enums.ChequeEstado;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Date;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author FiruzzZ
 */
@Entity
@Table(name = "cheque_propio", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"proveedor", "numero"})})
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "ChequePropio.findAll", query = "SELECT c FROM ChequePropio c"),
    @NamedQuery(name = "ChequePropio.findById", query = "SELECT c FROM ChequePropio c WHERE c.id = :id"),
    @NamedQuery(name = "ChequePropio.findByNumero", query = "SELECT c FROM ChequePropio c WHERE c.numero = :numero"),
    @NamedQuery(name = "ChequePropio.findByImporte", query = "SELECT c FROM ChequePropio c WHERE c.importe = :importe")})
public class ChequePropio extends Cheque implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Basic(optional = false)
    @Column(name = "estado", nullable = false)
    private int estado;
    @Transient
    private ChequeEstado chequeEstado;
    @Column(name = "endosatario", length = 200)
    private String endosatario;
    @Column(name = "fecha_endoso")
    @Temporal(TemporalType.DATE)
    private Date fechaEndoso;
    @JoinColumn(name = "usuario", referencedColumnName = "id", nullable = false)
    @ManyToOne(optional = false)
    private Usuario usuario;
    @JoinColumn(name = "librado", referencedColumnName = "id", nullable = false)
    @ManyToOne(optional = false)
    private Librado librado;
    @JoinColumn(name = "proveedor", referencedColumnName = "id", nullable = false)
    @ManyToOne(optional = false)
    private Proveedor proveedor;
    @JoinColumn(name = "banco_sucursal", referencedColumnName = "id", nullable = false)
    @ManyToOne(optional = false)
    private BancoSucursal bancoSucursal;
    @JoinColumn(name = "banco", referencedColumnName = "id", nullable = false)
    @ManyToOne(optional = false)
    private Banco banco;
    @Basic(optional = false)
    @Column(name = "bound")
    /**
     * 1=factura_compra, 3 =remesa
     */
    private int bound;
    @Column(name = "bound_id")
    private Long boundId;
    @Version
    @Column(name = "version_cheque")
    private Long version;

    public ChequePropio() {
    }

    public ChequePropio(Integer id, Long numero, Date fechaCheque, boolean cruzado, String observacion, ChequeEstado estado, Date fechaCobro, String endosatario, Date fechaEndoso, BigDecimal importe, Usuario usuario, Proveedor proveedor, Librado librado, BancoSucursal bancoSucursal, Banco banco) {
        this.id = id;
        this.numero = numero;
        this.fechaCheque = fechaCheque;
        this.cruzado = cruzado;
        this.observacion = observacion;
        this.chequeEstado = estado;
        this.estado = estado.getId();
        this.fechaCobro = fechaCobro;
        this.endosatario = endosatario;
        this.fechaEndoso = fechaEndoso;
        this.importe = importe;
        this.usuario = usuario;
        this.proveedor = proveedor;
        this.librado = librado;
        this.bancoSucursal = bancoSucursal;
        this.banco = banco;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

//    public Long getNumero() {
//        return numero;
//    }
//
//    public void setNumero(Long numero) {
//        this.numero = numero;
//    }
//
//    public Date getFechaCheque() {
//        return fechaCheque;
//    }
//
//    public void setFechaCheque(Date fechaCheque) {
//        this.fechaCheque = fechaCheque;
//    }
    public boolean getCruzado() {
        return cruzado;
    }

//    public void setCruzado(boolean cruzado) {
//        this.cruzado = cruzado;
//    }
//    public String getObservacion() {
//        return observacion;
//    }
//
//    public void setObservacion(String observacion) {
//        this.observacion = observacion;
//    }
    public ChequeEstado getEstado() {
        if (chequeEstado == null) {
            chequeEstado = ChequeEstado.findById(id);
        }
        return chequeEstado;
    }

    public void setEstado(ChequeEstado estado) {
        this.chequeEstado = estado;
        this.estado = estado.getId();
    }

//    public Date getFechaCreacion() {
//        return fechaCreacion;
//    }
//    public void setFechaCreacion(Date fechaCreacion) {
//        this.fechaCreacion = fechaCreacion;
//    }
//    public Date getFechaCobro() {
//        return fechaCobro;
//    }
//    public void setFechaCobro(Date fechaCobro) {
//        this.fechaCobro = fechaCobro;
//    }
    public String getEndosatario() {
        return endosatario;
    }

    public void setEndosatario(String endosatario) {
        this.endosatario = endosatario;
    }

    public Date getFechaEndoso() {
        return fechaEndoso;
    }

    public void setFechaEndoso(Date fechaEndoso) {
        this.fechaEndoso = fechaEndoso;
    }

//    public BigDecimal getImporte() {
//        return importe;
//    }
//    public void setImporte(BigDecimal importe) {
//        this.importe = importe;
//    }
    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Proveedor getProveedor() {
        return proveedor;
    }

    public void setProveedor(Proveedor proveedor) {
        this.proveedor = proveedor;
    }

    public Librado getLibrado() {
        return librado;
    }

    public void setLibrado(Librado librado) {
        this.librado = librado;
    }

    public BancoSucursal getBancoSucursal() {
        return bancoSucursal;
    }

    public void setBancoSucursal(BancoSucursal bancoSucursal) {
        this.bancoSucursal = bancoSucursal;
    }

    public Banco getBanco() {
        return banco;
    }

    public void setBanco(Banco banco) {
        this.banco = banco;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public int getBound() {
        return bound;
    }

    public void setBound(int bound) {
        this.bound = bound;
    }

    public Long getBoundId() {
        return boundId;
    }

    public void setBoundId(Long boundId) {
        this.boundId = boundId;
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
        if (!(object instanceof ChequePropio)) {
            return false;
        }
        ChequePropio other = (ChequePropio) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return super.toString() + "\nChequePropio{" + "id=" + id + ", estado=" + estado + ", chequeEstado=" + chequeEstado + ", endosatario=" + endosatario + ", fechaEndoso=" + fechaEndoso + ", usuario=" + usuario + ", proveedor=" + proveedor + ", librado=" + librado + ", bancoSucursal=" + bancoSucursal + ", banco=" + banco + ", bound=" + bound + ", boundId=" + boundId + ", version=" + version + '}';
    }
}
