package com.akameiot.app.data.local.database.entities

//Realizar los imports obligatorios para nuestro Entity
import androidx.room.Entity
import androidx.room.PrimaryKey

//@Entity es la manera de decirle al Room que se creará una tabla
@Entity(tableName = "users")
//Definimos las columnas
data class UserEntity(
    //@PrimaryKey lo usamos para establecer que la siguiente variable debajo de, será nuestra llave primaria, con autogeneracion
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val username: String,
    val password: String, //Solo para la simulacion
    val createdAt: Long = System.currentTimeMillis()
)
