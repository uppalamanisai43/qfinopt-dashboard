package com.qfinopt.app.data.repository

import com.qfinopt.app.data.api.ApiClient
import com.qfinopt.app.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody

class FundRepository {

    private val api get() = ApiClient.getApi()

    suspend fun checkHealth(): Result<Map<String, Any>> = withContext(Dispatchers.IO) {
        try {
            val response = api.healthCheck()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Health check failed with HTTP ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMarket(forceRefresh: Boolean = false): Result<MarketResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.getMarket(refresh = forceRefresh)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch market data: ${response.errorBody()?.string() ?: response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFilteredFunds(
        categories: List<String>?,
        riskLevels: List<String>?
    ): Result<FundsFilterResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.getFilteredFunds(categories, riskLevels)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to filter funds: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFundStatistics(fundName: String): Result<FundStatsResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.getFundStatistics(fundName)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch fund stats: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun simulateWithdrawal(req: WithdrawalRequest): Result<WithdrawalResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.simulateWithdrawal(req)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val err = response.errorBody()?.string()?.takeIf { it.isNotBlank() } ?: response.message()
                Result.failure(Exception("Withdrawal simulation error: $err"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun simulateSip(req: SipRequest): Result<SipResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.simulateSip(req)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val err = response.errorBody()?.string()?.takeIf { it.isNotBlank() } ?: response.message()
                Result.failure(Exception("SIP simulation error: $err"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFundHistory(
        fundName: String,
        period: String = "1Y",
        showForecast: Boolean = true,
        forecastDays: Int = 30
    ): Result<FundHistoryResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.getFundHistory(fundName, period, showForecast, forecastDays)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to load fund history: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun compareFunds(req: CompareRequest): Result<CompareResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.compareFunds(req)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to compare funds: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchLiveNav(query: String): Result<NavSearchResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.searchLiveNav(query)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("NAV search failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPlatformRecommendation(investment: Double): Result<PlatformRecommendationResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.getPlatformRecommendation(investment)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Platform recommendation failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun downloadPdfReport(req: PdfReportRequest): Result<ResponseBody> = withContext(Dispatchers.IO) {
        try {
            val response = api.downloadPdfReport(req)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to download PDF: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(req: UserRegisterRequest): Result<UserResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.register(req)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val err = response.errorBody()?.string()?.takeIf { it.isNotBlank() } ?: response.message()
                Result.failure(Exception(err))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(req: UserLoginRequest): Result<UserResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.login(req)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val err = response.errorBody()?.string()?.takeIf { it.isNotBlank() } ?: response.message()
                Result.failure(Exception(err))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginGuest(req: GuestLoginRequest): Result<UserResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.loginGuest(req)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val err = response.errorBody()?.string()?.takeIf { it.isNotBlank() } ?: response.message()
                Result.failure(Exception(err))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPortfolio(userId: String): Result<PortfolioResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.getPortfolio(userId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val err = response.errorBody()?.string()?.takeIf { it.isNotBlank() } ?: response.message()
                Result.failure(Exception("Portfolio load error: $err"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addHolding(req: AddHoldingRequest, userId: String): Result<HoldingItem> = withContext(Dispatchers.IO) {
        try {
            val response = api.addHolding(req, userId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val err = response.errorBody()?.string()?.takeIf { it.isNotBlank() } ?: response.message()
                Result.failure(Exception("Add holding error: $err"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteHolding(holdingId: String, userId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = api.deleteHolding(holdingId, userId)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                val err = response.errorBody()?.string()?.takeIf { it.isNotBlank() } ?: response.message()
                Result.failure(Exception("Delete holding error: $err"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getWatchlist(userId: String): Result<WatchlistResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.getWatchlist(userId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val err = response.errorBody()?.string()?.takeIf { it.isNotBlank() } ?: response.message()
                Result.failure(Exception("Watchlist error: $err"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleWatchlist(fundName: String, userId: String): Result<Map<String, Any>> = withContext(Dispatchers.IO) {
        try {
            val response = api.toggleWatchlist(WatchlistToggleRequest(fundName), userId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val err = response.errorBody()?.string()?.takeIf { it.isNotBlank() } ?: response.message()
                Result.failure(Exception("Watchlist toggle error: $err"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
