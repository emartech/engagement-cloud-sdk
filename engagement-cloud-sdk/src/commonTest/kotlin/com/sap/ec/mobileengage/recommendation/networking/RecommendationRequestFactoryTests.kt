package com.sap.ec.mobileengage.recommendation.networking

import com.sap.ec.core.collections.ThreadSafePersistentStoreApi
import com.sap.ec.core.networking.model.UrlRequest
import com.sap.ec.core.url.ECUrlType
import com.sap.ec.core.url.UrlFactoryApi
import com.sap.ec.event.SdkEvent
import com.sap.ec.event.SdkEvent.Internal.Sdk.RequestRecommendation
import com.sap.ec.recommendation.CartItem
import com.sap.ec.recommendation.ComparisonType
import com.sap.ec.recommendation.FilterType
import com.sap.ec.recommendation.RecommendationFilter
import com.sap.ec.recommendation.RecommendationLogic
import com.sap.ec.recommendation.RecommendationOptions
import com.sap.ec.util.toJsonObject
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.resetCalls
import dev.mokkery.verify
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import io.ktor.http.HttpMethod
import io.ktor.http.Url
import io.ktor.http.formUrlEncode
import io.ktor.http.parameters
import kotlinx.coroutines.test.runTest
import kotlin.collections.emptyList
import kotlin.test.BeforeTest
import kotlin.test.Test


class RecommendationRequestFactoryTests {

    private lateinit var mockUrlFactory: UrlFactoryApi
    private lateinit var mockCartItemStorage: ThreadSafePersistentStoreApi<CartItem>
    private lateinit var recommendationRequestFactory: RecommendationRequestFactoryApi

    private companion object {
        const val APP_CODE = "ABCDE-12345"
        const val RECOMMENDATION_BASE_URL =
            "https://recommender.scarabresearch.com/merchants/$APP_CODE"
        const val CART_ITEM_1_ITEM_ID = "testCategory's/test%Item"
        const val CART_ITEM_1_ITEM_ID_URL_ENCODED = "testCategory%27s%2Ftest%25Item"
        const val CART_ITEM_2_ITEM_ID = "testCategory's/test%Item2"
        const val CART_ITEM_2_ITEM_ID_URL_ENCODED = "testCategory%27s%2Ftest%25Item2"
        val CART_ITEM_1 = CartItem(CART_ITEM_1_ITEM_ID, 10.0, 1.0)
        val CART_ITEM_2 = CartItem(CART_ITEM_2_ITEM_ID, 20.0, 3.0)
        const val TEST_CATEGORY_WITH_SPECIAL_CHAR = "test%category"
        const val TEST_ORDER_ID = "test%Order"
        const val SEARCH_TERM = "Search%Test"
        const val TEST_TAG = "test%Tag"
        const val TEST_CATEGORY = "testCategory"
        const val TEST_CATEGORY2 = "testCategory2"
    }

    @BeforeTest
    fun setup() {
        mockUrlFactory = mock(MockMode.autofill)
        everySuspend { mockUrlFactory.create(ECUrlType.Recommendation) } returns Url(
            RECOMMENDATION_BASE_URL
        )
        mockCartItemStorage = mock(MockMode.autofill)
        every { mockCartItemStorage.items } returns mutableListOf()
        recommendationRequestFactory =
            RecommendationRequestFactory(mockUrlFactory, mockCartItemStorage)
    }

    @Test
    fun test_create_ItemView_shouldReturn_itemViewUrlPath_withDoubleUrlEncodedCartItemId() =
        runTest {
            val itemViewEvent =
                SdkEvent.External.RecommendationTrackEvent.ItemView(CART_ITEM_1.itemId)
            val params = parameters {
                append("v", "i:$CART_ITEM_1_ITEM_ID_URL_ENCODED")
            }.formUrlEncode()
            val expectedUrl = "$RECOMMENDATION_BASE_URL?$params"

            val result = recommendationRequestFactory.create(itemViewEvent)

            assertUrl(result, expectedUrl)
        }

    @Test
    fun test_create_Cart_shouldReturn_cartEventUrlPath_when_trackingSingleCartItem() = runTest {
        val cartEvent = SdkEvent.External.RecommendationTrackEvent.Cart(listOf(CART_ITEM_1))
        val params = parameters {
            append("cv", "1")
            append(
                "ca",
                "i:${CART_ITEM_1_ITEM_ID_URL_ENCODED},p:${CART_ITEM_1.price},q:${CART_ITEM_1.quantity}"
            )
        }.formUrlEncode()
        val expectedUrl =
            "$RECOMMENDATION_BASE_URL?$params"

        val result = recommendationRequestFactory.create(cartEvent)

        assertUrl(result, expectedUrl)
    }

    @Test
    fun test_create_Cart_shouldReturn_cartEventUrlPath_when_trackingMultipleCartItems() = runTest {
        val cartEvent =
            SdkEvent.External.RecommendationTrackEvent.Cart(listOf(CART_ITEM_1, CART_ITEM_2))
        val params = parameters {
            append("cv", "1")
            append(
                "ca",
                "i:${CART_ITEM_1_ITEM_ID_URL_ENCODED},p:${CART_ITEM_1.price},q:${CART_ITEM_1.quantity}" +
                        "|i:${CART_ITEM_2_ITEM_ID_URL_ENCODED},p:${CART_ITEM_2.price},q:${CART_ITEM_2.quantity}"
            )
        }.formUrlEncode()
        val expectedUrl = "$RECOMMENDATION_BASE_URL?$params"

        val result = recommendationRequestFactory.create(cartEvent)

        assertUrl(result, expectedUrl)
    }

    @Test
    fun test_create_CategoryView_shouldReturn_categoryViewUrlPath() = runTest {
        val categoryView =
            SdkEvent.External.RecommendationTrackEvent.CategoryView(TEST_CATEGORY_WITH_SPECIAL_CHAR)
        val params = parameters {
            append("vc", TEST_CATEGORY_WITH_SPECIAL_CHAR)
        }.formUrlEncode()
        val expectedUrl = "$RECOMMENDATION_BASE_URL?$params"

        val result = recommendationRequestFactory.create(categoryView)

        assertUrl(result, expectedUrl)
    }

    @Test
    fun test_create_Purchase_shouldReturn_purchaseUrlPath_when_trackingSinglePurchasedCartItems() =
        runTest {
            val purchase =
                SdkEvent.External.RecommendationTrackEvent.Purchase(
                    TEST_ORDER_ID,
                    listOf(CART_ITEM_1)
                )
            val params = parameters {
                append("oi", TEST_ORDER_ID)
                append(
                    "co",
                    "i:${CART_ITEM_1_ITEM_ID_URL_ENCODED},p:${CART_ITEM_1.price},q:${CART_ITEM_1.quantity}"
                )
            }.formUrlEncode()
            val expectedUrl =
                "$RECOMMENDATION_BASE_URL?$params"

            val result = recommendationRequestFactory.create(purchase)

            assertUrl(result, expectedUrl)
        }

    @Test
    fun test_create_Purchase_shouldReturn_purchaseUrlPath_when_trackingMultiplePurchasedCartItems() =
        runTest {
            val purchase = SdkEvent.External.RecommendationTrackEvent.Purchase(
                TEST_ORDER_ID,
                listOf(CART_ITEM_1, CART_ITEM_2)
            )
            val params = parameters {
                append("oi", TEST_ORDER_ID)
                append(
                    "co",
                    "i:${CART_ITEM_1_ITEM_ID_URL_ENCODED},p:${CART_ITEM_1.price},q:${CART_ITEM_1.quantity}" +
                            "|i:${CART_ITEM_2_ITEM_ID_URL_ENCODED},p:${CART_ITEM_2.price},q:${CART_ITEM_2.quantity}"
                )
            }.formUrlEncode()
            val expectedUrl =
                "$RECOMMENDATION_BASE_URL?$params"

            val result = recommendationRequestFactory.create(purchase)

            assertUrl(result, expectedUrl)
        }

    @Test
    fun test_create_Search_shouldReturn_searchUrlPath_when_trackingSearch() = runTest {
        val search = SdkEvent.External.RecommendationTrackEvent.Search(SEARCH_TERM)
        val params = parameters {
            append("q", SEARCH_TERM)
        }.formUrlEncode()
        val expectedUrl = "$RECOMMENDATION_BASE_URL?$params"

        val result = recommendationRequestFactory.create(search)

        assertUrl(result, expectedUrl)
    }

    @Test
    fun test_create_Tag_shouldReturn_tagUrlPath_when_trackingTag_withoutTagAttributes() = runTest {
        val tag = SdkEvent.External.RecommendationTrackEvent.Tag(TEST_TAG)
        val params = parameters {
            append("t", TEST_TAG)
        }.formUrlEncode()
        val expectedUrl = "$RECOMMENDATION_BASE_URL?$params"

        val result = recommendationRequestFactory.create(tag)

        assertUrl(result, expectedUrl)
    }

    @Test
    fun test_create_Tag_shouldReturn_tagUrlPath_when_trackingTag_withTagAttributes_withSingleKeyValuePair() =
        runTest {
            val tagAttributes = mapOf("ke%y" to "val%ue")
            val tagEvent = SdkEvent.External.RecommendationTrackEvent.Tag(TEST_TAG, tagAttributes)
            val params = parameters {
                append(
                    "ta",
                    "{\"name\":\"$TEST_TAG\",\"attributes\":${tagAttributes.toJsonObject()}}"
                )
            }.formUrlEncode()
            val expectedUrl = "$RECOMMENDATION_BASE_URL?$params"

            val result = recommendationRequestFactory.create(tagEvent)

            assertUrl(result, expectedUrl)
        }

    @Test
    fun test_create_Tag_shouldReturn_tagUrlPath_when_trackingTag_withTagAttributes_withMultipleKeyValuePairs() =
        runTest {
            val tagAttributes = mapOf("ke%y" to "val%.!:ue", "ke%y2" to "val%ue2")
            val tagEvent = SdkEvent.External.RecommendationTrackEvent.Tag(TEST_TAG, tagAttributes)
            val params = parameters {
                append(
                    "ta",
                    "{\"name\":\"$TEST_TAG\",\"attributes\":${tagAttributes.toJsonObject()}}"
                )
            }.formUrlEncode()
            val expectedUrl = "$RECOMMENDATION_BASE_URL?$params"

            val result = recommendationRequestFactory.create(tagEvent)

            assertUrl(result, expectedUrl)
        }

    @Test
    fun test_creationClick_shouldReturn_recommendationClickUrlPath_withDoubleUrlEncodedProductId_when_trackingRecommendationClick() =
        runTest {
            val feature = "testFeature"
            val cohort = "testCohort"
            val recommendationClick =
                SdkEvent.External.RecommendationTrackEvent.RecommendationTrackClick(
                    CART_ITEM_1_ITEM_ID,
                    feature,
                    cohort
                )
            val params = parameters {
                append(
                    "v",
                    "i:$CART_ITEM_1_ITEM_ID_URL_ENCODED,t:$feature,c:$cohort"
                )
            }.formUrlEncode()
            val expectedUrl = "$RECOMMENDATION_BASE_URL?$params"

            val result = recommendationRequestFactory.create(recommendationClick)

            assertUrl(result, expectedUrl)
        }

    @Test
    fun test_create_shouldNotCall_cartItemsStorageItems_when_recommendationEvent_isNotRequestRecommendation() =
        runTest {
            val itemViewEvent =
                SdkEvent.External.RecommendationTrackEvent.ItemView(CART_ITEM_1.itemId)

            recommendationRequestFactory.create(itemViewEvent)

            verify(VerifyMode.exactly(0)) { mockCartItemStorage.items }
        }

    @Test
    fun test_create_shouldAlwaysReturn_recommendationUrlPath_withCartItemsAdded_andCallCartItemsStorageItems_whenEventIsRequestRecommendation() =
        runTest {
            every { mockCartItemStorage.items } returns mutableListOf(CART_ITEM_1)
            val requestRecommendationEvent = RequestRecommendation(
                options = RecommendationOptions(
                    RecommendationLogic.HOME
                )
            )
            val params = parameters {
                append(
                    "f",
                    "f:HOME,l:5,o:0"
                )
                append("cv", "1")
                append(
                    "ca",
                    "i:${CART_ITEM_1_ITEM_ID_URL_ENCODED},p:${CART_ITEM_1.price},q:${CART_ITEM_1.quantity}"
                )
            }.formUrlEncode()
            val expectedUrl = "$RECOMMENDATION_BASE_URL?$params"

            val result = recommendationRequestFactory.create(requestRecommendationEvent)

            assertUrl(result, expectedUrl)
            verify { mockCartItemStorage.items }
        }

    @Test
    fun test_create_shouldReturn_recommendationUrlPath_withBasicHomeLogic() = runTest {
        val requestRecommendationEvent = RequestRecommendation(
            options = RecommendationOptions(
                RecommendationLogic.HOME
            )
        )
        val params = parameters {
            append(
                "f",
                "f:HOME,l:5,o:0"
            )
            append("cv", "1")
            append(
                "ca",
                ""
            )
        }.formUrlEncode()
        val expectedUrl = "$RECOMMENDATION_BASE_URL?$params"

        val result = recommendationRequestFactory.create(requestRecommendationEvent)

        assertUrl(result, expectedUrl)
        verify { mockCartItemStorage.items }
    }

    @Test
    fun test_create_shouldReturn_recommendationUrlPath_withLimit10() = runTest {
        val requestRecommendationEvent =
            RequestRecommendation(
                options = RecommendationOptions(
                    logic = RecommendationLogic.HOME,
                    limit = 10
                )
            )
        val params = parameters {
            append(
                "f",
                "f:HOME,l:10,o:0"
            )
            append("cv", "1")
            append(
                "ca",
                ""
            )
        }.formUrlEncode()
        val expectedUrl = "$RECOMMENDATION_BASE_URL?$params"

        val result = recommendationRequestFactory.create(requestRecommendationEvent)

        assertUrl(result, expectedUrl)
        verify { mockCartItemStorage.items }
    }

    @Test
    fun test_create_shouldReturn_recommendationUrlPath_withOffset() = runTest {
        val requestRecommendationEvent =
            RequestRecommendation(
                options = RecommendationOptions(
                    logic = RecommendationLogic.HOME,
                    offset = 15
                )
            )
        val params = parameters {
            append(
                "f",
                "f:HOME,l:5,o:15"
            )
            append("cv", "1")
            append(
                "ca",
                ""
            )
        }.formUrlEncode()
        val expectedUrl = "$RECOMMENDATION_BASE_URL?$params"

        val result = recommendationRequestFactory.create(requestRecommendationEvent)

        assertUrl(result, expectedUrl)
        verify { mockCartItemStorage.items }
    }

    @Test
    fun test_create_shouldReturn_recommendationUrlPath_withAvailabilityZone() = runTest {
        val requestRecommendationEvent = RequestRecommendation(
            options = RecommendationOptions(
                logic = RecommendationLogic.HOME,
                availabilityZone = "eu"
            )
        )
        val params = parameters {
            append(
                "f",
                "f:HOME,l:5,o:0"
            )
            append("az", "eu")
            append("cv", "1")
            append(
                "ca",
                ""
            )
        }.formUrlEncode()
        val expectedUrl = "$RECOMMENDATION_BASE_URL?$params"

        val result = recommendationRequestFactory.create(requestRecommendationEvent)

        assertUrl(result, expectedUrl)
        verify { mockCartItemStorage.items }
    }

    @Test
    fun test_create_shouldReturn_recommendationUrlPath_withLanguage() = runTest {
        val requestRecommendationEvent =
            RequestRecommendation(
                options = RecommendationOptions(
                    logic = RecommendationLogic.HOME,
                    language = "hu"
                )
            )
        val params = parameters {
            append(
                "f",
                "f:HOME,l:5,o:0"
            )
            append("lang", "hu")
            append("cv", "1")
            append(
                "ca",
                ""
            )
        }.formUrlEncode()
        val expectedUrl = "$RECOMMENDATION_BASE_URL?$params"

        val result = recommendationRequestFactory.create(requestRecommendationEvent)

        assertUrl(result, expectedUrl)
        verify { mockCartItemStorage.items }
    }

    @Test
    fun test_create_shouldReturn_recommendationUrlPath_withCurrency() = runTest {
        val requestRecommendationEvent =
            RequestRecommendation(
                options = RecommendationOptions(
                    logic = RecommendationLogic.HOME,
                    displayCurrency = "EUR"
                )
            )
        val params = parameters {
            append(
                "f",
                "f:HOME,l:5,o:0"
            )
            append("currency", "EUR")
            append("cv", "1")
            append(
                "ca",
                ""
            )
        }.formUrlEncode()
        val expectedUrl = "$RECOMMENDATION_BASE_URL?$params"

        val result = recommendationRequestFactory.create(requestRecommendationEvent)

        assertUrl(result, expectedUrl)
        verify { mockCartItemStorage.items }
    }

    @Test
    fun test_create_shouldReturn_recommendationUrlPath_withFilters_parameterized() = runTest {
        data class TestCase(val options: RecommendationOptions, val expectedUrl: String)

        listOf(
            TestCase(
                options = RecommendationOptions(RecommendationLogic.HOME, filters = null),
                expectedUrl = "$RECOMMENDATION_BASE_URL?${
                    parameters {
                        append("f", "f:HOME,l:5,o:0")
                        append("cv", "1")
                        append("ca", "")
                    }.formUrlEncode()
                }"
            ),
            TestCase(
                options = RecommendationOptions(RecommendationLogic.HOME, filters = emptyList()),
                expectedUrl = "$RECOMMENDATION_BASE_URL?${
                    parameters {
                        append("f", "f:HOME,l:5,o:0")
                        append("cv", "1")
                        append("ca", "")
                    }.formUrlEncode()
                }"
            ),
            TestCase(
                options = RecommendationOptions(
                    RecommendationLogic.HOME,
                    filters = listOf(
                        RecommendationFilter(
                            FilterType.INCLUDE,
                            "category",
                            ComparisonType.IS,
                            TEST_CATEGORY
                        )
                    )
                ),
                expectedUrl = "$RECOMMENDATION_BASE_URL?${
                    parameters {
                        append("f", "f:HOME,l:5,o:0")
                        append(
                            "ex",
                            "[{\"f\":\"category\",\"r\":\"IS\",\"v\":\"$TEST_CATEGORY\",\"n\":true}]"
                        )
                        append("cv", "1")
                        append("ca", "")
                    }.formUrlEncode()
                }"
            ),
            TestCase(
                options = RecommendationOptions(
                    RecommendationLogic.HOME,
                    filters = listOf(
                        RecommendationFilter(
                            FilterType.INCLUDE,
                            "category",
                            ComparisonType.HAS,
                            TEST_CATEGORY
                        )
                    )
                ),
                expectedUrl = "$RECOMMENDATION_BASE_URL?${
                    parameters {
                        append("f", "f:HOME,l:5,o:0")
                        append(
                            "ex",
                            "[{\"f\":\"category\",\"r\":\"HAS\",\"v\":\"$TEST_CATEGORY\",\"n\":true}]"
                        )
                        append("cv", "1")
                        append("ca", "")
                    }.formUrlEncode()
                }"
            ),
            TestCase(
                options = RecommendationOptions(
                    RecommendationLogic.HOME,
                    filters = listOf(
                        RecommendationFilter(
                            FilterType.INCLUDE,
                            "category",
                            ComparisonType.OVERLAPS,
                            listOf(TEST_CATEGORY, TEST_CATEGORY2)
                        )
                    )
                ),
                expectedUrl = "$RECOMMENDATION_BASE_URL?${
                    parameters {
                        append("f", "f:HOME,l:5,o:0")
                        append(
                            "ex",
                            "[{\"f\":\"category\",\"r\":\"OVERLAPS\",\"v\":\"$TEST_CATEGORY|$TEST_CATEGORY2\",\"n\":true}]"
                        )
                        append("cv", "1")
                        append("ca", "")
                    }.formUrlEncode()
                }"
            ),
            TestCase(
                options = RecommendationOptions(
                    RecommendationLogic.HOME,
                    filters = listOf(
                        RecommendationFilter(
                            FilterType.INCLUDE,
                            "category",
                            ComparisonType.IN,
                            listOf(TEST_CATEGORY, TEST_CATEGORY2)
                        )
                    )
                ),
                expectedUrl = "$RECOMMENDATION_BASE_URL?${
                    parameters {
                        append("f", "f:HOME,l:5,o:0")
                        append(
                            "ex",
                            "[{\"f\":\"category\",\"r\":\"IN\",\"v\":\"$TEST_CATEGORY|$TEST_CATEGORY2\",\"n\":true}]"
                        )
                        append("cv", "1")
                        append("ca", "")
                    }.formUrlEncode()
                }"
            ),
            TestCase(
                options = RecommendationOptions(
                    RecommendationLogic.HOME,
                    filters = listOf(
                        RecommendationFilter(
                            FilterType.EXCLUDE,
                            "category",
                            ComparisonType.IS,
                            TEST_CATEGORY
                        )
                    )
                ),
                expectedUrl = "$RECOMMENDATION_BASE_URL?${
                    parameters {
                        append("f", "f:HOME,l:5,o:0")
                        append(
                            "ex",
                            "[{\"f\":\"category\",\"r\":\"IS\",\"v\":\"$TEST_CATEGORY\",\"n\":false}]"
                        )
                        append("cv", "1")
                        append("ca", "")
                    }.formUrlEncode()
                }"
            ),
            TestCase(
                options = RecommendationOptions(
                    RecommendationLogic.HOME, filters = listOf(
                        RecommendationFilter(
                            FilterType.INCLUDE,
                            "category",
                            ComparisonType.IS,
                            "bike"
                        ),
                        RecommendationFilter(
                            FilterType.EXCLUDE,
                            "type",
                            ComparisonType.IS,
                            "electric"
                        )
                    )
                ),
                expectedUrl = "$RECOMMENDATION_BASE_URL?${
                    parameters {
                        append("f", "f:HOME,l:5,o:0")
                        append(
                            "ex",
                            "[{\"f\":\"category\",\"r\":\"IS\",\"v\":\"bike\",\"n\":true},{\"f\":\"type\",\"r\":\"IS\",\"v\":\"electric\",\"n\":false}]"
                        )
                        append("cv", "1")
                        append("ca", "")
                    }.formUrlEncode()
                }"
            ),
            TestCase(
                options = RecommendationOptions(
                    RecommendationLogic.HOME, limit = 10, offset = 20, filters = listOf(
                        RecommendationFilter(
                            FilterType.INCLUDE,
                            "category",
                            ComparisonType.IS,
                            "bike"
                        ),
                        RecommendationFilter(
                            FilterType.EXCLUDE,
                            "type",
                            ComparisonType.IS,
                            "electric"
                        )
                    ), availabilityZone = "eu", language = "hu", displayCurrency = "EUR"
                ),
                expectedUrl = "$RECOMMENDATION_BASE_URL?${
                    parameters {
                        append("f", "f:HOME,l:10,o:20")
                        append(
                            "ex",
                            "[{\"f\":\"category\",\"r\":\"IS\",\"v\":\"bike\",\"n\":true},{\"f\":\"type\",\"r\":\"IS\",\"v\":\"electric\",\"n\":false}]"
                        )
                        append("az", "eu")
                        append("lang", "hu")
                        append("currency", "EUR")
                        append("cv", "1")
                        append("ca", "")
                    }.formUrlEncode()
                }"
            )
        ).forEach { (options, expectedUrl) ->
            val result =
                recommendationRequestFactory.create(RequestRecommendation(options = options))
            assertUrl(result, expectedUrl)
            verify { mockCartItemStorage.items }
            resetCalls(mockUrlFactory)
        }
    }

    private fun assertUrl(
        result: UrlRequest,
        expectedUrl: String
    ) {
        result.url.toString() shouldBe expectedUrl
        result.method shouldBe HttpMethod.Get
        verifySuspend(VerifyMode.exactly(1)) { mockUrlFactory.create(ECUrlType.Recommendation) }
    }
}