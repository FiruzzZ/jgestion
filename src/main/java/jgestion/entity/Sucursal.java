package jgestion.entity;

import java.io.Serializable;
import java.math.BigInteger;
import javax.persistence.*;

/**
 *
 * @author Administrador
 */
@Entity
@NamedQueries({
    @NamedQuery(name = "Sucursal.findAll", query = "SELECT s FROM Sucursal s ORDER BY s.nombre")
    ,
    @NamedQuery(name = "Sucursal.findById", query = "SELECT s FROM Sucursal s WHERE s.id = :id")
})
@Access(AccessType.FIELD)
public class Sucursal implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Basic(optional = false)
    @Column(name = "nombre", nullable = false, unique = true, length = 200)
    private String nombre;
    @Basic(optional = false)
    @Column(name = "direccion", nullable = false, length = 200)
    private String direccion;
    @Column(name = "tele1")
    private BigInteger tele1;
    @Column(name = "tele2")
    private BigInteger tele2;
    @Column(name = "estado")
    private Integer estado;
    @Column(name = "interno1")
    private Integer interno1;
    @Column(name = "interno2")
    private Integer interno2;
    @Column(name = "encargado", length = 200)
    private String encargado;
    @Column(name = "email", length = 60)
    private String email;
    @JoinColumn(name = "departamento", referencedColumnName = "iddepto", nullable = false)
    @ManyToOne(optional = false)
    private Departamento departamento;
    @JoinColumn(name = "municipio", referencedColumnName = "id", nullable = false)
    @ManyToOne
    private Municipio municipio;
    @JoinColumn(name = "provincia", referencedColumnName = "idprovincia", nullable = false)
    @ManyToOne(optional = false)
    private Provincia provincia;
    /**
     * No todas las sucursales son puntos de venta (no todas las sucus pueden estar habilitadas para
     * emitir comprobantes)
     */
    @Column(unique = true, precision = 4)
    private Integer puntoVenta;
    @Basic(optional = false)
    @Column(name = "factura_a", precision = 8, nullable = false)
    private Integer factura_a;
    @Basic(optional = false)
    @Column(name = "factura_b", precision = 8, nullable = false)
    private Integer factura_b;
    @Basic(optional = false)
    @Column(name = "factura_c", precision = 8, nullable = false)
    private Integer factura_c;
    @Basic(optional = false)
    @Column(name = "notacredito_a", precision = 8, nullable = false)
    private Integer notaCredito_a;
    @Basic(optional = false)
    @Column(name = "notacredito_b", precision = 8, nullable = false)
    private Integer notaCredito_b;
    @Basic(optional = false)
    @Column(name = "notacredito_c", precision = 8, nullable = false)
    private Integer notaCredito_c;
    @Basic(optional = false)
    @Column(name = "notadebito_a", precision = 8, nullable = false)
    private Integer notaDebitoA;
    @Basic(optional = false)
    @Column(name = "notadebito_b", precision = 8, nullable = false)
    private Integer notaDebitoB;
    @Basic(optional = false)
    @Column(name = "notadebito_c", precision = 8, nullable = false)
    private Integer notaDebitoC;
    @Basic(optional = false)
    @Column(name = "recibo_a", precision = 8, nullable = false)
    private Integer recibo_a;
    @Basic(optional = false)
    @Column(name = "recibo_b", precision = 8, nullable = false)
    private Integer recibo_b;
    @Basic(optional = false)
    @Column(name = "recibo_c", precision = 8, nullable = false)
    private Integer recibo_c;
    @Basic(optional = false)
    @Column(name = "recibo_x", precision = 8, nullable = false)
    private Integer recibo_x;
    @Basic(optional = false)
    @Column(precision = 8, nullable = false)
    private Integer remito;
    @Column(name = "webservices", nullable = false)
    private boolean webServices;

    public Sucursal() {
    }

    public Sucursal(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public BigInteger getTele1() {
        return tele1;
    }

    public void setTele1(BigInteger tele1) {
        this.tele1 = tele1;
    }

    public BigInteger getTele2() {
        return tele2;
    }

    public void setTele2(BigInteger tele2) {
        this.tele2 = tele2;
    }

    public Integer getEstado() {
        return estado;
    }

    public void setEstado(Integer estado) {
        this.estado = estado;
    }

    public Integer getInterno1() {
        return interno1;
    }

    public void setInterno1(Integer interno1) {
        this.interno1 = interno1;
    }

    public Integer getInterno2() {
        return interno2;
    }

    public void setInterno2(Integer interno2) {
        this.interno2 = interno2;
    }

    public String getEncargado() {
        return encargado;
    }

    public void setEncargado(String encargado) {
        this.encargado = encargado;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Departamento getDepartamento() {
        return departamento;
    }

    public void setDepartamento(Departamento departamento) {
        this.departamento = departamento;
    }

    public Municipio getMunicipio() {
        return municipio;
    }

    public void setMunicipio(Municipio municipio) {
        this.municipio = municipio;
    }

    public Provincia getProvincia() {
        return provincia;
    }

    public void setProvincia(Provincia provincia) {
        this.provincia = provincia;
    }

    public Integer getPuntoVenta() {
        return puntoVenta;
    }

    public void setPuntoVenta(Integer puntoVenta) {
        this.puntoVenta = puntoVenta;
    }

    public Integer getFactura_a() {
        return factura_a;
    }

    public void setFactura_a(Integer factura_a) {
        this.factura_a = factura_a;
    }

    public Integer getFactura_b() {
        return factura_b;
    }

    public void setFactura_b(Integer factura_b) {
        this.factura_b = factura_b;
    }

    public Integer getFactura_c() {
        return factura_c;
    }

    public void setFactura_c(Integer factura_c) {
        this.factura_c = factura_c;
    }

    public Integer getNotaCredito_a() {
        return notaCredito_a;
    }

    public void setNotaCredito_a(Integer notaCredito_a) {
        this.notaCredito_a = notaCredito_a;
    }

    public Integer getNotaCredito_b() {
        return notaCredito_b;
    }

    public void setNotaCredito_b(Integer notaCredito_b) {
        this.notaCredito_b = notaCredito_b;
    }

    public Integer getNotaCredito_c() {
        return notaCredito_c;
    }

    public void setNotaCredito_c(Integer notaCredito_c) {
        this.notaCredito_c = notaCredito_c;
    }

    public Integer getNotaDebitoA() {
        return notaDebitoA;
    }

    public void setNotaDebitoA(Integer notaDebitoA) {
        this.notaDebitoA = notaDebitoA;
    }

    public Integer getNotaDebitoB() {
        return notaDebitoB;
    }

    public void setNotaDebitoB(Integer notaDebitoB) {
        this.notaDebitoB = notaDebitoB;
    }

    public Integer getNotaDebitoC() {
        return notaDebitoC;
    }

    public void setNotaDebitoC(Integer notaDebitoC) {
        this.notaDebitoC = notaDebitoC;
    }

    public Integer getRecibo_a() {
        return recibo_a;
    }

    public void setRecibo_a(Integer recibo_a) {
        this.recibo_a = recibo_a;
    }

    public Integer getRecibo_b() {
        return recibo_b;
    }

    public void setRecibo_b(Integer recibo_b) {
        this.recibo_b = recibo_b;
    }

    public Integer getRecibo_c() {
        return recibo_c;
    }

    public void setRecibo_c(Integer recibo_c) {
        this.recibo_c = recibo_c;
    }

    public Integer getRecibo_x() {
        return recibo_x;
    }

    public void setRecibo_x(Integer recibo_x) {
        this.recibo_x = recibo_x;
    }

    public Integer getRemito() {
        return remito;
    }

    public void setRemito(Integer remito) {
        this.remito = remito;
    }

    public boolean isWebServices() {
        return webServices;
    }

    public void setWebServices(boolean webServices) {
        this.webServices = webServices;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Sucursal)) {
            return false;
        }
        Sucursal other = (Sucursal) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Sucursal{" + "id=" + id + ", nombre=" + nombre + ", direccion=" + direccion
                + ", tele1=" + tele1 + ", tele2=" + tele2 + ", estado=" + estado
                + ", interno1=" + interno1 + ", interno2=" + interno2 + ", encargado=" + encargado
                + ", email=" + email + ", departamento=" + departamento + ", municipio=" + municipio
                + ", provincia=" + provincia + ", puntoVenta=" + puntoVenta + ", factura_a=" + factura_a
                + ", factura_b=" + factura_b + ", factura_c=" + factura_c + ", notaCredito_a=" + notaCredito_a
                + ", notaCredito_b=" + notaCredito_b + ", notaCredito_c=" + notaCredito_c
                + ", notaDebitoA=" + notaDebitoA + ", notaDebitoB=" + notaDebitoB + ", notaDebitoC=" + notaDebitoC
                + ", recibo_a=" + recibo_a + ", recibo_b=" + recibo_b + ", recibo_c=" + recibo_c + ", recibo_x="
                + recibo_x + ", remito=" + remito + ", webServices=" + webServices + '}';
    }

}
