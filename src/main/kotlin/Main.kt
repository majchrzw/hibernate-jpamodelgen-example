package org.example

import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import org.hibernate.SessionFactory
import org.hibernate.cfg.AvailableSettings.JAKARTA_JDBC_DRIVER
import org.hibernate.cfg.AvailableSettings.JAKARTA_JDBC_PASSWORD
import org.hibernate.cfg.AvailableSettings.JAKARTA_JDBC_URL
import org.hibernate.cfg.AvailableSettings.JAKARTA_JDBC_USER
import org.hibernate.cfg.Configuration
import org.testcontainers.containers.PostgreSQLContainer

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
    println("Hibernate Jpamodelgen reproducer")
    val sessionFactory: SessionFactory = initHibernate(postgresContainer)
    postgresContainer.stop()
    println(SecondEntity_.FIRST)
}

fun initHibernate(container: PostgreSQLContainer<Nothing>) : SessionFactory{
    return Configuration()
        .addAnnotatedClass(MyEntity::class.java)
        .addAnnotatedClass(SecondEntity::class.java)
        .setProperty(JAKARTA_JDBC_URL, "jdbc:postgresql://localhost:${container.getMappedPort(5432)}/testdb")
        .setProperty(JAKARTA_JDBC_USER, "testuser")
        .setProperty(JAKARTA_JDBC_PASSWORD, "testpass")
        .setProperty(JAKARTA_JDBC_DRIVER, "org.postgresql.Driver")
        .buildSessionFactory()
}

@Entity
class MyEntity (
) {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    var id: Long? = null
    @OneToMany(mappedBy = "first")
    val second: MutableList<SecondEntity> = mutableListOf()
}

@Entity
class SecondEntity (
    val name: String,
    @ManyToOne(fetch = FetchType.LAZY)
    val first: MyEntity? = null
) {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    var id: Long? = null
}