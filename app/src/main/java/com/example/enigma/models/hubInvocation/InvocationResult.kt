package com.example.enigma.models.hubInvocation

import com.example.enigma.models.PendingMessage

open class InvocationResult<T>(
    val data: T? = null,
    val success: Boolean? = null,
    val errors: List<Error>? = null
) {
    fun errorsToString(): String?
    {
        return errors?.joinToString(limit = 3)
    }
}

class GenerateTokenResult(
    data: String? = null,
    success: Boolean? = null,
    errors: List<Error>? = null
) : InvocationResult<String>(data, success, errors)

class AuthenticateResult(
    data: Boolean? = null,
    success: Boolean? = null,
    errors: List<Error>? = null
) : InvocationResult<Boolean>(data, success, errors)

class VertexBroadcastResult(
    data: Boolean? = null,
    success: Boolean? = null,
    errors: List<Error>? = null
) : InvocationResult<Boolean>(data, success, errors)

class PullResult(
    data: List<PendingMessage>? = null,
    success: Boolean? = null,
    errors: List<Error>? = null
) : InvocationResult<List<PendingMessage>>(data, success, errors)

class RouteResult(
    data: Boolean? = null,
    success: Boolean? = null,
    errors: List<Error>? = null
) : InvocationResult<Boolean>(data, success, errors)

class CleanupResult(
    data: Boolean? = null,
    success: Boolean? = null,
    errors: List<Error>? = null
) : InvocationResult<Boolean>(data, success, errors)
