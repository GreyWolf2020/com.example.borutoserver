package com.example.repository

import com.example.models.ApiResponse
import com.example.models.Hero

interface HeroRepository {

    val heroes: Map<Int, List<Hero>>

    val page01: List<Hero>
    val page02: List<Hero>
    val page03: List<Hero>
    val page04: List<Hero>
    val page05: List<Hero>

    suspend fun getAllHeroes(page: Int): ApiResponse
    suspend fun searchHeroes(name: String?): ApiResponse

}