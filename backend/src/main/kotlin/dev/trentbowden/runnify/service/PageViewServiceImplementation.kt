package dev.trentbowden.runnify.service

import dev.trentbowden.runnify.entity.PageView
import dev.trentbowden.runnify.repository.PageViewRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class PageViewServiceImplementation(private val pageViewRepository: PageViewRepository) :
        PageViewService {
    override fun savePageView(pageView: PageView): PageView {
        return pageViewRepository.save(pageView)
    }

    override fun getAllPageViews(): List<PageView> {
        return pageViewRepository.findAll()
    }

    override fun getPageViewsByUrl(pageUrl: String): List<PageView> {
        return pageViewRepository.findByPageUrl(pageUrl)
    }
}
