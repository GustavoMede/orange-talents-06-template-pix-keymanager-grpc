package br.com.zupacademy.keymanager.clients.dto

import br.com.zupacademy.keymanager.model.BankAccount
import br.com.zupacademy.keymanager.model.Owner
import java.time.LocalDateTime

class BCBCadastroResponse(
    val keyType: String,
    val key: String,
    val bankAccount: BankAccount,
    val owner: Owner
) {
}

class RemocaoResponse(
    val key: String,
    val participant: String,
    val deletedAt: LocalDateTime
) {
}