package com.example.matchlist.API

import com.example.matchlist.API.ProdutoDto
import retrofit2.http.GET

interface FakeStoreApi {
    // A base da URL é "https://fakestoreapi.com/"
    @GET("products/category/electronics")
    suspend fun getEletronicos(): List<ProdutoDto>
}