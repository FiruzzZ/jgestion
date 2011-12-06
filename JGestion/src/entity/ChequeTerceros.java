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
@Table(name = "cheque_terceros", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"cliente", "numero"})})
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "ChequeTerceros.findAll", query = "SELECT c FROM ChequeTerceros c"),
    @NamedQuery(name = "ChequeTerceros.findById", query = "SELECT c FROM ChequeTerceros c WHERE c.id = :id"),
    @NamedQuery(name = "ChequeTerceros.findByNumero", query = "SELECT c FROM ChequeTerceros c WHERE c.numero = :numero"),
    @NamedQuery(name = "ChequeTerceros.findByImporte", query = "SELECT c FROM ChequeTerceros c WHERE c.importe = :importe")})
public class ChequeTerceros extends Cheque implements Serializable {

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
    @JoinColumn(name = "cliente", referencedColumnName = "id", nullable = false)
    @ManyToOne(optional = false)
    private Cliente cliente;
    @JoinColumn(name = "banco_sucursal", referencedColumnName = "id", nullable = false)
    @ManyToOne(optional = false)
    private BancoSucursal bancoSucursal;
    @JoinColumn(name = "banco", referencedColumnName = "id", nullable = false)
    @ManyToOne(optional = false)
    private Banco banco;
    @Basic(optional = false)
    @Column(name = "bound")
    /**
     * 2=factura_venta, 4=recibo
     */
    private int bound;
    @Column(name = "bound_id")
    /**
     * ID of the entity represented by the atribute bound
     */
    private Long boundId;
    @Version
    @Column(name = "version_cheque")
    private Long version;

    public ChequeTerceros() {
    }

    public ChequeTerceros(Integer id) {
        this.id = id;
    }

    public ChequeTerceros(Integer id, Long numero, Date fechaCheque, boolean cruzado, String observacion, ChequeEstado chequeEstado, Date fechaCobro, String endosatario, Date fechaEndoso, BigDecimal importe, Usuario usuario, Librado librado, Cliente cliente, BancoSucursal bancoSucursal, Banco banco) {
        this.id = id;
        this.numero = numero;
        this.fechaCheque = fechaCheque;
        this.cruzado = cruzado;
        this.observacion = observacion;
        this.chequeEstado = chequeEstado;
        this.estado = chequeEstado.getId();
        this.fechaCobro = fechaCobro;
        this.endosatario = endosatario;
        this.fechaEndoso = fechaEndoso;
        this.importe = importe;
        this.usuario = usuario;
        this.librado = librado;
        this.cliente = cliente;
        this.bancoSucursal = bancoSucursal;
        this.banco = banco;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public ChequeEstado getEstado() {
        return chequeEstado;
    }

    public void setEstado(ChequeEstado estado) {
        this.chequeEstado = estado;
        this.estado = estado.getId();
    }

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

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Librado getLibrado() {
        return librado;
    }

    public void setLibrado(Librado librado) {
        this.librado = librado;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
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
        if (!(object instanceof ChequeTerceros)) {
            return false;
        }
        ChequeTerceros other = (ChequeTerceros) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return super.toString() + "\nChequeTerceros{" + "id=" + id + ", estado=" + estado + ", chequeEstado=" + chequeEstado + ", endosatario=" + endosatario + ", fechaEndoso=" + fechaEndoso + ", usuario=" + usuario + ", librado=" + librado + ", cliente=" + cliente + ", bancoSucursal=" + bancoSucursal + ", banco=" + banco + ", bound=" + bound + ", boundId=" + boundId + ", version=" + version + '}';
    }
}
