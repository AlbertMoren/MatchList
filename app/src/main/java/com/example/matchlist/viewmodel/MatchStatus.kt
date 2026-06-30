package com.example.matchlist.viewmodel

// necessario para habilitar traducao dos estados de carregamento da matchviewmodel
sealed class MatchStatus {
    object Loading   : MatchStatus()
    object Empty     : MatchStatus()
    object Ready     : MatchStatus()
    data class Error(val message: String) : MatchStatus()
}