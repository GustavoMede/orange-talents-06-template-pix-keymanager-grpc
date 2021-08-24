package br.com.zupacademy.keymanager.clients.dto

import br.com.zupacademy.keymanager.model.BankAccount
import br.com.zupacademy.keymanager.model.Owner

class BCBCadastroRequest(
    val keyType: String,
    val key: String,
    val bankAccount: BankAccount,
    val owner: Owner,
) {
}

class BCBRemocaoRequest(
    val key: String,
    val participant: String
) {
}