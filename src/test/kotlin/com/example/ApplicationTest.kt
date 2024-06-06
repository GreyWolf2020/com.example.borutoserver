package com.example

import com.example.models.ApiResponse
import com.example.models.Hero
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.server.testing.*
import kotlin.test.*
import io.ktor.http.*
import com.example.plugins.*
import com.example.repository.HeroRepository
import com.example.repository.NEXT_PAGE_KEY
import com.example.repository.PREVIOUS_PAGE_KEY
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.koin.java.KoinJavaComponent.inject

class ApplicationTest {

    private val heroRepository: HeroRepository by inject(HeroRepository::class.java)

    @Test
    fun `access root endpoint, assert correct information`() = testApplication {
        application {
            configureRouting()
        }
        client.get("/").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("Welcome to our Boruto API", bodyAsText())
        }
    }

    @Test
    fun `access all heroes endpoint, assert correct information`() = testApplication {
        environment {
            developmentMode = false
        }
        application {
            configureRouting()
        }
        val response = client.get("/boruto/heroes")
        assertEquals(HttpStatusCode.OK, response.status)
        val actual = Json.decodeFromString<ApiResponse>(response.bodyAsText())
        val expected = ApiResponse(
            success = true,
            message = "ok",
            prevPage = null,
            nextPage = 2,
            heroes = heroRepository.page01,
            lastUpdated = actual.lastUpdated
        )
        println("Expected: $expected")
        println("Actual: $actual")
        assertEquals(expected, actual)

    }

    @Test
    fun `access all heroes endpoint, query all pages, assert correct information`() = testApplication {
        environment {
            developmentMode = false
        }
        application {
            configureRouting()
        }
        val pages = 1 .. 5
        val heroes = listOf(
            { heroRepository.page01 },
            { heroRepository.page02 },
            { heroRepository.page03 },
            { heroRepository.page04 },
            { heroRepository.page05 }
        )
        pages.forEach { page ->
            val response = client.get("/boruto/heroes?page=$page")
            assertEquals(
                expected = HttpStatusCode.OK,
                actual = response.status
            )
            val actual = Json.decodeFromString<ApiResponse>(response.bodyAsText())
            val expected = ApiResponse(
                success = true,
                message = "ok",
                nextPage = calculatePage(page = page)["nextPage"],
                prevPage = calculatePage(page = page)["prevPage"],
                heroes = heroes[page - 1](),
                lastUpdated = actual.lastUpdated
            )

            assertEquals(
                expected = expected,
                actual = actual
            )
        }
    }

    @Test
    fun `access all heroes endpoint, query non existing page number, assert error`() = testApplication {
        environment {
            developmentMode = false
        }
        application {
            configureRouting()
        }
        client.get("/boruto/heroes?page=6").run {
            assertEquals(HttpStatusCode.NotFound, status)
//            val expected = ApiResponse(
//                success = false,
//                message = "Heroes Not Found"
//            )
//            val actual = Json.decodeFromString<ApiResponse>(bodyAsText())
//            println("Expected: $expected")
//            println("Actual: $actual")
//            assertEquals(expected, actual)
        }
    }

    @Test
    fun `access all heroes endpoint, query invalid page, assert error`() = testApplication {
        environment {
            developmentMode = false
        }
        application {
            configureRouting()
        }
        val response = client.get("/boruto/heroes?page=invalid")
        assertEquals(HttpStatusCode.BadRequest, response.status)
        val expected = ApiResponse(
            success = false,
            message = "Only Numbers Allowed"
        )
        val actual = Json.decodeFromString<ApiResponse>(response.bodyAsText())
        println("Expected: $expected")
        println("Actual: $actual")
        assertEquals(expected, actual)
    }

    private fun calculatePage(page: Int): Map<String, Int?> {
       return mapOf(
           PREVIOUS_PAGE_KEY to if (page in 2 .. 5) page.minus(1) else null,
           NEXT_PAGE_KEY to if (page in 1 .. 4) page.plus(1) else null
       )
    }


    @Test
    fun `access search heroes endpoint, query hero name, assert single hero`() = testApplication {
        environment {
            developmentMode = false
        }
        application {
            configureRouting()
        }
        client.get("/boruto/heroes/search?name=sas").run {
            assertEquals(HttpStatusCode.OK, status)
            val expected = 1
            val actual = Json.decodeFromString<ApiResponse>(bodyAsText()).heroes.size
            println("Expected: $expected")
            println("Actual: $actual")
            assertEquals(expected, actual)
        }
    }


    @Test
    fun `access search heroes endpoint, query hero name, assert multiple heroes`() = testApplication {
        environment {
            developmentMode = false
        }
        application {
            configureRouting()
        }
        client.get("/boruto/heroes/search?name=sa").run {
            assertEquals(HttpStatusCode.OK, status)
            val expected = 3
            val actual = Json.decodeFromString<ApiResponse>(bodyAsText()).heroes.size
            println("Expected: $expected")
            println("Actual: $actual")
            assertEquals(expected, actual)
        }
    }


    @Test
    fun `access search heroes endpoint, query any empty text, assert empty list of heroes`() = testApplication {
        environment {
            developmentMode = false
        }
        application {
            configureRouting()
        }
    val response = client.get("/boruto/heroes/search?name")
        assertEquals(HttpStatusCode.OK, response.status)
        val expected = emptyList<Hero>()
        val actual = Json.decodeFromString<ApiResponse>(response.bodyAsText()).heroes
        println("Expected: $expected")
        println("Actual: $actual")
        assertEquals(expected, actual)
    }

    @Test
    fun `access search heroes endpoint, query non existing hero, assert empty list of heroes`() = testApplication {
        environment {
            developmentMode = false
        }
        application {
            configureRouting()
        }
        client.get("/boruto/heroes/search?name=unkown").run {
            assertEquals(HttpStatusCode.OK, status)
            val expected = emptyList<Hero>()
            val actual = Json.decodeFromString<ApiResponse>(bodyAsText()).heroes
            println("Expected: $expected")
            println("Actual: $actual")
            assertEquals(expected, actual)
        }
    }


    @Test
    fun `access non existing endpoint, assert not found`() = testApplication {
        environment {
            developmentMode = false
        }
        application {
            configureRouting()
        }
        val response = client.get("/unkown")
        assertEquals(HttpStatusCode.NotFound, response.status)
        /*val expected = "page not found"
        val actual = Json.decodeFromString<ApiResponse>(bodyAsText()).message
        println("Expected: $expected")
        println("Actual: $actual")
        assertEquals(expected, actual)*/

    }


}
