package com.example.enigma.models.hubInvocation

open class InvocationResult<T>(
    val data: T? = null,
    val success: Boolean? = null,
    val errors: List<Error>? = null
)

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
