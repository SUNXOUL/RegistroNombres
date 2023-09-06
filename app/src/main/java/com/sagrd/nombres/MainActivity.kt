package com.sagrd.nombres

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import com.sagrd.nombres.ui.theme.NombresTheme
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    lateinit var personaDB: PersonaDB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NombresTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RegistroNombreScreen(hiltViewModel())
                }
            }
        }
    }
}
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
    @Composable
    fun RegistroNombreScreen(
        viewModel: PersonaViewModel
    )
    {
        var nombre : String by remember {
            mutableStateOf("")
        }
        val conjuntoPersonas by viewModel.Personas.collectAsStateWithLifecycle()
        val keyboardController = LocalSoftwareKeyboardController.current

        Scaffold(
            topBar = { TopAppBar(title = { Text(text = "Registro de Nombres") }, modifier = Modifier.shadow(8.dp))},
            content = ({
                Column(modifier = Modifier
                    .padding(top = 80.dp, end = 16.dp, start = 16.dp)
                    .fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally ) {
                    OutlinedTextField(
                        value = viewModel.nombre,
                        onValueChange = { viewModel.nombre = it },
                        label = { Text("Ingrese el nombre") },
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                        modifier=Modifier.fillMaxWidth()
                    )
                    OutlinedButton(onClick = {
                        keyboardController?.hide()
                        viewModel.savePersona()
                        viewModel.setMessageShown()
                                             }, modifier=Modifier.fillMaxWidth()) {
                        Row {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription ="Guardar", tint = Color.Green )
                            Text(text = "Guardar", color = Color.Green)
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Divider()
                    LazyColumn(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.Start)
                    {
                        items(conjuntoPersonas){ persona ->
                            Card (modifier = Modifier.padding(5.dp)){
                                Text(text = persona.nombre, modifier = Modifier.padding(5.dp))
                            }
                        }
                    }

                }
            })
        )
    }
//viewModel de personas
@HiltViewModel
class PersonaViewModel @Inject constructor(
    private val personaDB: PersonaDB
) : ViewModel(){

    var nombre : String by mutableStateOf("")

    private val _isMessageShown = MutableSharedFlow<Boolean>()
    val isMessageShownFlow = _isMessageShown.asSharedFlow()

    fun setMessageShown() {
        viewModelScope.launch {
            _isMessageShown.emit(true)
        }
    }

    val Personas: StateFlow<List<Persona>> = personaDB.personaDao().getAll()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun savePersona() {
        viewModelScope.launch {
            val persona = Persona(
                nombre = nombre
            )
            personaDB.personaDao().Save(persona)
            limpiar()
        }
    }

    fun limpiar(){
        nombre=""
    }
}

//Clase para el nombres en este ejemplo le colocare el nombre de persona
@Entity(tableName = "Personas")
data class Persona(
    @PrimaryKey
    val personaId : Int?=null,
    var nombre:String
)

//DAO de la clase persona
@Dao
interface personaDao{

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun Save(persona: Persona)

    @Query("Select * from Personas")
    fun getAll(): Flow<List<Persona>>
}

//Base de Datos con los nombres de las personas
@Database(
    entities = [Persona::class],
    version = 2
)
abstract class PersonaDB : RoomDatabase(){
    public abstract fun personaDao() : personaDao
}