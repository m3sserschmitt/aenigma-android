package ro.aenigma.util

import ro.aenigma.models.Vertex

open class GuardSelectionResult(val chosenGuard: Vertex?) {

    class Error: GuardSelectionResult(null)

    class NoGuardAvailable: GuardSelectionResult(null)

    class SelectionNotRequired(currentGuard: Vertex): GuardSelectionResult(currentGuard)

    class Success(chosenGuard: Vertex): GuardSelectionResult(chosenGuard)
}
