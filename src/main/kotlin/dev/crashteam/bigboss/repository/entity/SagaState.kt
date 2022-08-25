package dev.crashteam.bigboss.repository.entity

enum class SagaState {
    commit,
    rollback,
    in_progress
}
