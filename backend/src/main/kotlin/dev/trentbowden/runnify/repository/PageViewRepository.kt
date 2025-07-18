package dev.trentbowden.runnify.repository

import dev.trentbowden.runnify.entity.PageView
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PageViewRepository : JpaRepository<PageView, Long> {
    fun findByPageUrl(pageUrl: String): List<PageView>
}
