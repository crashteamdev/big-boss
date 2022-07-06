package dev.crashteam.bigboss.repository

import dev.crashteam.bigboss.repository.entity.ProductEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface ProductRepository : JpaRepository<ProductEntity, UUID>
