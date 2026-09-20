package com.qfinopt.app.data.api

import com.qfinopt.app.data.model.*
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface QFinOptApi {

    @GET("health")
    suspend fun healthCheck(): Response<Map<String, Any>>

    @GET("api/market")
    suspend fun getMarket(
        @Query("refresh") refresh: Boolean = false
    ): Response<MarketResponse>

    @GET("api/funds/filter")
    suspend fun getFilteredFunds(
        @Query("categories") categories: List<String>? = null,
        @Query("risk_levels") riskLevels: List<String>? = null
    ): Response<FundsFilterResponse>

    @GET("api/funds/{fund_name}/stats")
    suspend fun getFundStatistics(
        @Path("fund_name") fundName: String
    ): Response<FundStatsResponse>

    @POST("api/simulate/withdrawal")
    suspend fun simulateWithdrawal(
        @Body req: WithdrawalRequest
    ): Response<WithdrawalResponse>

    @POST("api/simulate/sip")
    suspend fun simulateSip(
        @Body req: SipRequest
    ): Response<SipResponse>

    @GET("api/funds/{fund_name}/history")
    suspend fun getFundHistory(
        @Path("fund_name") fundName: String,
        @Query("period") period: String = "1Y",
        @Query("show_forecast") showForecast: Boolean = true,
        @Query("forecast_days") forecastDays: Int = 30
    ): Response<FundHistoryResponse>

    @POST("api/funds/compare")
    suspend fun compareFunds(
        @Body req: CompareRequest
    ): Response<CompareResponse>

    @GET("api/nav/search")
    suspend fun searchLiveNav(
        @Query("q") query: String
    ): Response<NavSearchResponse>

    @GET("api/platform/recommendation")
    suspend fun getPlatformRecommendation(
        @Query("investment") investment: Double
    ): Response<PlatformRecommendationResponse>

    @POST("api/report/pdf")
    @Streaming
    suspend fun downloadPdfReport(
        @Body req: PdfReportRequest
    ): Response<ResponseBody>

    @POST("api/auth/register")
    suspend fun register(
        @Body req: UserRegisterRequest
    ): Response<UserResponse>

    @POST("api/auth/login")
    suspend fun login(
        @Body req: UserLoginRequest
    ): Response<UserResponse>

    @POST("api/auth/guest")
    suspend fun loginGuest(
        @Body req: GuestLoginRequest
    ): Response<UserResponse>

    @GET("api/portfolio")
    suspend fun getPortfolio(
        @Query("user_id") userId: String
    ): Response<PortfolioResponse>

    @POST("api/portfolio/holding")
    suspend fun addHolding(
        @Body req: AddHoldingRequest,
        @Query("user_id") userId: String
    ): Response<HoldingItem>

    @DELETE("api/portfolio/holding/{holding_id}")
    suspend fun deleteHolding(
        @Path("holding_id") holdingId: String,
        @Query("user_id") userId: String
    ): Response<Map<String, Any>>

    @GET("api/watchlist")
    suspend fun getWatchlist(
        @Query("user_id") userId: String
    ): Response<WatchlistResponse>

    @POST("api/watchlist/toggle")
    suspend fun toggleWatchlist(
        @Body req: WatchlistToggleRequest,
        @Query("user_id") userId: String
    ): Response<Map<String, Any>>
}
