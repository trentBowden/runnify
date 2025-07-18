package dev.trentbowden.runnify.service

import dev.trentbowden.runnify.entity.PageView

interface PageViewService {
    fun savePageView(pageView: PageView): PageView
    fun getAllPageViews(): List<PageView>
    fun getPageViewsByUrl(pageUrl: String): List<PageView>
}
