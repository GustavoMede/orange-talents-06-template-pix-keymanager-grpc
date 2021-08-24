package br.com.zupacademy.keymanager.extensions

import br.com.zupacademy.keymanager.CadastroChaveRequest
import br.com.zupacademy.keymanager.TipoChave
import br.com.zupacademy.keymanager.clients.dto.BCBCadastroRequest
import br.com.zupacademy.keymanager.model.BankAccount
import br.com.zupacademy.keymanager.model.Owner

fun novaChaveBCBRequest(
    bankAccount: BankAccount,
    owner: Owner, request: CadastroChaveRequest
): BCBCadastroRequest {
    return when (request.tipoChave) {
        TipoChave.ALEATORIA -> BCBCadastroRequest(
            "RANDOM", "", bankAccount, owner
        )
        else -> BCBCadastroRequest(
            request.tipoChave.name, request.valorTipo, bankAccount, owner
        )
    }
}