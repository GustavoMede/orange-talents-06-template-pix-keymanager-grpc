package br.com.zupacademy.keymanager.validator

import br.com.zupacademy.keymanager.RemoveRequest
import br.com.zupacademy.keymanager.repository.ChaveRepository
import io.grpc.Status

class RemoveChaveRequestValidator(private val chaveRepository: ChaveRepository) {

    fun validaRequest(request: RemoveRequest) {

        if (request.participant.isBlank()) {
            throw Status.INVALID_ARGUMENT
                .withDescription("O isbp do banco deve ser informado.")
                .asRuntimeException()
        }

        if (request.key.isBlank()) {
            throw Status.INVALID_ARGUMENT
                .withDescription("O ID da chave PIX deve ser informado.")
                .asRuntimeException()
        }

        if(!chaveRepository.existsByPixIdAndClienteId(request.key, request.clientId)){
            throw Status.NOT_FOUND
                .withDescription("Chave PIX já removida ou inexistente.")
                .asRuntimeException()
        }
    }
}