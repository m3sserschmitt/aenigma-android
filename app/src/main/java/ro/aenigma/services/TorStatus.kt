package ro.aenigma.services

sealed class TorStatus {
    object Idle : TorStatus()
    object Starting : TorStatus()
    object On : TorStatus()
    object Off : TorStatus()
    object Timeout : TorStatus()
}
