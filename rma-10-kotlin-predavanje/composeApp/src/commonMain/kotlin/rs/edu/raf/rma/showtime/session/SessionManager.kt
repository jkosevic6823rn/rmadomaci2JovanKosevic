package rs.edu.raf.rma.showtime.session

import rs.edu.raf.rma.core.auth.AuthStore
import rs.edu.raf.rma.showtime.library.data.db.LibraryDao
import rs.edu.raf.rma.showtime.quiz.data.db.QuizDao

/**
 * Centralna odjava. Briše token i SVE lokalne korisničke podatke (favorites,
 * watchlist, quiz rezultati), ali ČUVA globalne filmove (katalog, quiz pool)
 * koji nisu vezani za korisnika.
 */
class SessionManager(
    private val authStore: AuthStore,
    private val libraryDao: LibraryDao,
    private val quizDao: QuizDao,
) {
    suspend fun logout() {
        libraryDao.clearUserData()
        quizDao.clearResults()
        authStore.clearAuthData()
    }
}
