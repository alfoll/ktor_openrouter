package com.alfoll.database.repository

import com.alfoll.database.table.AiHistory
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AiRepositoryImplTest {
    private lateinit var db: Database
    private lateinit var repo: AiRepository

    @BeforeEach
    fun createDB() {
        val jdbcUrl = "jdbc:sqlite:data/test/testDB.db"
        val driver = "org.sqlite.JDBC"

        db = Database.connect(
            url = jdbcUrl,
            driver = driver,
        )

        transaction(db) {
            SchemaUtils.create(AiHistory)
        }

        repo = AiRepositoryImpl(db)
    }

    @AfterEach
    fun closeDB(){
        transaction(db) {
            SchemaUtils.drop(AiHistory)
        }
    }

    @Test
    fun `create saves record and gerById returns it`() = runBlocking {
        val record = repo.create("shatla", "alfoll", "2026-03-28T10:10:10")
        val ans = repo.getById(record.id)

        assertAll(
            { assertNotNull(ans) },
            { assertTrue(record.id > 0) },

            { assertEquals("shatla", ans?.originalText) },
            { assertEquals("alfoll", ans?.aiResponse) },
            { assertEquals("2026-03-28T10:10:10", ans?.createdAt) }
        )
    }

    @Test
    fun `getAll returns all records`() = runBlocking {
        repo.create("text 1", "ans 1", "2026-03-28T10:10:10")
        repo.create("text 2", "ans 2", "2026-03-28T01:01:01")

        val all = repo.getAll()

        assertAll(
            { assertEquals(2, all.size) },

            { assertTrue(all.first().originalText == "text 1") },
            { assertTrue(all.last().originalText == "text 2") },

            { assertEquals("ans 1", all.first().aiResponse) },
            { assertEquals("ans 2", all.last().aiResponse) },

            { assertTrue(all.any { it.createdAt == "2026-03-28T10:10:10"}) },
            { assertTrue(all.any { it.createdAt == "2026-03-28T01:01:01"}) }
        )
    }

    @Test
    fun `getById returns null when record does not exists`() = runBlocking {
        val record = repo.getById(1000)

        assertNull(record)
    }

    @Test
    fun `deleteById returns false when record does not exist`() = runBlocking {
        val deleted = repo.deleteById(1000)

        assertFalse(deleted)
    }

    @Test
    fun `deleteById delete record`() = runBlocking {
        val record = repo.create("text", "ans", "2026-03-28T10:10:10")
        val deleted = repo.deleteById(record.id)
        val foundDel = repo.getById(record.id)

        assertAll(
            { assertTrue(deleted) },
            { assertNull(foundDel) }
        )

    }
}