package entity;

import entity.enums.ChequeEstado;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.Version;

/**
 * Basic model of a Check.
 *
 * @author FiruzzZ
 */
@MappedSuperclass
@Inheritance(strategy = InheritanceType.JOINED)
@Access(AccessType.FIELD)
public abstract class Cheque implements Serializable {

    private static final long serialVersionUID = 1L;
    @Column(nullable = false, precision = 8)
    protected Long numero;
    @JoinColumn(name = "banco", nullable = false)
    @ManyToOne(optional = false)
    protected Banco banco;
//    @JoinColumn(name = "banco_sucursal", nullable = false)
    @JoinColumn(name = "banco_sucursal")
    @ManyToOne//(optional = false)
    protected BancoSucursal bancoSucursal;
    @Basic(optional = false)
    @Column(name = "importe", nullable = false, precision = 10, scale = 2)
    protected BigDecimal importe;
    @Basic(optional = false)
    @Column(name = "fecha_cheque", nullable = false)
    @Temporal(TemporalType.DATE)
    protected Date fechaCheque;
    @Basic(optional = false)
    @Column(name = "cruzado", nullable = false)
    protected boolean cruzado;
    @Column(name = "observacion", length = 300)
    protected String observacion;
    @Basic(optional = false)
    @Column(name = "fecha_cobro", nullable = false)
    @Temporal(TemporalType.DATE)
    protected Date fechaCobro;
    @Basic(optional = false)
    @Column(name = "fecha_creacion", nullable = false, insertable = false, updatable = false, columnDefinition = "timestamp with time zone NOT NULL DEFAULT now()")
    @Temporal(TemporalType.TIMESTAMP)
    protected Date fechaCreacion;
    @Basic(optional = false)
    @Column(name = "estado", nullable = false)
    protected int estado;
    @Transient
    protected transient ChequeEstado chequeEstado;
    @Column(name = "endosatario", length = 200)
    protected String endosatario;
    @Column(name = "fecha_endoso")
    @Temporal(TemporalType.DATE)
    protected Date fechaEndoso;
    @JoinColumn(name = "usuario", nullable = false)
    @ManyToOne(optional = false)
    protected Usuario usuario;
    @JoinColumn(name = "librado")
    @ManyToOne//(optional = false)
    protected Librado librado;
    @Version
    @Column(name = "version_cheque")
    protected Long version;

    protected Cheque() {
    }

    protected Cheque(Long numero, Banco banco, BancoSucursal bancoSucursal, BigDecimal importe, Date fechaCheque, Date fechaCobro, boolean cruzado, String observacion, ChequeEstado chequeEstado, String endosatario, Date fechaEndoso, Usuario usuario, Librado librado) {
        this.numero = numero;
        this.banco = banco;
        this.bancoSucursal = bancoSucursal;
        this.importe = importe;
        this.fechaCheque = fechaCheque;
        this.cruzado = cruzado;
        this.observacion = observacion;
        this.fechaCobro = fechaCobro;
        this.chequeEstado = chequeEstado;
        this.estado = chequeEstado.getId();
        this.endosatario = endosatario;
        this.fechaEndoso = fechaEndoso;
        this.usuario = usuario;
        this.librado = librado;
    }

    /**
     * Get the value of numero
     *
     * @return the value of numero
     */
    public Long getNumero() {
        return numero;
    }

    /**
     * Set the value of numero
     *
     * @param numero new value of numero
     */
    public void setNumero(Long numero) {
        this.numero = numero;
    }

    public Banco getBanco() {
        return banco;
    }

    public void setBanco(Banco banco) {
        this.banco = banco;
    }

    public BancoSucursal getBancoSucursal() {
        return bancoSucursal;
    }

    public void setBancoSucursal(BancoSucursal bancoSucursal) {
        this.bancoSucursal = bancoSucursal;
    }

    public boolean isCruzado() {
        return cruzado;
    }

    public void setCruzado(boolean cruzado) {
        this.cruzado = cruzado;
    }

    public Date getFechaCheque() {
        return fechaCheque;
    }

    public void setFechaCheque(Date fechaCheque) {
        this.fechaCheque = fechaCheque;
    }

    public Date getFechaCobro() {
        return fechaCobro;
    }

    public void setFechaCobro(Date fechaCobro) {
        this.fechaCobro = fechaCobro;
    }

    public Date getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public BigDecimal getImporte() {
        return importe;
    }

    public void setImporte(BigDecimal importe) {
        this.importe = importe;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public int getEstado() {
        return estado;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }

    public ChequeEstado getChequeEstado() {
        return ChequeEstado.findById(this.estado);
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

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Cheque other = (Cheque) obj;
        if (this.numero != other.numero && (this.numero == null || !this.numero.equals(other.numero))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 53 * hash + (this.numero != null ? this.numero.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "Cheque{" + "numero=" + numero + ", banco=" + banco.getId() + ", bancoSucursal=" + bancoSucursal.getId() + ", importe=" + importe + ", fechaCheque=" + fechaCheque + ", cruzado=" + cruzado + ", observacion=" + observacion + ", fechaCobro=" + fechaCobro + ", fechaCreacion=" + fechaCreacion + ", estado=" + estado + ", chequeEstado=" + chequeEstado + ", endosatario=" + endosatario + ", fechaEndoso=" + fechaEndoso + ", usuario=" + usuario + ", librado=" + librado + ", version=" + version + '}';
    }
}
