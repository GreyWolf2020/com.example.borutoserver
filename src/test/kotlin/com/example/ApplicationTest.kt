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
        client.get("/boruto/heroes").apply {
            assertEquals(HttpStatusCode.OK, status)
            val actual = Json.decodeFromString<ApiResponse>(bodyAsText())
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
            client.get("/boruto/heroes?page=$page").apply {
                assertEquals(
                    expected = HttpStatusCode.OK,
                    actual = status
                )
                val actual = Json.decodeFromString<ApiResponse>(bodyAsText())
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
    }

    @Test
    fun `access all heroes endpoint, query non existing page number, assert error`() = testApplication {
        environment {
            developmentMode = false
        }
        application {
            configureRouting()
        }
        client.get("/boruto/heroes?page=6").apply {
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
        client.get("/boruto/heroes?page=invalid").apply {
            assertEquals(HttpStatusCode.BadRequest, status)
            val expected = ApiResponse(
                success = false,
                message = "Only Numbers Allowed"
            )
            val actual = Json.decodeFromString<ApiResponse>(bodyAsText())
            println("Expected: $expected")
            println("Actual: $actual")
            assertEquals(expected, actual)
        }
    }

    private fun calculatePage(page: Int): Map<String, Int?> {
        var prevPage: Int? = page
        var nextPage: Int? = page
        if(page in 1 .. 4) {
            nextPage = nextPage?.plus(1)
        }
        if (page in  2 .. 5) {
            prevPage = prevPage?.minus(1)
        }
        if (page == 1) {
            prevPage = null
        }
        if (page == 5) {
            nextPage = null
        }
        return mapOf(
            "prevPage" to prevPage,
            "nextPage" to nextPage
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
        client.get("/boruto/heroes/search?name=sas").apply {
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
        client.get("/boruto/heroes/search?name=sa").apply {
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
        client.get("/boruto/heroes/search?name").apply {
            assertEquals(HttpStatusCode.OK, status)
            val expected = emptyList<Hero>()
            val actual = Json.decodeFromString<ApiResponse>(bodyAsText()).heroes
            println("Expected: $expected")
            println("Actual: $actual")
            assertEquals(expected, actual)
        }
    }

    @Test
    fun `access search heroes endpoint, query non existing hero, assert empty list of heroes`() = testApplication {
        environment {
            developmentMode = false
        }
        application {
            configureRouting()
        }
        client.get("/boruto/heroes/search?name=unkown").apply {
            assertEquals(HttpStatusCode.OK, status)
            val expected = emptyList<Hero>()
            val actual = Json.decodeFromString<ApiResponse>(bodyAsText()).heroes
            println("Expected: $expected")
            println("Actual: $actual")
            assertEquals(expected, actual)
        }
    }


    @Test
    fun `access non existing endpoint, assertnot found`() = testApplication {
        environment {
            developmentMode = false
        }
        application {
            configureRouting()
        }
        client.get("/unkown").apply {
            assertEquals(HttpStatusCode.NotFound, status)
            /*val expected = "page not found"
            val actual = Json.decodeFromString<ApiResponse>(bodyAsText()).message
            println("Expected: $expected")
            println("Actual: $actual")
            assertEquals(expected, actual)*/
        }
    }


}
