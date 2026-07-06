/*
    Aenigma - Private Messaging
    Client Android mobile application for Aenigma - Federated messaging system
    Copyright © 2025-2026 Romulus-Emanuel Ruja <romulus-emanuel.ruja@tutanota.com>

    This file is part of Aenigma project.

    Aenigma is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Aenigma is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Aenigma.  If not, see <https://www.gnu.org/licenses/>.
*/

package ro.aenigma.models.hubInvocation

import ro.aenigma.models.PendingMessageDto

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

class PullResult(
    data: List<PendingMessageDto>? = null,
    success: Boolean? = null,
    errors: List<Error>? = null
) : InvocationResult<List<PendingMessageDto>>(data, success, errors)

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
