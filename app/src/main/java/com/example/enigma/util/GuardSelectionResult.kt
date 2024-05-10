package com.example.enigma.util

import com.example.enigma.models.Vertex

open class GuardSelectionResult(val chosenGuard: Vertex?) {

    class Error: GuardSelectionResult(null)

    class NoGuardAvailable: GuardSelectionResult(null)

    class SelectionNotRequired: GuardSelectionResult(null)

    class Success(chosenGuard: Vertex): GuardSelectionResult(chosenGuard)
}
