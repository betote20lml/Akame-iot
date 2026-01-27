package com.akameiot.app.ui.login

//Importar el ViewModel
import androidx.lifecycle.ViewModel

//Import StateFlow para exportar estados
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


class LoginViewModel : ViewModel() {

    //Crear StateFlows privados y sus expuestos
    //las variables privadas se modifican solo dentro del VM
    private val _user = MutableStateFlow("")
    //las variables "expuestas" las lee nuestra UI
    val user: StateFlow<String> = _user

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    private val _rememberUser = MutableStateFlow(false)
    val rememberUser: StateFlow<Boolean> = _rememberUser

    //Creamos los estados para los errores de los inputs nulls
    private val _userError = MutableStateFlow<String?>(null)
    val userError: StateFlow<String?> = _userError

    private val _passError = MutableStateFlow<String?>(null)
    val passError: StateFlow<String?> = _passError

    //Crear funciones para manejar eventos (event handlers)
    //La UI no debe hacer user = it. En vez de eso, la UI le dice al VM:“cambió el user”.
    fun onUserChange(value: String)
    {
        //Actualiza valor de variable "_user"
        _user.value = value
        //Limpia _userError
        if (_userError.value != null) _userError.value = null
    }


    fun onPasswordChange(value: String)
    {
        _password.value = value
        if (_passError.value != null) _passError.value = null
    }

    //Manejar el toggle del checkbox recordar usuario
    fun toggleRememberUser()
    {
        _rememberUser.value = !_rememberUser.value
    }

    //Funcion para validar datos que el usuario mande
    fun validate(): Boolean{
        val u = _user.value.trim()
        val p = _password.value

        _userError.value = if (u.isEmpty()) "El usuario es obligatorio" else null
        _passError.value = when {
            p.isEmpty() -> "La contraseña es obligatoria"
            p.length < 6 -> "Minimo 6 caracteres"
            else -> null
        }

        return _userError.value==null && _passError.value == null
    }

    //Enum class nos permite tener una serie de variables predefinidas
    enum class LoginField{
        USER,
        PASSWORD
    }

    fun firstErrorField(): LoginField? {
        return when {
            _userError.value != null -> LoginField.USER
            _passError.value != null -> LoginField.PASSWORD
            else -> null
        }
    }

}
