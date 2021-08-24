package br.com.zupacademy.keymanager.clients.dto

import br.com.zupacademy.keymanager.model.Titular

data class ITAUResponse(
    val tipo: String,
    val agencia: String,
    val numero: String,
    val titular: Titular,
)