package dev.trentbowden.runnify.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "page_views")
public class PageView(
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null,
        var pageUrl: String? = null,
        var ipAddress: String? = null,
        var timestamp: LocalDateTime = LocalDateTime.now()
)
