package br.com.zupacademy.keymanager.chave

import br.com.zupacademy.keymanager.KeymanagerRemoveServiceGrpc
import br.com.zupacademy.keymanager.RemoveRequest
import br.com.zupacademy.keymanager.RemoveResponse
import br.com.zupacademy.keymanager.clients.BCBClient
import br.com.zupacademy.keymanager.extensions.removeChaveBCBRequest
import br.com.zupacademy.keymanager.repository.ChaveRepository
import br.com.zupacademy.keymanager.validator.RemoveChaveRequestValidator
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.stub.StreamObserver
import io.micronaut.http.client.exceptions.HttpClientResponseException
import javax.inject.Singleton

@Singleton
class KeymanagerRemoveGrpcService(private val bcbClient: BCBClient, private val chaveRepository: ChaveRepository) :
    KeymanagerRemoveServiceGrpc.KeymanagerRemoveServiceImplBase() {

    override fun removeChave(request: RemoveRequest?, responseObserver: StreamObserver<RemoveResponse>?) {

        if (request!!.allFields.isNotEmpty()) {

            val removeChaveRequestValidator = RemoveChaveRequestValidator(chaveRepository)

            try {
                removeChaveRequestValidator.validaRequest(request)
            } catch (e: StatusRuntimeException) {
                responseObserver?.onError(e)
                responseObserver!!.onCompleted()
                return
            }

            try {
                val chaveEncontrada = chaveRepository.findById(request.key)

                chaveEncontrada.isPresent.let {
                    try {
                        bcbClient.removeChaveBCB(
                            request.key ,removeChaveBCBRequest(request)
                        ).let {
                            chaveRepository.delete(chaveEncontrada.get())
                            responseObserver?.onNext(
                                RemoveResponse.newBuilder()
                                    .setRemovido(true)
                                    .build()
                            )
                            responseObserver?.onCompleted()
                            return
                        }
                    } catch (e: HttpClientResponseException) {
                        when (e.status.code) {
                            422 -> {
                                responseObserver?.onError(
                                    Status.ALREADY_EXISTS
                                        .withDescription(e.message)
                                        .asRuntimeException()
                                )
                                responseObserver?.onCompleted()
                                return
                            }
                            403 -> {
                                responseObserver?.onError(
                                    Status.PERMISSION_DENIED
                                        .withDescription(e.message)
                                        .asRuntimeException()
                                )
                                responseObserver?.onCompleted()
                                return
                            }
                            404 -> {
                                responseObserver?.onError(
                                    Status.NOT_FOUND
                                        .withDescription(e.message)
                                        .asRuntimeException()
                                )
                                responseObserver?.onCompleted()
                                return
                            }
                            else -> responseObserver?.onError(
                                Status.UNKNOWN
                                    .withDescription("Um erro inesperado ocorreu.")
                                    .asRuntimeException()
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                responseObserver?.onError(e)
                responseObserver?.onCompleted()
                return
            }
        }
    }
}