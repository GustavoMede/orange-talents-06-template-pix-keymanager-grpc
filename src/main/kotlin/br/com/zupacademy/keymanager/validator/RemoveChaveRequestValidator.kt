package br.com.zupacademy.keymanager.validator

import br.com.zupacademy.keymanager.RemoveRequest
import br.com.zupacademy.keymanager.repository.ChaveRepository
import io.grpc.Status

class RemoveChaveRequestValidator(private val chaveRepository: ChaveRepository) {

    fun validaRequest(request: RemoveRequest) {

        if (request.clienteId.isBlank()) {
            throw Status.INVALID_ARGUMENT
                .withDescription("O identificador do cliente deve ser informado.")
                .asRuntimeException()
        }

        if (request.pixId.isBlank()) {
            throw Status.INVALID_ARGUMENT
                .withDescription("O ID da chave PIX deve ser informado.")
                .asRuntimeException()
        }

        if(!chaveRepository.existsByPixIdAndClienteId(request.pixId, request.clienteId)){
            throw Status.NOT_FOUND
                .withDescription("Chave PIX já removida ou inexistente.")
                .asRuntimeException()
        }
    }
}