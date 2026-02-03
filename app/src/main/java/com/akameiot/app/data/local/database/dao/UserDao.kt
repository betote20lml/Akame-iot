package com.akameiot.app.data.local.database.dao

//Imports requeridos para el uso de nuestro DAO
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
//Importamos nuestra entity de Users
import com.akameiot.app.data.local.database.entities.UserEntity

@Dao //Le dice al room que esto controla una tabla
interface UserDao{
//Interface hace que room genere la implementacion

    //Se crea la funcion para poder insertaer "Usuarios"
    @Insert(onConflict = OnConflictStrategy.REPLACE) //REPLACE si existe el mismo lo sobreescribe
    suspend fun insertUser(user: UserEntity) //obligatorio para que funcione solo en el caso que debe de ser

    //Armamos nuestro Query para la consulta de credenciales
    @Query("""
        SELECT * FROM users
        WHERE username = :username AND password = :password
        LIMIT 1
    """)
    suspend fun login(
        username: String,
        password: String
    ): UserEntity?
}
