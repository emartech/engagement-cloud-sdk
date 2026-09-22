package com.sap.ec.networking.clients.recommendation

import com.sap.ec.core.log.Logger
import com.sap.ec.core.networking.model.Response
import com.sap.ec.core.networking.model.UrlRequest
import com.sap.ec.recommendation.Product
import com.sap.ec.util.JsonUtil
import dev.mokkery.MockMode
import dev.mokkery.mock
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.ktor.http.Headers
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class RecommendationResponseMapperTests {

    private lateinit var recommendationResponseMapper: RecommendationResponseMapper
    private lateinit var mockLogger: Logger

    private companion object {
        val PRODUCT_1 = Product(
            productId = "2417",
            title = "LSL Women Cardigan Vest - White",
            linkUrl = "https://lifestylelabels.com/lsl-women-cardigan-vest-white.html",
            customFields = emptyMap(),
            imageUrl = "https://lifestylelabels.com/pub/media/catalog/product/w/t/wt029.jpg",
            zoomImageUrl = "https://lifestylelabels.com/pub/media/catalog/product/w/t/wt029.jpg",
            categoryPath = "WOMEN>Tops & Shirts|WOMEN>Hoodies|Cardigans & Jumpers",
            available = true,
            description = "LSL Women Cardigan Vest - White",
            price = 75.0,
            msrp = 75.0,
            brand = null,
            feature = "HOME_1",
            cohort = "AAAA"
        )

        val PRODUCT_1_WITH_CUSTOM_FIELDS = PRODUCT_1.copy(
            customFields = mapOf(
                "title_de_DE" to "LSL Damen Cardigan Weste – Weiß",
                "description_de_DE" to "LSL Damen Cardigan Weste – Weiß",
                "available_de_DE" to "false",
                "msrp_de_DE" to "75",
                "price_de_DE" to "75",
                "c_onsale" to null,
                "c_onsale_de_DE" to "onsale"
            )
        )
        val PRODUCT_2 = Product(
            productId = "2345",
            title = "LSL Women Long Trouser Artistic Pink Pattern",
            linkUrl = "https://lifestylelabels.com/lsl-women-long-trouser-artistic-pink-pattern.html",
            customFields = emptyMap(),
            imageUrl = "https://lifestylelabels.com/pub/media/catalog/product/w/t/wt014.jpg",
            zoomImageUrl = "https://lifestylelabels.com/pub/media/catalog/product/w/t/wt014.jpg",
            categoryPath = "WOMEN>Trousers",
            available = true,
            description = "LSL Women Long Trouser Artistic Pink Pattern",
            price = 650.0,
            msrp = 650.0,
            brand = null,
            feature = "HOME_1",
            cohort = "AAAA"
        )
        val PRODUCT_2_WITH_CUSTOM_FIELDS = PRODUCT_2.copy(
            customFields = mapOf(
                "title_de_DE" to "LSL Damen Lange Hose Künstlerisches Rosa Muster",
                "description_de_DE" to "LSL Damen Lange Hose Künstlerisches Rosa Muster",
                "available_de_DE" to "true",
                "msrp_de_DE" to "650",
                "price_de_DE" to "650",
                "c_onsale" to null,
                "c_onsale_de_DE" to "onsale"
            )
        )
    }

    @BeforeTest
    fun setup() {
        mockLogger = mock(MockMode.autofill)
        recommendationResponseMapper =
            RecommendationResponseMapper(
                JsonUtil.json,
                mockLogger
            )
    }

    @Test
    fun map_should_map_recommendationResponse_withoutCustomFields_into_listOfProducts() = runTest {
        val recommendationResponse = Response(
            originalRequest = UrlRequest(Url("test.com"), HttpMethod.Get),
            status = HttpStatusCode.OK,
            headers = Headers.Empty,
            bodyAsText = """
            {
                "cohort": "AAAA",
                "visitor": "44416F78595EAE75",
                "session": "2DF46162A933D904",
                "features": {
                    "HOME_1": {
                        "topicLabel": "WOMEN",
                        "hasMore": false,
                        "merchants": [
                            "1DF86BF95CBE8F19"
                        ],
                        "items": [
                            { "id": "2417" },
                            { "id": "2345" }
                        ]
                    }
                },
                "products": {
                    "2417": {
                        "item": "2417",
                        "category": "WOMEN>Tops & Shirts|WOMEN>Hoodies|Cardigans & Jumpers",
                        "title": "LSL Women Cardigan Vest - White",
                        "description": "LSL Women Cardigan Vest - White",
                        "available": true,
                        "msrp": 75.0,
                        "price": 75.0,
                        "link": "https://lifestylelabels.com/lsl-women-cardigan-vest-white.html",
                        "image": "https://lifestylelabels.com/pub/media/catalog/product/w/t/wt029.jpg",
                        "zoom_image": "https://lifestylelabels.com/pub/media/catalog/product/w/t/wt029.jpg"
                    },
                    "2345": {
                        "item": "2345",
                        "category": "WOMEN>Trousers",
                        "title": "LSL Women Long Trouser Artistic Pink Pattern",
                        "description": "LSL Women Long Trouser Artistic Pink Pattern",
                        "available": true,
                        "msrp": 650.0,
                        "price": 650.0,
                        "link": "https://lifestylelabels.com/lsl-women-long-trouser-artistic-pink-pattern.html",
                        "image": "https://lifestylelabels.com/pub/media/catalog/product/w/t/wt014.jpg",
                        "zoom_image": "https://lifestylelabels.com/pub/media/catalog/product/w/t/wt014.jpg"
                    }
                }
            }
        """.trimIndent()
        )

        val result = recommendationResponseMapper.map(recommendationResponse)

        result shouldBe listOf(PRODUCT_1, PRODUCT_2)
    }

    @Test
    fun map_should_map_recommendationResponse_into_listOfProducts_withCustomFieldsIncluded() =
        runTest {
            val recommendationResponse = Response(
                originalRequest = UrlRequest(Url("test.com"), HttpMethod.Get),
                status = HttpStatusCode.OK,
                headers = Headers.Empty,
                bodyAsText = """
                {
                    "cohort": "AAAA",
                    "visitor": "44416F78595EAE75",
                    "session": "2DF46162A933D904",
                    "features": {
                        "HOME_1": {
                            "topicLabel": "WOMEN",
                            "hasMore": false,
                            "merchants": [
                                "1DF86BF95CBE8F19"
                            ],
                            "items": [
                                { "id": "2417" },
                                { "id": "2345" }
                            ]
                        }
                    },
                    "products": {
                        "2417": {
                            "item": "2417",
                            "category": "WOMEN>Tops & Shirts|WOMEN>Hoodies|Cardigans & Jumpers",
                            "title": "LSL Women Cardigan Vest - White",
                            "title_de_DE": "LSL Damen Cardigan Weste – Weiß",
                            "description": "LSL Women Cardigan Vest - White",
                            "description_de_DE": "LSL Damen Cardigan Weste – Weiß",
                            "available": true,
                            "available_de_DE": "false",
                            "msrp": 75.0,
                            "price": 75.0,
                            "msrp_de_DE": "75",
                            "price_de_DE": "75",
                            "c_onsale": null,
                            "c_onsale_de_DE": "onsale",
                            "link": "https://lifestylelabels.com/lsl-women-cardigan-vest-white.html",
                            "image": "https://lifestylelabels.com/pub/media/catalog/product/w/t/wt029.jpg",
                            "zoom_image": "https://lifestylelabels.com/pub/media/catalog/product/w/t/wt029.jpg"
                        },
                        "2345": {
                            "item": "2345",
                            "category": "WOMEN>Trousers",
                            "title": "LSL Women Long Trouser Artistic Pink Pattern",
                            "title_de_DE": "LSL Damen Lange Hose Künstlerisches Rosa Muster",
                            "description": "LSL Women Long Trouser Artistic Pink Pattern",
                            "description_de_DE": "LSL Damen Lange Hose Künstlerisches Rosa Muster",
                            "available": true,
                            "available_de_DE": "true",
                            "msrp": 650.0,
                            "price": 650.0,
                            "msrp_de_DE": "650",
                            "price_de_DE": "650",
                            "c_onsale": null,
                            "c_onsale_de_DE": "onsale",
                            "link": "https://lifestylelabels.com/lsl-women-long-trouser-artistic-pink-pattern.html",
                            "image": "https://lifestylelabels.com/pub/media/catalog/product/w/t/wt014.jpg",
                            "zoom_image": "https://lifestylelabels.com/pub/media/catalog/product/w/t/wt014.jpg"
                        }
                    }
                }
            """.trimIndent()
            )

            val result = recommendationResponseMapper.map(recommendationResponse)

            result shouldBe listOf(PRODUCT_1_WITH_CUSTOM_FIELDS, PRODUCT_2_WITH_CUSTOM_FIELDS)
        }

    @Test
    fun map_should_map_recommendationResponse_into_listOfProducts_withCustomFieldsIncluded_when_multipleFeaturesReceived() =
        runTest {
            val recommendationResponse = Response(
                originalRequest = UrlRequest(Url("test.com"), HttpMethod.Get),
                status = HttpStatusCode.OK,
                headers = Headers.Empty,
                bodyAsText = """
            {
                "cohort": "AAAA",
                "visitor": "44416F78595EAE75",
                "session": "2DF46162A933D904",
                "features": {
                    "HOME_1": {
                        "topicLabel": "WOMEN",
                        "hasMore": false,
                        "merchants": [
                            "1DF86BF95CBE8F19"
                        ],
                        "items": [
                            { "id": "2417" },
                            { "id": "2345" }
                        ]
                    },
                    "PERSONAL": {
                        "hasMore": true,
                        "merchants": [
                            "1DF86BF95CBE8F19"
                        ],
                        "items": [
                            { "id": "2359" }
                        ]
                    }
                },
                "products": {
                    "2417": {
                        "item": "2417",
                        "category": "WOMEN>Tops & Shirts|WOMEN>Hoodies|Cardigans & Jumpers",
                        "title": "LSL Women Cardigan Vest - White",
                        "title_de_DE": "LSL Damen Cardigan Weste – Weiß",
                        "description": "LSL Women Cardigan Vest - White",
                        "description_de_DE": "LSL Damen Cardigan Weste – Weiß",
                        "available": true,
                        "available_de_DE": "false",
                        "msrp": 75.0,
                        "price": 75.0,
                        "msrp_de_DE": "75",
                        "price_de_DE": "75",
                        "c_onsale": null,
                        "c_onsale_de_DE": "onsale",
                        "link": "https://lifestylelabels.com/lsl-women-cardigan-vest-white.html",
                        "image": "https://lifestylelabels.com/pub/media/catalog/product/w/t/wt029.jpg",
                        "zoom_image": "https://lifestylelabels.com/pub/media/catalog/product/w/t/wt029.jpg"
                    },
                    "2345": {
                        "item": "2345",
                        "category": "WOMEN>Trousers",
                        "title": "LSL Women Long Trouser Artistic Pink Pattern",
                        "title_de_DE": "LSL Damen Lange Hose Künstlerisches Rosa Muster",
                        "description": "LSL Women Long Trouser Artistic Pink Pattern",
                        "description_de_DE": "LSL Damen Lange Hose Künstlerisches Rosa Muster",
                        "available": true,
                        "available_de_DE": "true",
                        "msrp": 650.0,
                        "price": 650.0,
                        "msrp_de_DE": "650",
                        "price_de_DE": "650",
                        "c_onsale": null,
                        "c_onsale_de_DE": "onsale",
                        "link": "https://lifestylelabels.com/lsl-women-long-trouser-artistic-pink-pattern.html",
                        "image": "https://lifestylelabels.com/pub/media/catalog/product/w/t/wt014.jpg",
                        "zoom_image": "https://lifestylelabels.com/pub/media/catalog/product/w/t/wt014.jpg"
                    },
                    "2359": {
                        "item": "2359",
                        "category": "WOMEN>Shoes",
                        "title": "LSL Women Sandals Classic - Black",
                        "description": "LSL Women Sandals Classic - Black",
                        "available": true,
                        "msrp": 350.0,
                        "price": 350.0,
                        "link": "https://lifestylelabels.com/lsl-women-sandals-classic-black.html",
                        "image": "https://lifestylelabels.com/pub/media/catalog/product/w/s/wsa001.jpg",
                        "zoom_image": "https://lifestylelabels.com/pub/media/catalog/product/w/s/wsa001.jpg"
                    }
                }
            }
        """.trimIndent()
            )

            val result = recommendationResponseMapper.map(recommendationResponse)

            result shouldBe listOf(
                PRODUCT_1_WITH_CUSTOM_FIELDS,
                PRODUCT_2_WITH_CUSTOM_FIELDS,
                Product(
                    productId = "2359",
                    title = "LSL Women Sandals Classic - Black",
                    linkUrl = "https://lifestylelabels.com/lsl-women-sandals-classic-black.html",
                    customFields = emptyMap(),
                    imageUrl = "https://lifestylelabels.com/pub/media/catalog/product/w/s/wsa001.jpg",
                    zoomImageUrl = "https://lifestylelabels.com/pub/media/catalog/product/w/s/wsa001.jpg",
                    categoryPath = "WOMEN>Shoes",
                    available = true,
                    description = "LSL Women Sandals Classic - Black",
                    price = 350.0,
                    msrp = 350.0,
                    feature = "PERSONAL",
                    cohort = "AAAA"
                )
            )
        }

    @Test
    fun map_shouldReturnEmptyListInsteadOfProduct_when_castingExceptionIsThrown_duringProductMappingAtMSRP() =
        runTest {
            val recommendationResponse = Response(
                originalRequest = UrlRequest(Url("test.com"), HttpMethod.Get),
                status = HttpStatusCode.OK,
                headers = Headers.Empty,
                bodyAsText = """
            {
                "cohort": "AAAA",
                "visitor": "44416F78595EAE75",
                "session": "2DF46162A933D904",
                "features": {
                    "HOME_1": {
                        "topicLabel": "WOMEN",
                        "hasMore": false,
                        "merchants": [
                            "1DF86BF95CBE8F19"
                        ],
                        "items": [
                            { "id": "2417" }
                        ]
                    }
                },
                "products": {
                    "2417": {
                        "item": "2417",
                        "category": "WOMEN>Tops & Shirts|WOMEN>Hoodies|Cardigans & Jumpers",
                        "title": "LSL Women Cardigan Vest - White",
                        "description": "LSL Women Cardigan Vest - White",
                        "available": true,
                        "msrp": "75.0abc",
                        "price": 75.0,
                        "link": "https://lifestylelabels.com/lsl-women-cardigan-vest-white.html",
                        "image": "https://lifestylelabels.com/pub/media/catalog/product/w/t/wt029.jpg",
                        "zoom_image": "https://lifestylelabels.com/pub/media/catalog/product/w/t/wt029.jpg"
                    }
                }
            }
        """.trimIndent()
            )

            val result = recommendationResponseMapper.map(recommendationResponse)

            result shouldBe emptyList()
        }

    @Test
    fun map_shouldSkipProduct_when_exceptionIsThrown_duringProductMapping() =
        runTest {
            val recommendationResponse = Response(
                originalRequest = UrlRequest(Url("test.com"), HttpMethod.Get),
                status = HttpStatusCode.OK,
                headers = Headers.Empty,
                bodyAsText = """
            {
                "cohort": "AAAA",
                "visitor": "44416F78595EAE75",
                "session": "2DF46162A933D904",
                "features": {
                    "HOME_1": {
                        "topicLabel": "WOMEN",
                        "hasMore": false,
                        "merchants": [
                            "1DF86BF95CBE8F19"
                        ],
                        "items": [
                            { "id": "2417" },
                            { "id": "2345" }
                        ]
                    }
                },
                "products": {
                    "2417": {
                        "item": "2417",
                        "category": "WOMEN>Tops & Shirts|WOMEN>Hoodies|Cardigans & Jumpers",
                        "title": "LSL Women Cardigan Vest - White",
                        "description": "LSL Women Cardigan Vest - White",
                        "available": true,
                        "msrp": "75.0abc",
                        "price": 75.0,
                        "link": "https://lifestylelabels.com/lsl-women-cardigan-vest-white.html",
                        "image": "https://lifestylelabels.com/pub/media/catalog/product/w/t/wt029.jpg",
                        "zoom_image": "https://lifestylelabels.com/pub/media/catalog/product/w/t/wt029.jpg"
                    },
                    "2345": {
                        "item": "2345",
                        "category": "WOMEN>Trousers",
                        "title": "LSL Women Long Trouser Artistic Pink Pattern",
                        "title_de_DE": "LSL Damen Lange Hose Künstlerisches Rosa Muster",
                        "description": "LSL Women Long Trouser Artistic Pink Pattern",
                        "description_de_DE": "LSL Damen Lange Hose Künstlerisches Rosa Muster",
                        "available": true,
                        "available_de_DE": "true",
                        "msrp": 650.0,
                        "price": 650.0,
                        "msrp_de_DE": "650",
                        "price_de_DE": "650",
                        "c_onsale": null,
                        "c_onsale_de_DE": "onsale",
                        "link": "https://lifestylelabels.com/lsl-women-long-trouser-artistic-pink-pattern.html",
                        "image": "https://lifestylelabels.com/pub/media/catalog/product/w/t/wt014.jpg",
                        "zoom_image": "https://lifestylelabels.com/pub/media/catalog/product/w/t/wt014.jpg"
                    }
                }
            }
        """.trimIndent()
            )

            val result = recommendationResponseMapper.map(recommendationResponse)

            result shouldBe listOf(PRODUCT_2_WITH_CUSTOM_FIELDS)
        }

    @Test
    fun map_shouldThrow_when_featuresAreEmpty() = runTest {
        val recommendationResponse = Response(
            originalRequest = UrlRequest(Url("test.com"), HttpMethod.Get),
            status = HttpStatusCode.OK,
            headers = Headers.Empty,
            bodyAsText = """
            {
                "cohort": "AAAA",
                "visitor": "44416F78595EAE75",
                "session": "2DF46162A933D904",
                "features": {},
                "products": {
                    "2417": {
                        "item": "2417",
                        "category": "WOMEN>Tops & Shirts|WOMEN>Hoodies|Cardigans & Jumpers",
                        "title": "LSL Women Cardigan Vest - White",
                        "description": "LSL Women Cardigan Vest - White",
                        "available": true,
                        "msrp": "75.0abc",
                        "price": 75.0,
                        "link": "https://lifestylelabels.com/lsl-women-cardigan-vest-white.html",
                        "image": "https://lifestylelabels.com/pub/media/catalog/product/w/t/wt029.jpg",
                        "zoom_image": "https://lifestylelabels.com/pub/media/catalog/product/w/t/wt029.jpg"
                    }
                }
            }
        """.trimIndent()
        )

        shouldThrow<RuntimeException> { recommendationResponseMapper.map(recommendationResponse) }
    }

    @Test
    fun map_shouldReturnEmptyList_when_featuresAreMissing() = runTest {
        val recommendationResponse = Response(
            originalRequest = UrlRequest(Url("test.com"), HttpMethod.Get),
            status = HttpStatusCode.OK,
            headers = Headers.Empty,
            bodyAsText = """
            {
                "cohort": "AAAA",
                "visitor": "44416F78595EAE75",
                "session": "2DF46162A933D904",
                "products": {
                    "2417": {
                        "item": "2417",
                        "category": "WOMEN>Tops & Shirts|WOMEN>Hoodies|Cardigans & Jumpers",
                        "title": "LSL Women Cardigan Vest - White",
                        "description": "LSL Women Cardigan Vest - White",
                        "available": true,
                        "msrp": "75.0abc",
                        "price": 75.0,
                        "link": "https://lifestylelabels.com/lsl-women-cardigan-vest-white.html",
                        "image": "https://lifestylelabels.com/pub/media/catalog/product/w/t/wt029.jpg",
                        "zoom_image": "https://lifestylelabels.com/pub/media/catalog/product/w/t/wt029.jpg"
                    }
                }
            }
        """.trimIndent()
        )

        shouldThrow<RuntimeException> { recommendationResponseMapper.map(recommendationResponse) }
    }

    @Test
    fun map_shouldReturnEmptyList_when_noProductsReturnedForTheSearchCriteria() = runTest {
        val recommendationResponse = Response(
            originalRequest = UrlRequest(Url("test.com"), HttpMethod.Get),
            status = HttpStatusCode.OK,
            headers = Headers.Empty,
            bodyAsText = """
            {
                "cohort": "AAAA",
                "visitor": "44416F78595EAE75",
                "session": "2DF46162A933D904",
                "features": {
                    "HOME_1": {
                        "hasMore": false,
                        "merchants": [],
                        "items": []
                    }
                }
            }
        """.trimIndent()
        )

        val result = recommendationResponseMapper.map(recommendationResponse)

        result shouldBe emptyList()
    }

    @Test
    fun map_shouldOmitProduct_when_itIsNotAssignedToAFeature() = runTest {
        val recommendationResponse = Response(
            originalRequest = UrlRequest(Url("test.com"), HttpMethod.Get),
            status = HttpStatusCode.OK,
            headers = Headers.Empty,
            bodyAsText = """
            {
                "cohort": "AAAA",
                "visitor": "44416F78595EAE75",
                "session": "2DF46162A933D904",
                "features": {
                    "HOME_1": {
                        "topicLabel": "WOMEN",
                        "hasMore": false,
                        "merchants": [
                            "1DF86BF95CBE8F19"
                        ],
                        "items": [
                            { "id": "2417" }
                        ]
                    }
                },
                "products": {
                    "2417": {
                        "item": "2417",
                        "category": "WOMEN>Tops & Shirts|WOMEN>Hoodies|Cardigans & Jumpers",
                        "title": "LSL Women Cardigan Vest - White",
                        "description": "LSL Women Cardigan Vest - White",
                        "available": true,
                        "msrp": 75.0,
                        "price": 75.0,
                        "link": "https://lifestylelabels.com/lsl-women-cardigan-vest-white.html",
                        "image": "https://lifestylelabels.com/pub/media/catalog/product/w/t/wt029.jpg",
                        "zoom_image": "https://lifestylelabels.com/pub/media/catalog/product/w/t/wt029.jpg"
                    },
                    "2345": {
                        "item": "2345",
                        "category": "WOMEN>Trousers",
                        "title": "LSL Women Long Trouser Artistic Pink Pattern",
                        "title_de_DE": "LSL Damen Lange Hose Künstlerisches Rosa Muster",
                        "description": "LSL Women Long Trouser Artistic Pink Pattern",
                        "description_de_DE": "LSL Damen Lange Hose Künstlerisches Rosa Muster",
                        "available": true,
                        "available_de_DE": "true",
                        "msrp": 650.0,
                        "price": 650.0,
                        "msrp_de_DE": "650",
                        "price_de_DE": "650",
                        "c_onsale": null,
                        "c_onsale_de_DE": "onsale",
                        "link": "https://lifestylelabels.com/lsl-women-long-trouser-artistic-pink-pattern.html",
                        "image": "https://lifestylelabels.com/pub/media/catalog/product/w/t/wt014.jpg",
                        "zoom_image": "https://lifestylelabels.com/pub/media/catalog/product/w/t/wt014.jpg"
                    }
                }
            }
        """.trimIndent()
        )

        val result = recommendationResponseMapper.map(recommendationResponse)

        result shouldBe listOf(PRODUCT_1)
    }

    @Test
    fun map_shouldThrow_when_responseDoesntContainCohort() = runTest {
        val recommendationResponse = Response(
            originalRequest = UrlRequest(Url("test.com"), HttpMethod.Get),
            status = HttpStatusCode.OK,
            headers = Headers.Empty,
            bodyAsText = """
            {
                "visitor": "44416F78595EAE75",
                "session": "2DF46162A933D904",
                "features": {
                    "HOME_1": {
                        "topicLabel": "WOMEN",
                        "hasMore": false,
                        "merchants": [
                            "1DF86BF95CBE8F19"
                        ],
                        "items": [
                            { "id": "2417" }
                        ]
                    }
                },
                "products": {
                    "2417": {
                        "item": "2417",
                        "category": "WOMEN>Tops & Shirts|WOMEN>Hoodies|Cardigans & Jumpers",
                        "title": "LSL Women Cardigan Vest - White",
                        "description": "LSL Women Cardigan Vest - White",
                        "available": true,
                        "msrp": 75.0,
                        "price": 75.0,
                        "link": "https://lifestylelabels.com/lsl-women-cardigan-vest-white.html",
                        "image": "https://lifestylelabels.com/pub/media/catalog/product/w/t/wt029.jpg",
                        "zoom_image": "https://lifestylelabels.com/pub/media/catalog/product/w/t/wt029.jpg"
                    }
                }
            }
        """.trimIndent()
        )

        shouldThrow<RuntimeException> { recommendationResponseMapper.map(recommendationResponse) }
    }

    @Test
    fun map_shouldThrow_serializationException_when_MandatoryItemFieldIsMissingFromFeature() = runTest {
        val recommendationResponse = Response(
            originalRequest = UrlRequest(Url("test.com"), HttpMethod.Get),
            status = HttpStatusCode.OK,
            headers = Headers.Empty,
            bodyAsText = """
            {
                "cohort": "AAAA",
                "visitor": "44416F78595EAE75",
                "session": "2DF46162A933D904",
                "features": {
                    "HOME_1": {
                        "topicLabel": "WOMEN",
                        "hasMore": false,
                        "merchants": [
                            "1DF86BF95CBE8F19"
                        ]
                    }
                },
                "products": {
                    "2417": {
                        "item": "2417",
                        "category": "WOMEN>Tops & Shirts|WOMEN>Hoodies|Cardigans & Jumpers",
                        "title": "LSL Women Cardigan Vest - White",
                        "description": "LSL Women Cardigan Vest - White",
                        "available": true,
                        "msrp": 75.0,
                        "price": 75.0,
                        "link": "https://lifestylelabels.com/lsl-women-cardigan-vest-white.html",
                        "image": "https://lifestylelabels.com/pub/media/catalog/product/w/t/wt029.jpg",
                        "zoom_image": "https://lifestylelabels.com/pub/media/catalog/product/w/t/wt029.jpg"
                    },
                    "2345": {
                        "item": "2345",
                        "category": "WOMEN>Trousers",
                        "title": "LSL Women Long Trouser Artistic Pink Pattern",
                        "description": "LSL Women Long Trouser Artistic Pink Pattern",
                        "available": true,
                        "msrp": 650.0,
                        "price": 650.0,
                        "link": "https://lifestylelabels.com/lsl-women-long-trouser-artistic-pink-pattern.html",
                        "image": "https://lifestylelabels.com/pub/media/catalog/product/w/t/wt014.jpg",
                        "zoom_image": "https://lifestylelabels.com/pub/media/catalog/product/w/t/wt014.jpg"
                    }
                }
            }
        """.trimIndent()
        )

        shouldThrow<RuntimeException> { recommendationResponseMapper.map(recommendationResponse) }
    }

}