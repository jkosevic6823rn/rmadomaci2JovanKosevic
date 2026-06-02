package rs.edu.raf.rma.showtime.profile.di

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import rs.edu.raf.rma.showtime.profile.ProfileViewModel

val showtimeProfileModule = module {
    viewModelOf(::ProfileViewModel)
}
