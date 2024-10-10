package org.example

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import org.hibernate.SessionFactory
import org.hibernate.annotations.Type
import org.hibernate.cfg.AvailableSettings.JAKARTA_JDBC_DRIVER
import org.hibernate.cfg.AvailableSettings.JAKARTA_JDBC_PASSWORD
import org.hibernate.cfg.AvailableSettings.JAKARTA_JDBC_URL
import org.hibernate.cfg.AvailableSettings.JAKARTA_JDBC_USER
import org.hibernate.cfg.Configuration
import org.hibernate.engine.spi.SharedSessionContractImplementor
import org.hibernate.usertype.UserType
import org.testcontainers.containers.PostgreSQLContainer
import java.io.Serializable
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Types

fun main() {
    val postgresContainer = PostgreSQLContainer<Nothing>("postgres:latest")
        .apply {
            withDatabaseName("testdb")
            withUsername("testuser")
            withPassword("testpass")
            withExposedPorts(5432)
        }
    postgresContainer.start()
    Thread.sleep(1000) // wait for psql container to start
    println("Hibernate Bootstrapping example")
    val sessionFactory: SessionFactory = initHibernate(postgresContainer)
    postgresContainer.stop()
    println("Works?")
}

fun initHibernate(container: PostgreSQLContainer<Nothing>) : SessionFactory{
    return Configuration()
        .addAnnotatedClass(MyEntity::class.java)
        .setProperty(JAKARTA_JDBC_URL, "jdbc:postgresql://localhost:${container.getMappedPort(5432)}/testdb")
        .setProperty(JAKARTA_JDBC_USER, "testuser")
        .setProperty(JAKARTA_JDBC_PASSWORD, "testpass")
        .setProperty(JAKARTA_JDBC_DRIVER, "org.postgresql.Driver")
        .buildSessionFactory()
}

@Entity
class MyEntity (
    @Type(value = ColorType::class)
    @Column(columnDefinition = "color[]")
    val colors: MutableList<Color> = mutableListOf(),
    // if you comment field above, everything works as intended
) {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    var id: Long? = null
}

class ColorType : UserType<List<Color>>{
    override fun equals(x: List<Color>?, y: List<Color>?): Boolean =
        if (x is List<Color> && y is List<Color>) {
            x == y
        } else {
            false
        }

    override fun hashCode(x: List<Color>?): Int {
        if (x == null) return 0

        var hashCode = 1
        for (element in x) {
            hashCode = 31 * hashCode + (element.hashCode())
        }

        return hashCode
    }

    override fun getSqlType(): Int = Types.ARRAY

    override fun returnedClass(): Class<List<Color>> = List::class.java as Class<List<Color>>

    override fun nullSafeGet(rs: ResultSet, position: Int, session: SharedSessionContractImplementor?, owner: Any?): List<Color>? {
        val array = rs.getObject(position) as? Array<String> ?: return null
        val listOfBrands = array.toList()
        return listOfBrands.map { Color.valueOf(it) }
    }

    override fun isMutable(): Boolean = false

    override fun assemble(cached: Serializable?, owner: Any?): List<Color> =
        throw UnsupportedOperationException("Unable to assemble cached data")

    override fun disassemble(value: List<Color>?): Serializable {
        if (value != null) {
            return ArrayList(value)
        } else {
            throw UnsupportedOperationException("Unable to disassemble null value")
        }
    }

    override fun deepCopy(value: List<Color>?): List<Color> {
        if (value != null) {
            return ArrayList(value)
        } else {
            throw UnsupportedOperationException("Unable to deep copy null value")
        }
    }

    override fun nullSafeSet(st: PreparedStatement?, value: List<Color>?, index: Int, session: SharedSessionContractImplementor?) {
        if (value == null) {
            st!!.setNull(index, Types.ARRAY)
        } else {
            val array = value.toTypedArray()
            st!!.setObject(index, array)
        }
    }
}

enum class Color{
    RED,
    GREEN,
    BLUE
}