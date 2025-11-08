package com.edufelip.shared.di

import com.edufelip.shared.domain.usecase.NoteUseCases
import com.edufelip.shared.domain.usecase.buildNoteUseCases
import com.edufelip.shared.domain.validation.NoteValidationRules
import com.edufelip.shared.ui.vm.DefaultNoteUiViewModel
import com.edufelip.shared.ui.vm.NoteUiViewModel
import org.koin.core.Koin
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
import kotlin.native.concurrent.ThreadLocal

private val coreModule = module {
    single<NoteUseCases> { buildNoteUseCases(get(), NoteValidationRules()) }
    single<NoteUiViewModel> { DefaultNoteUiViewModel(get()) }
}

expect fun platformModule(): Module

@ThreadLocal
object SharedKoin {
    private var koinApplication: KoinApplication? = null

    fun init(appDeclaration: KoinAppDeclaration? = null): KoinApplication {
        val current = koinApplication
        if (current != null) return current
        val app = startKoin {
            appDeclaration?.invoke(this)
            modules(coreModule, platformModule())
        }
        koinApplication = app
        return app
    }

    fun koin(): Koin = (koinApplication ?: init()).koin
}

fun initKoin(appDeclaration: KoinAppDeclaration? = null): KoinApplication = SharedKoin.init(appDeclaration)

fun getSharedKoin(): Koin = SharedKoin.koin()
