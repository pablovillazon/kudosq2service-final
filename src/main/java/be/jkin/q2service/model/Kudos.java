package be.jkin.q2service.model;

import com.datastax.driver.core.DataType;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.Date;
import java.util.UUID;


@Table("kudos")
public class Kudos {

    @PrimaryKeyColumn(name="id",ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    @CassandraType(type = DataType.Name.UUID, userTypeName = "user_type")
    private UUID id;

    @CassandraType(type = DataType.Name.VARCHAR, userTypeName = "user_type")
    private String fuente;

    @CassandraType(type = DataType.Name.VARCHAR, userTypeName = "user_type")
    private String destino;

    @CassandraType(type = DataType.Name.VARCHAR, userTypeName = "user_type")
    private KudosTema tema;

    @CassandraType(type = DataType.Name.TIMESTAMP, userTypeName = "user_type")
    private Date fecha;

    @CassandraType(type = DataType.Name.VARCHAR, userTypeName = "user_type")
    private String lugar;

    @CassandraType(type = DataType.Name.VARCHAR, userTypeName = "user_type")
    private String texto;

    public enum KudosTema{
        THANKS,
        WELL_DONE,
        GREAT_JOB,
        CHALA,
        GENERICO
    }

    //Constructors
    public Kudos(){}

    public Kudos(UUID id, String fuente, String destino, KudosTema tema, Date fecha, String lugar, String texto)
    {
        this.id = id;
        this.fuente = fuente;
        this.destino = destino;
        this.tema = tema;
        this.fecha = fecha;
        this.lugar = lugar;
        this.texto = texto;
    }

    public void setId(UUID _id)
    {
        this.id = _id;
    }

    public String getId()
    {
        return this.id.toString();
    }

    public String getFuente() {
        return fuente;
    }

    public void setFuente(String fuente) {
        this.fuente = fuente;
    }

    public String getDestino() {
        return destino;
    }

    public void setDestino(String destino) {
        this.destino = destino;
    }

    public KudosTema getTema() {
        return tema;
    }

    public void setTema(KudosTema tema) {
        this.tema = tema;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public String getLugar() {
        return lugar;
    }

    public void setLugar(String lugar) {
        this.lugar = lugar;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }


}
