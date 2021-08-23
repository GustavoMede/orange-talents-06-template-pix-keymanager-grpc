package br.com.zupacademy.keymanager.model

import br.com.zupacademy.keymanager.TipoConta
import java.util.*
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.Id

@Entity
data class Chave (
    val chavePix: String,
    val clienteId: String,
    @field:Enumerated(EnumType.STRING) val tipoConta: TipoConta
        ){

    @Id
    var pixId: String = UUID.randomUUID().toString()

}