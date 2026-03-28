package com.alfoll.service

import com.alfoll.database.repository.AiRepository
import com.alfoll.exception.RecordNotFoundException
import com.alfoll.model.RewriteRequestDTO
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

class AiServiceImplTest {
    private val repo = mockk<AiRepository>()
    private val service = AiServiceImpl(
        aiRepository = repo,
        aiUrl = "https://openrouter.ai/api/v1/chat/completions",
        apiKey = "test_api_key",
        model = "openrouter/free"
    )

    @Test
    fun `rewrite with blank text throws IllegalArgumentException` () {
        val ex = assertThrows(IllegalArgumentException::class.java) {
            runBlocking{
                service.rewrite(RewriteRequestDTO("  "))
            }
        }
        assertEquals("Text cannot be empty", ex.message)
    }

    @Test
    fun `getRecord with id under 0 throws IllegalArgumentException`() {
        val ex = assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                service.getRecord(-10)
            }
        }
        assertEquals("Id must be positive", ex.message)
    }

    @Test
    fun `deleteRecord throws RecordNotFoundException when AiRepository returns false`() {
        runBlocking {
            coEvery { repo.deleteById(1) } returns false

            val ex = assertFailsWith<RecordNotFoundException> {
                service.deleteRecord(1)
            }
            assertEquals("Record not found", ex.message)
        }
    }

    @Test
    fun `getRecord throws RecordNotFoundException when AiRepository returns null`() {
        runBlocking {
            coEvery { repo.getById(2) } returns null

            val ex = assertFailsWith<RecordNotFoundException> {
                service.getRecord(2)
            }
            assertEquals("Record not found", ex.message)
        }
    }
}