package com.example.data.network

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

data class FoodAnalysisResult(
    val foodName: String,
    val portion: String,
    val caloriesKcal: Int,
    val proteinGrams: Double,
    val carbsGrams: Double,
    val fatGrams: Double,
    val fiberGrams: Double,
    val healthScore: Int,
    val coachVerdict: String
)

data class HuaweiHealthResult(
    val workoutType: String,
    val durationMinutes: Int,
    val caloriesBurnedKcal: Int,
    val steps: Int,
    val distanceKm: Double,
    val avgHeartRateBpm: Int,
    val coachFeedback: String
)

class GeminiService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
    private val modelName = "gemini-3.5-flash"
    private val baseUrl = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent"

    private fun getApiKey(): String {
        return BuildConfig.GEMINI_API_KEY
    }

    suspend fun sendChatMessage(
        userMessage: String,
        conversationHistory: List<Pair<String, String>>, // sender ("user" or "model"), text
        systemInstructionText: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val apiKey = getApiKey()
            val url = "$baseUrl?key=$apiKey"

            val contentsArray = JSONArray()
            // Add conversation history
            for ((role, text) in conversationHistory.takeLast(10)) {
                val turnObj = JSONObject()
                val apiRole = if (role == "user") "user" else "model"
                turnObj.put("role", apiRole)
                val parts = JSONArray().apply {
                    put(JSONObject().put("text", text))
                }
                turnObj.put("parts", parts)
                contentsArray.put(turnObj)
            }

            // Add latest user message
            val currentTurn = JSONObject().apply {
                put("role", "user")
                put("parts", JSONArray().apply {
                    put(JSONObject().put("text", userMessage))
                })
            }
            contentsArray.put(currentTurn)

            val requestBodyJson = JSONObject().apply {
                put("contents", contentsArray)
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", systemInstructionText))
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                    put("topP", 0.95)
                })
            }

            val request = Request.Builder()
                .url(url)
                .post(requestBodyJson.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP ${response.code}: $responseBody"))
            }

            val rootJson = JSONObject(responseBody)
            val candidates = rootJson.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val candidate = candidates.getJSONObject(0)
                val content = candidate.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                if (parts != null && parts.length() > 0) {
                    val text = parts.getJSONObject(0).optString("text")
                    return@withContext Result.success(text)
                }
            }
            Result.failure(Exception("Empty response from Gemini"))
        } catch (e: Exception) {
            Log.e("GeminiService", "Error calling Gemini chat", e)
            Result.failure(e)
        }
    }

    suspend fun analyzeFoodPhoto(
        bitmap: Bitmap,
        userNote: String = ""
    ): Result<FoodAnalysisResult> = withContext(Dispatchers.IO) {
        try {
            val apiKey = getApiKey()
            val url = "$baseUrl?key=$apiKey"

            val base64Data = bitmapToBase64(bitmap)
            val promptText = """
                Kamu adalah Coach Profesional Diet Pribadi (Ahli Gizi). Analisis foto makanan ini secara detail untuk seorang perempuan umur 32 tahun, BB 67 kg, TB 155 cm yang sedang program diet defisit kalori target 1400 kcal/hari (target BB 52 kg).
                Catatan tambahan dari user: "$userNote".

                Balas HANYA dalam format JSON murni tanpa markdown formatting atau backticks:
                {
                  "foodName": "Nama hidangan lengkap dalam Bahasa Indonesia",
                  "portion": "Estimasi takaran porsi (contoh: 1 mangkok sedang / 1 piring)",
                  "caloriesKcal": 420,
                  "proteinGrams": 28.5,
                  "carbsGrams": 35.0,
                  "fatGrams": 12.0,
                  "fiberGrams": 5.5,
                  "healthScore": 8,
                  "coachVerdict": "Penilaian ahli gizi tentang kelayakan makanan ini untuk target defisit kalori dan tips praktis (contoh: kurangi kuah minyak / tambah sayuran hijau)"
                }
            """.trimIndent()

            val contentsArray = JSONArray().apply {
                val turn = JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", promptText))
                        put(JSONObject().apply {
                            put("inlineData", JSONObject().apply {
                                put("mimeType", "image/jpeg")
                                put("data", base64Data)
                            })
                        })
                    })
                }
                put(turn)
            }

            val requestBodyJson = JSONObject().apply {
                put("contents", contentsArray)
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.2)
                    put("responseMimeType", "application/json")
                })
            }

            val request = Request.Builder()
                .url(url)
                .post(requestBodyJson.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP ${response.code}: $responseBody"))
            }

            val rootJson = JSONObject(responseBody)
            val candidates = rootJson.optJSONArray("candidates")
            val text = candidates?.getJSONObject(0)
                ?.optJSONObject("content")
                ?.optJSONArray("parts")
                ?.getJSONObject(0)
                ?.optString("text") ?: ""

            val cleanedJson = text.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
            val parsed = JSONObject(cleanedJson)

            val result = FoodAnalysisResult(
                foodName = parsed.optString("foodName", "Makanan Diet Sehat"),
                portion = parsed.optString("portion", "1 porsi"),
                caloriesKcal = parsed.optInt("caloriesKcal", 350),
                proteinGrams = parsed.optDouble("proteinGrams", 20.0),
                carbsGrams = parsed.optDouble("carbsGrams", 30.0),
                fatGrams = parsed.optDouble("fatGrams", 10.0),
                fiberGrams = parsed.optDouble("fiberGrams", 4.0),
                healthScore = parsed.optInt("healthScore", 8),
                coachVerdict = parsed.optString("coachVerdict", "Pilihan yang baik untuk defisit kalori harianmu!")
            )
            Result.success(result)
        } catch (e: Exception) {
            Log.e("GeminiService", "Error analyzing food photo", e)
            Result.failure(e)
        }
    }

    suspend fun analyzeHuaweiHealthScreenshot(
        bitmap: Bitmap
    ): Result<HuaweiHealthResult> = withContext(Dispatchers.IO) {
        try {
            val apiKey = getApiKey()
            val url = "$baseUrl?key=$apiKey"

            val base64Data = bitmapToBase64(bitmap)
            val promptText = """
                Kamu adalah Asisten Analisis Data Olahraga. Gambar ini adalah tangkapan layar (screenshot) dari aplikasi Huawei Health.
                Tugasmu: Ekstrak data aktivitas fisik/olahraga dari screenshot tersebut secara akurat.
                Klien: Wanita 32 tahun, 67kg, program diet 4 bulan.

                Balas HANYA dalam format JSON murni tanpa markdown formatting:
                {
                  "workoutType": "Jenis aktivitas (contoh: Jalan Kaki Luar Ruangan, Bersepeda, Treadmill, Total Aktivitas Harian, Aerobik)",
                  "durationMinutes": 45,
                  "caloriesBurnedKcal": 230,
                  "steps": 6500,
                  "distanceKm": 4.2,
                  "avgHeartRateBpm": 122,
                  "coachFeedback": "Apresiasi dan saran profesional dari coach mengenai intensitas olahraga ini terhadap pembakaran lemak harian"
                }
            """.trimIndent()

            val contentsArray = JSONArray().apply {
                val turn = JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", promptText))
                        put(JSONObject().apply {
                            put("inlineData", JSONObject().apply {
                                put("mimeType", "image/jpeg")
                                put("data", base64Data)
                            })
                        })
                    })
                }
                put(turn)
            }

            val requestBodyJson = JSONObject().apply {
                put("contents", contentsArray)
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.2)
                    put("responseMimeType", "application/json")
                })
            }

            val request = Request.Builder()
                .url(url)
                .post(requestBodyJson.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP ${response.code}: $responseBody"))
            }

            val rootJson = JSONObject(responseBody)
            val candidates = rootJson.optJSONArray("candidates")
            val text = candidates?.getJSONObject(0)
                ?.optJSONObject("content")
                ?.optJSONArray("parts")
                ?.getJSONObject(0)
                ?.optString("text") ?: ""

            val cleanedJson = text.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
            val parsed = JSONObject(cleanedJson)

            val result = HuaweiHealthResult(
                workoutType = parsed.optString("workoutType", "Aktivitas Huawei Health"),
                durationMinutes = parsed.optInt("durationMinutes", 35),
                caloriesBurnedKcal = parsed.optInt("caloriesBurnedKcal", 180),
                steps = parsed.optInt("steps", 5000),
                distanceKm = parsed.optDouble("distanceKm", 3.0),
                avgHeartRateBpm = parsed.optInt("avgHeartRateBpm", 120),
                coachFeedback = parsed.optString("coachFeedback", "Kerja bagus! Pembakaran kalori dari Huawei Health sudah tercatat otomatis.")
            )
            Result.success(result)
        } catch (e: Exception) {
            Log.e("GeminiService", "Error analyzing Huawei Health screenshot", e)
            Result.failure(e)
        }
    }

    suspend fun generateAiDietRecipe(
        prompt: String,
        category: String
    ): Result<JSONObject> = withContext(Dispatchers.IO) {
        try {
            val apiKey = getApiKey()
            val url = "$baseUrl?key=$apiKey"

            val promptText = """
                Buatkan 1 resep masakan diet sehat lezat khas Indonesia/Asia untuk kategori $category.
                Klien: Perempuan umur 32 th, BB 67 kg, TB 155 cm (Target 1400 kcal/hari).
                Bahan atau preferensi yang diminta: "$prompt".

                Balas HANYA dalam format JSON murni:
                {
                  "title": "Nama Masakan Menarik & Sehat",
                  "category": "$category",
                  "description": "Deskripsi singkat kenikmatan & manfaatnya",
                  "prepTimeMinutes": 10,
                  "cookTimeMinutes": 20,
                  "caloriesKcal": 320,
                  "proteinGrams": 26.0,
                  "carbsGrams": 24.0,
                  "fatGrams": 8.0,
                  "fiberGrams": 6.0,
                  "ingredients": "Bahan 1\nBahan 2\nBahan 3 (tulis berbaris)",
                  "instructions": "1. Langkah pertama\n2. Langkah kedua\n3. Langkah ketiga"
                }
            """.trimIndent()

            val contentsArray = JSONArray().apply {
                val turn = JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", promptText))
                    })
                }
                put(turn)
            }

            val requestBodyJson = JSONObject().apply {
                put("contents", contentsArray)
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                    put("responseMimeType", "application/json")
                })
            }

            val request = Request.Builder()
                .url(url)
                .post(requestBodyJson.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP ${response.code}: $responseBody"))
            }

            val rootJson = JSONObject(responseBody)
            val text = rootJson.optJSONArray("candidates")
                ?.getJSONObject(0)
                ?.optJSONObject("content")
                ?.optJSONArray("parts")
                ?.getJSONObject(0)
                ?.optString("text") ?: ""

            val cleanedJson = text.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
            val parsed = JSONObject(cleanedJson)
            Result.success(parsed)
        } catch (e: Exception) {
            Log.e("GeminiService", "Error generating recipe", e)
            Result.failure(e)
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        // Resize bitmap if very large to prevent memory overflow
        val maxDimension = 1024
        val ratio = (maxDimension.toFloat() / Math.max(bitmap.width, bitmap.height)).coerceAtMost(1f)
        val scaledBitmap = if (ratio < 1f) {
            Bitmap.createScaledBitmap(bitmap, (bitmap.width * ratio).toInt(), (bitmap.height * ratio).toInt(), true)
        } else {
            bitmap
        }
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }
}
