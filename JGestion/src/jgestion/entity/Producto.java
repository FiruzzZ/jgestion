package jgestion.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import javax.persistence.*;
import org.eclipse.persistence.config.QueryHints;

/**
 *
 * @author FiruzzZ
 */
@Entity
@Table(name = "producto", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"nombre"}),
    @UniqueConstraint(columnNames = {"codigo"})
})
@NamedQueries({
    @NamedQuery(name = "Producto.findAll", query = "SELECT p FROM Producto p ORDER BY p.nombre",
            hints
            = @QueryHint(name = QueryHints.REFRESH, value = "true")),
    @NamedQuery(name = "Producto.findById", query = "SELECT p FROM Producto p WHERE p.id = :id",
            hints
            = @QueryHint(name = QueryHints.REFRESH, value = "true")),
    @NamedQuery(name = "Producto.findByCodigo", query = "SELECT p FROM Producto p WHERE p.codigo = :codigo",
            hints
            = @QueryHint(name = QueryHints.REFRESH, value = "true")),
    @NamedQuery(name = "Producto.findByNombre", query = "SELECT p FROM Producto p WHERE p.nombre = :nombre")
})
@SqlResultSetMappings({
    //para los ComboBox
    @SqlResultSetMapping(name = "ProductoToBuscador", entities = {
        @EntityResult(entityClass = Producto.class, fields = {
            @FieldResult(name = "id", column = "id"),
            @FieldResult(name = "codigo", column = "codigo"),
            @FieldResult(name = "nombre", column = "nombre")
        })
    }),
//para la GUI de Contenedor de Productos...
    @SqlResultSetMapping(name = "ProductoToContenedor", entities = {
        @EntityResult(entityClass = Producto.class, fields = {
            @FieldResult(name = "id", column = "id"),
            @FieldResult(name = "codigo", column = "codigo"),
            @FieldResult(name = "nombre", column = "nombre"),
            @FieldResult(name = "marca", column = "marca"),
            @FieldResult(name = "stockactual", column = "stockactual")
        })
    })
})
public class Producto implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "codigo", length = 50)
    private String codigo;
    @Column(name = "nombre", length = 250)
    private String nombre;
    @Basic(optional = false)
    @Column(name = "stockmaximo", nullable = false)
    private int stockmaximo;
    @Basic(optional = false)
    @Column(name = "stockactual", nullable = false)
    private int stockactual;
    @Basic(optional = false)
    @Column(name = "stockminimo", nullable = false)
    private int stockminimo;
    @Column(name = "deposito")
    private Integer deposito;
    @Column(name = "ubicacion", length = 50)
    private String ubicacion;
    @Column(name = "costo_compra", precision = 12, scale = 4)
    /**
     * Cuando se crea el producto es setteado a 0. Después es modificado por las
     * Facturas de compra.
     */
    private BigDecimal costoCompra;
    @Column(name = "descripcion", length = 2000)
    private String descripcion;
    @Column(nullable = false)
    private boolean updatePrecioVenta;
    @Basic(optional = false)
    @Column(name = "precio_venta", precision = 12, scale = 4, nullable = false)
    private BigDecimal precioVenta;
    @Column(name = "foto")
    private byte[] foto;
    @Column(name = "remunerativo", nullable = false)
    private boolean remunerativo;
    @Basic(optional = false)
    @Column(name = "fecha_alta", nullable = false, insertable = false, updatable = false, columnDefinition = "timestamp with time zone NOT NULL DEFAULT now()")
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date fechaAlta;
    @Column(name = "ultima_compra")
    @Temporal(value = TemporalType.DATE)
    private Date ultimaCompra;
    @JoinColumn(name = "iva", nullable = false)
    @ManyToOne(optional = false, fetch = FetchType.EAGER, cascade = {CascadeType.REFRESH, CascadeType.DETACH})
    private Iva iva;
    @JoinColumn(name = "marca", nullable = false)
    @ManyToOne(optional = false, fetch = FetchType.EAGER, cascade = {CascadeType.REFRESH})
    private Marca marca;
    @JoinColumn(name = "rubro", referencedColumnName = "idrubro", nullable = false)
    @ManyToOne(optional = false)
    private Rubro rubro;
    @JoinColumn(name = "subrubro", referencedColumnName = "idrubro")
    @ManyToOne
    private Rubro subrubro;
    @JoinColumn(name = "idunidadmedida", nullable = false)
    @ManyToOne(optional = false)
    private Unidadmedida idunidadmedida;
    /**
     * Los que son comercializados (se venden), lo demás pueden ser compras de
     * artículos de oficina.
     */
    @Column(name = "bien_de_cambio", nullable = false)
    private boolean bienDeCambio;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "producto")
    private List<Stock> stockList;

    public Producto() {
    }

    public Producto(Integer id) {
        this.id = id;
    }

    @PostPersist
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getStockmaximo() {
        return stockmaximo;
    }

    public void setStockmaximo(int stockmaximo) {
        this.stockmaximo = stockmaximo;
    }

    public int getStockactual() {
        return stockactual;
    }

    public void setStockactual(int stockactual) {
        this.stockactual = stockactual;
    }

    public int getStockminimo() {
        return stockminimo;
    }

    public void setStockminimo(int stockminimo) {
        this.stockminimo = stockminimo;
    }

    public Integer getDeposito() {
        return deposito;
    }

    public void setDeposito(Integer deposito) {
        this.deposito = deposito;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public BigDecimal getCostoCompra() {
        return costoCompra;
    }

    public void setCostoCompra(BigDecimal costoCompra) {
        this.costoCompra = costoCompra;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getPrecioVenta() {
        return precioVenta;
    }

    public void setPrecioVenta(BigDecimal precioVenta) {
        this.precioVenta = precioVenta;
    }

    public byte[] getFoto() {
        return foto;
    }

    public void setFoto(byte[] foto) {
        this.foto = foto;
    }

    public boolean getRemunerativo() {
        return remunerativo;
    }

    public void setRemunerativo(boolean remunerativo) {
        this.remunerativo = remunerativo;
    }

    public Date getFechaAlta() {
        return fechaAlta;
    }

    public void setFechaAlta(Date fechaAlta) {
        this.fechaAlta = fechaAlta;
    }

    public Date getUltimaCompra() {
        return ultimaCompra;
    }

    public void setUltimaCompra(Date ultimaCompra) {
        this.ultimaCompra = ultimaCompra;
    }

    public List<Stock> getStockList() {
        return stockList;
    }

    public void setStockList(List<Stock> stockList) {
        this.stockList = stockList;
    }

    public Iva getIva() {
        return iva;
    }

    public void setIva(Iva iva) {
        this.iva = iva;
    }

    public Marca getMarca() {
        return marca;
    }

    public void setMarca(Marca marca) {
        this.marca = marca;
    }

    public Rubro getRubro() {
        return rubro;
    }

    public void setRubro(Rubro rubro) {
        this.rubro = rubro;
    }

    public Rubro getSubrubro() {
        return subrubro;
    }

    public void setSubrubro(Rubro subrubro) {
        this.subrubro = subrubro;
    }

    public Unidadmedida getIdunidadmedida() {
        return idunidadmedida;
    }

    public void setIdunidadmedida(Unidadmedida idunidadmedida) {
        this.idunidadmedida = idunidadmedida;
    }

    public void setUpdatePrecioVenta(boolean updatePrecioVenta) {
        this.updatePrecioVenta = updatePrecioVenta;
    }

    public boolean getUpdatePrecioVenta() {
        return updatePrecioVenta;
    }

    public boolean isBienDeCambio() {
        return bienDeCambio;
    }

    public void setBienDeCambio(boolean bienDeCambio) {
        this.bienDeCambio = bienDeCambio;
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
        if (!(object instanceof Producto)) {
            return false;
        }
        Producto other = (Producto) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Producto{" + "id=" + id + ", codigo=" + codigo + ", nombre=" + nombre + ", stockmaximo=" + stockmaximo + ", stockactual=" + stockactual + ", stockminimo=" + stockminimo + ", deposito=" + deposito + ", ubicacion=" + ubicacion + ", costoCompra=" + costoCompra + ", descripcion=" + descripcion + ", updatePrecioVenta=" + updatePrecioVenta + ", precioVenta=" + precioVenta + ", remunerativo=" + remunerativo + ", fechaAlta=" + fechaAlta + ", ultimaCompra=" + ultimaCompra + ", iva=" + iva + ", marca=" + marca + ", rubro=" + rubro + ", subrubro=" + subrubro + ", idunidadmedida=" + idunidadmedida + ", bienDeCambio=" + bienDeCambio + '}';
    }

    /**
     * Retorna el mínimo valor (monetario) al cual se podrá vender este. <ol>
     * Por prioridad: <li> {@link Producto#precioVenta} si está setteado.</li>
     * <li> {@link Producto#costoCompra} cuando {@link Producto#precioVenta}
     * <code> == null</code>.</li> </ol>
     *
     * @return precio mínimo.
     */
    public BigDecimal getMinimoPrecioDeVenta() {
        return this.precioVenta != null ? this.precioVenta : this.costoCompra;
    }
}
