package com.juandroiddev.chirp.infra.database.repositories

import com.juandroiddev.chirp.domain.type.UserId
import com.juandroiddev.chirp.infra.database.entities.ChatParticipantEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface ChatParticipantRepository : JpaRepository<ChatParticipantEntity, UserId> {
    fun findByUserIdIn(userIds: Set<UserId>):Set<ChatParticipantEntity>
    @Query(
        """
           SELECT p
           FROM ChatParticipantEntity p
           WHERE LOWER(p.email) = :query OR LOWER(p.username) = :query
        """
    )

    fun findByEmailOrUsername(query:String): ChatParticipantEntity?

}