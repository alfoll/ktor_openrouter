package com.alfoll

import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals

class RoutingTest {

    @Test
    fun `rewrite on blank text returns BadRequest 400`() = testApplication {
        environment {
            config = MapApplicationConfig(
            "storage.url" to "jdbc:sqlite:data/test/testDB.db",
            "storage.driver" to "org.sqlite.JDBC",

            "open_router.api_key" to "test_api_key",
            "open_router.base_url" to "https://openrouter.ai/api/v1/chat/completions",
            "open_router.model" to "openrouter/free",
            )
        }
        application {
            module()
        }
        client.post("/ai/rewrite") {
            contentType(ContentType.Application.Json)
            setBody("""{"text":" "}""")
        }
            .apply {
            assertEquals(HttpStatusCode.BadRequest, status)
            assertEquals("Text cannot be empty", bodyAsText())
        }
    }

    @Test
    fun `get and delete record by id when record does not exist`() = testApplication {
        environment {
            config = MapApplicationConfig(
                "storage.url" to "jdbc:sqlite:data/test/testDB.db",
                "storage.driver" to "org.sqlite.JDBC",

                "open_router.api_key" to "test_api_key",
                "open_router.base_url" to "https://openrouter.ai/api/v1/chat/completions",
                "open_router.model" to "openrouter/free",
            )
        }
        application {
            module()
        }

        client.get("/ai/history/1000")
            .apply {
                assertEquals(HttpStatusCode.NotFound, status)
                assertEquals("Record not found", bodyAsText())
            }

        client.delete("/ai/history/1000")
            .apply {
                assertEquals(HttpStatusCode.NotFound, status)
                assertEquals("Record not found", bodyAsText())
            }
    }

}
