package rs.edu.raf.rma.showtime.profile

interface ProfileContract {
    data class UiState(
        val isLoading: Boolean = true,
        val fullName: String = "",
        val username: String = "",
        val bestScore: Double? = null,
        val gamesPlayed: Int = 0,
        val favoritesCount: Int = 0,
        val watchlistCount: Int = 0,
        val error: String? = null,
    )

    sealed interface UiEvent {
        data object Retry : UiEvent
        data object Logout : UiEvent
    }
}
