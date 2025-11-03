package com.juandroiddev.chirp.infra.database

import com.juandroiddev.chirp.domain.type.UserId
import com.juandroiddev.chirp.infra.database.entities.ChatParticipantEntity
import org.springframework.data.jpa.repository.Query

interface ChatParticipantRepository {
    fun findByUserIdIn(userIds:List<UserId>):Set<ChatParticipantEntity>
    @Query(
        """
           SELECT p
           FROM ChatParticipantEntity p
           WHERE LOWER(p.email) = :query OR LOWER(p.username) = :query
        """
    )

    fun findByEmailOrUsername(query:String):ChatParticipantEntity?

}