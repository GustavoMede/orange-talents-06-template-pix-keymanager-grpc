package br.com.zupacademy.keymanager.validator

import br.com.zupacademy.keymanager.CadastroChaveRequest
import br.com.zupacademy.keymanager.TipoChave
import br.com.zupacademy.keymanager.repository.ChaveRepository
import io.grpc.Status
import io.grpc.StatusRuntimeException

class CadastraChaveRequestValidator(private val chaveRepository: ChaveRepository) {

    fun validaRequest(request: CadastroChaveRequest) {
        if (request.tipoChave == null || request.tipoChave.name.isBlank()) {
            throw Status.INVALID_ARGUMENT
                .withDescription("O tipo da chave deve ser informado.")
                .asRuntimeException()
        }

        if (request.tipoConta == null || request.tipoConta.name.isBlank()) {
            throw Status.INVALID_ARGUMENT
                .withDescription("O tipo da conta deve ser informado.")
                .asRuntimeException()

        }

        if (request.valorTipo.length > 77) {
            throw Status.INVALID_ARGUMENT
                .withDescription("Por favor, insira uma chave com até 77 caracteres.")
                .asRuntimeException()

        }

        if (request.tipoChave == TipoChave.CPF && !(request.valorTipo.matches("^[0-9]{11}$".toRegex()))) {
            throw Status.INVALID_ARGUMENT
                .withDescription("CPF em branco ou inválido.")
                .augmentDescription("Formado esperado 111.222.333-44")
                .asRuntimeException()

        }

        if (request.tipoChave == TipoChave.EMAIL && !(request.valorTipo.matches("(?:[a-z0-9!#\$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#\$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])".toRegex()))) {
            throw Status.INVALID_ARGUMENT
                .withDescription("Email em branco ou inválido.")
                .augmentDescription("Formato esperado example@example.com")
                .asRuntimeException()

        }

        if (request.tipoChave == TipoChave.TELEFONE && !(request.valorTipo.matches("^\\+[1-9][0-9]\\d{1,14}\$".toRegex()))) {
            throw Status.INVALID_ARGUMENT
                .withDescription("Telefone em branco ou inválido.")
                .augmentDescription("Formato esperado +5585988714077")
                .asRuntimeException()

        }

        if (chaveRepository.existsByChavePix(request.valorTipo)) {
            throw Status.ALREADY_EXISTS
                .withDescription("Chave já cadastrada.")
                .asRuntimeException()
        }
    }
}