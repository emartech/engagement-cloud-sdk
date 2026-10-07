package com.sap.ec.mobileengage.recommendation.networking

import com.sap.ec.core.collections.ThreadSafePersistentStoreApi
import com.sap.ec.core.networking.model.UrlRequest
import com.sap.ec.core.url.ECUrlType
import com.sap.ec.core.url.UrlFactoryApi
import com.sap.ec.event.SdkEvent
import com.sap.ec.event.SdkEvent.Internal.Sdk.RequestRecommendation
import com.sap.ec.recommendation.models.CartItem
import com.sap.ec.recommendation.models.requestRecommendation.ComparisonType
import com.sap.ec.recommendation.models.requestRecommendation.FilterType
import com.sap.ec.recommendation.models.requestRecommendation.RecommendationFilter
import com.sap.ec.recommendation.models.requestRecommendation.RecommendationLogic
import com.sap.ec.recommendation.models.requestRecommendation.RecommendationOptions
import com.sap.ec.recommendation.networking.RecommendationRequestFactory
import com.sap.ec.recommendation.networking.RecommendationRequestFactoryApi
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
import io.ktor.http.ParametersBuilder
import io.ktor.http.Url
import io.ktor.http.formUrlEncode
import io.ktor.http.parameters
import kotlinx.coroutines.test.runTest
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
                appendEmptyCart()
            }.formUrlEncode()
            val expectedUrl = "$RECOMMENDATION_BASE_URL?$params"

            val result = recommendationRequestFactory.create(itemViewEvent)

            verifyResult(result, expectedUrl, shouldMockCartItemStorageBeCalled = true)
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

        verifyResult(result, expectedUrl, shouldMockCartItemStorageBeCalled = false)
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

        verifyResult(result, expectedUrl, shouldMockCartItemStorageBeCalled = false)
    }

    @Test
    fun test_create_CategoryView_shouldReturn_categoryViewUrlPath() = runTest {
        val categoryView =
            SdkEvent.External.RecommendationTrackEvent.CategoryView(TEST_CATEGORY_WITH_SPECIAL_CHAR)
        val params = parameters {
            append("vc", TEST_CATEGORY_WITH_SPECIAL_CHAR)
            appendEmptyCart()
        }.formUrlEncode()
        val expectedUrl = "$RECOMMENDATION_BASE_URL?$params"

        val result = recommendationRequestFactory.create(categoryView)

        verifyResult(result, expectedUrl, shouldMockCartItemStorageBeCalled = true)
    }

    @Test
    fun test_create_Purchase_shouldReturn_purchaseUrlPath_withEmptyActualCart_when_trackingSinglePurchasedCartItems() =
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
                appendEmptyCart()
            }.formUrlEncode()
            val expectedUrl =
                "$RECOMMENDATION_BASE_URL?$params"

            val result = recommendationRequestFactory.create(purchase)

            verifyResult(result, expectedUrl, shouldMockCartItemStorageBeCalled = false)
        }

    @Test
    fun test_create_Purchase_shouldReturn_purchaseUrlPath_withEmptyActualCart_when_trackingMultiplePurchasedCartItems() =
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
                appendEmptyCart()
            }.formUrlEncode()
            val expectedUrl =
                "$RECOMMENDATION_BASE_URL?$params"

            val result = recommendationRequestFactory.create(purchase)

            verifyResult(result, expectedUrl, shouldMockCartItemStorageBeCalled = false)
        }

    @Test
    fun test_create_Search_shouldReturn_searchUrlPath_withEmptyCart_when_trackingSearch() = runTest {
        val search = SdkEvent.External.RecommendationTrackEvent.Search(SEARCH_TERM)
        val params = parameters {
            append("q", SEARCH_TERM)
            appendEmptyCart()
        }.formUrlEncode()
        val expectedUrl = "$RECOMMENDATION_BASE_URL?$params"

        val result = recommendationRequestFactory.create(search)

        verifyResult(result, expectedUrl, shouldMockCartItemStorageBeCalled = true)
    }

    @Test
    fun test_create_Tag_shouldReturn_tagUrlPath_withEmptyCart_when_trackingTag_withoutTagAttributes() = runTest {
        val tag = SdkEvent.External.RecommendationTrackEvent.Tag(TEST_TAG)
        val params = parameters {
            append("t", TEST_TAG)
            appendEmptyCart()
        }.formUrlEncode()
        val expectedUrl = "$RECOMMENDATION_BASE_URL?$params"

        val result = recommendationRequestFactory.create(tag)

        verifyResult(result, expectedUrl, shouldMockCartItemStorageBeCalled = true)
    }

    @Test
    fun test_create_Tag_shouldReturn_tagUrlPath_withEmptyCart_when_trackingTag_withTagAttributes_withSingleKeyValuePair() =
        runTest {
            val tagAttributes = mapOf("ke%y" to "val%ue")
            val tagEvent = SdkEvent.External.RecommendationTrackEvent.Tag(TEST_TAG, tagAttributes)
            val params = parameters {
                append(
                    "ta",
                    "{\"name\":\"$TEST_TAG\",\"attributes\":${tagAttributes.toJsonObject()}}"
                )
                appendEmptyCart()
            }.formUrlEncode()
            val expectedUrl = "$RECOMMENDATION_BASE_URL?$params"

            val result = recommendationRequestFactory.create(tagEvent)

            verifyResult(result, expectedUrl, shouldMockCartItemStorageBeCalled = true)
        }

    @Test
    fun test_create_Tag_shouldReturn_tagUrlPath_withEmptyCart_when_trackingTag_withTagAttributes_withMultipleKeyValuePairs() =
        runTest {
            val tagAttributes = mapOf("ke%y" to "val%.!:ue", "ke%y2" to "val%ue2")
            val tagEvent = SdkEvent.External.RecommendationTrackEvent.Tag(TEST_TAG, tagAttributes)
            val params = parameters {
                append(
                    "ta",
                    "{\"name\":\"$TEST_TAG\",\"attributes\":${tagAttributes.toJsonObject()}}"
                )
                appendEmptyCart()
            }.formUrlEncode()
            val expectedUrl = "$RECOMMENDATION_BASE_URL?$params"

            val result = recommendationRequestFactory.create(tagEvent)

            verifyResult(result, expectedUrl, shouldMockCartItemStorageBeCalled = true)
        }

    @Test
    fun test_creationClick_shouldReturn_recommendationClickUrlPath_withEmptyCart_and_withDoubleUrlEncodedProductId_when_trackingRecommendationClick() =
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
                appendEmptyCart()
            }.formUrlEncode()
            val expectedUrl = "$RECOMMENDATION_BASE_URL?$params"

            val result = recommendationRequestFactory.create(recommendationClick)

            verifyResult(result, expectedUrl, shouldMockCartItemStorageBeCalled = true)
        }

    @Test
    fun test_create_shouldAlwaysReturn_recommendationUrlPath_withCartItemsAdded_andCallCartItemsStorageItems_whenEventIsRequestRecommendation() =
        runTest {
            every { mockCartItemStorage.items } returns mutableListOf(CART_ITEM_1)
            val requestRecommendationEvent = RequestRecommendation(
                options = RecommendationOptions(
                    RecommendationLogic.Home
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

            verifyResult(result, expectedUrl, shouldMockCartItemStorageBeCalled = true)
        }

    @Test
    fun test_create_recommendation_withCustomLogic_shouldReturnRequestWithCustomLogicName_regardlessItsCase() =
        runTest {
            val expectedString = "customLogicName"
            every { mockCartItemStorage.items } returns mutableListOf(CART_ITEM_1)
            val requestRecommendationEvent = RequestRecommendation(
                options = RecommendationOptions(
                    logic = RecommendationLogic.Custom(expectedString)
                )
            )
            val params = parameters {
                append(
                    "f",
                    "f:$expectedString,l:5,o:0"
                )
                append("cv", "1")
                append(
                    "ca",
                    "i:${CART_ITEM_1_ITEM_ID_URL_ENCODED},p:${CART_ITEM_1.price},q:${CART_ITEM_1.quantity}"
                )
            }.formUrlEncode()
            val expectedUrl = "$RECOMMENDATION_BASE_URL?$params"

            val result = recommendationRequestFactory.create(requestRecommendationEvent)

            verifyResult(result, expectedUrl, shouldMockCartItemStorageBeCalled = true)
        }

    @Test
    fun test_create_recommendation_withVariants_shouldReturnRequestWithVariants_whenVariantIsNull() =
        runTest {
            every { mockCartItemStorage.items } returns mutableListOf(CART_ITEM_1)
            val requestRecommendationEvent = RequestRecommendation(
                options = RecommendationOptions(
                    logic = RecommendationLogic.Home,
                    variants = null
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

            verifyResult(result, expectedUrl, shouldMockCartItemStorageBeCalled = true)
        }

    @Test
    fun test_create_recommendation_withVariants_shouldReturnRequestWithVariants_whenVariantIsEmptyList() =
        runTest {
            every { mockCartItemStorage.items } returns mutableListOf(CART_ITEM_1)
            val requestRecommendationEvent = RequestRecommendation(
                options = RecommendationOptions(
                    logic = RecommendationLogic.Home,
                    variants = emptyList()
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

            verifyResult(result, expectedUrl, shouldMockCartItemStorageBeCalled = true)
        }

    @Test
    fun test_create_recommendation_withVariants_shouldReturnRequestWithVariants_whenVariantListHasSingleElement() =
        runTest {
            every { mockCartItemStorage.items } returns mutableListOf(CART_ITEM_1)
            val requestRecommendationEvent = RequestRecommendation(
                options = RecommendationOptions(
                    logic = RecommendationLogic.Home,
                    variants = listOf("_1")
                )
            )
            val params = parameters {
                append(
                    "f",
                    "f:HOME_1,l:5,o:0"
                )
                append("cv", "1")
                append(
                    "ca",
                    "i:${CART_ITEM_1_ITEM_ID_URL_ENCODED},p:${CART_ITEM_1.price},q:${CART_ITEM_1.quantity}"
                )
            }.formUrlEncode()
            val expectedUrl = "$RECOMMENDATION_BASE_URL?$params"

            val result = recommendationRequestFactory.create(requestRecommendationEvent)

            verifyResult(result, expectedUrl, shouldMockCartItemStorageBeCalled = true)
        }

    @Test
    fun test_create_recommendation_withVariants_shouldReturnRequestWithVariants_whenVariantListHasMultipleElement() =
        runTest {
            every { mockCartItemStorage.items } returns mutableListOf(CART_ITEM_1)
            val requestRecommendationEvent = RequestRecommendation(
                options = RecommendationOptions(
                    logic = RecommendationLogic.Home,
                    variants = listOf("_1","_2")
                )
            )
            val params = parameters {
                append(
                    "f",
                    "f:HOME_1,l:5,o:0,f:HOME_2,l:5,o:0"
                )
                append("cv", "1")
                append(
                    "ca",
                    "i:${CART_ITEM_1_ITEM_ID_URL_ENCODED},p:${CART_ITEM_1.price},q:${CART_ITEM_1.quantity}"
                )
            }.formUrlEncode()
            val expectedUrl = "$RECOMMENDATION_BASE_URL?$params"

            val result = recommendationRequestFactory.create(requestRecommendationEvent)

            verifyResult(result, expectedUrl, shouldMockCartItemStorageBeCalled = true)
        }

    @Test
    fun test_create_shouldReturn_recommendationUrlPath_withEmptyCart_and_withBasicHomeLogic() = runTest {
        val requestRecommendationEvent = RequestRecommendation(
            options = RecommendationOptions(
                RecommendationLogic.Home
            )
        )
        val params = parameters {
            append(
                "f",
                "f:HOME,l:5,o:0"
            )
            appendEmptyCart()
        }.formUrlEncode()
        val expectedUrl = "$RECOMMENDATION_BASE_URL?$params"

        val result = recommendationRequestFactory.create(requestRecommendationEvent)

        verifyResult(result, expectedUrl, shouldMockCartItemStorageBeCalled = true)
    }

    @Test
    fun test_create_shouldReturn_recommendationUrlPath_withEmptyCart_and_withLimit10() = runTest {
        val requestRecommendationEvent =
            RequestRecommendation(
                options = RecommendationOptions(
                    logic = RecommendationLogic.Home,
                    limit = 10
                )
            )
        val params = parameters {
            append(
                "f",
                "f:HOME,l:10,o:0"
            )
            appendEmptyCart()
        }.formUrlEncode()
        val expectedUrl = "$RECOMMENDATION_BASE_URL?$params"

        val result = recommendationRequestFactory.create(requestRecommendationEvent)

        verifyResult(result, expectedUrl, shouldMockCartItemStorageBeCalled = true)
    }

    @Test
    fun test_create_shouldReturn_recommendationUrlPath_withEmptyCart_and_withOffset() = runTest {
        val requestRecommendationEvent =
            RequestRecommendation(
                options = RecommendationOptions(
                    logic = RecommendationLogic.Home,
                    offset = 15
                )
            )
        val params = parameters {
            append(
                "f",
                "f:HOME,l:5,o:15"
            )
            appendEmptyCart()
        }.formUrlEncode()
        val expectedUrl = "$RECOMMENDATION_BASE_URL?$params"

        val result = recommendationRequestFactory.create(requestRecommendationEvent)

        verifyResult(result, expectedUrl, shouldMockCartItemStorageBeCalled = true)
    }

    @Test
    fun test_create_shouldReturn_recommendationUrlPath_withEmptyCart_withAvailabilityZone() = runTest {
        val requestRecommendationEvent = RequestRecommendation(
            options = RecommendationOptions(
                logic = RecommendationLogic.Home,
                availabilityZone = "eu"
            )
        )
        val params = parameters {
            append(
                "f",
                "f:HOME,l:5,o:0"
            )
            append("az", "eu")
            appendEmptyCart()
        }.formUrlEncode()
        val expectedUrl = "$RECOMMENDATION_BASE_URL?$params"

        val result = recommendationRequestFactory.create(requestRecommendationEvent)

        verifyResult(result, expectedUrl, shouldMockCartItemStorageBeCalled = true)
    }

    @Test
    fun test_create_shouldReturn_recommendationUrlPath_withEmptyCart_and_withLanguage() = runTest {
        val requestRecommendationEvent =
            RequestRecommendation(
                options = RecommendationOptions(
                    logic = RecommendationLogic.Home,
                    language = "hu"
                )
            )
        val params = parameters {
            append(
                "f",
                "f:HOME,l:5,o:0"
            )
            append("lang", "hu")
            appendEmptyCart()
        }.formUrlEncode()
        val expectedUrl = "$RECOMMENDATION_BASE_URL?$params"

        val result = recommendationRequestFactory.create(requestRecommendationEvent)

        verifyResult(result, expectedUrl, shouldMockCartItemStorageBeCalled = true)
    }

    @Test
    fun test_create_shouldReturn_recommendationUrlPath_withEmptyCart_and_withCurrency() = runTest {
        val requestRecommendationEvent =
            RequestRecommendation(
                options = RecommendationOptions(
                    logic = RecommendationLogic.Home,
                    displayCurrency = "EUR"
                )
            )
        val params = parameters {
            append(
                "f",
                "f:HOME,l:5,o:0"
            )
            append("currency", "EUR")
            appendEmptyCart()
        }.formUrlEncode()
        val expectedUrl = "$RECOMMENDATION_BASE_URL?$params"

        val result = recommendationRequestFactory.create(requestRecommendationEvent)

        verifyResult(result, expectedUrl, shouldMockCartItemStorageBeCalled = true)
    }

    @Test
    fun test_create_RequestRecommendation_shouldReturn_recommendationUrlPath_withCartItemsAttachedFromContextEvent_whenContextEventIsCart() =
        runTest {
            val requestRecommendationEvent =
                RequestRecommendation(
                    options = RecommendationOptions(
                        logic = RecommendationLogic.Home,
                    ),
                    contextEvent = SdkEvent.External.RecommendationTrackEvent.Cart(
                        items = listOf(
                            CART_ITEM_1
                        )
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

            verifyResult(result, expectedUrl, shouldMockCartItemStorageBeCalled = false)
        }

    @Test
    fun test_create_RequestRecommendation_shouldReturn_recommendationUrlPath_withEmptyCartItemsAttached_whenContextEventIsPurchase() =
        runTest {
            val requestRecommendationEvent =
                RequestRecommendation(
                    options = RecommendationOptions(
                        logic = RecommendationLogic.Home,
                    ),
                    contextEvent = SdkEvent.External.RecommendationTrackEvent.Purchase(
                        TEST_ORDER_ID,
                        listOf(CART_ITEM_1)
                    )
                )
            val params = parameters {
                append(
                    "f",
                    "f:HOME,l:5,o:0"
                )
                append("oi", TEST_ORDER_ID)
                append(
                    "co",
                    "i:${CART_ITEM_1_ITEM_ID_URL_ENCODED},p:${CART_ITEM_1.price},q:${CART_ITEM_1.quantity}"
                )
                appendEmptyCart()
            }.formUrlEncode()
            val expectedUrl = "$RECOMMENDATION_BASE_URL?$params"

            val result = recommendationRequestFactory.create(requestRecommendationEvent)

            verifyResult(result, expectedUrl, shouldMockCartItemStorageBeCalled = false)
        }

    @Test
    fun test_create_RequestRecommendation_shouldReturn_recommendationUrlPath_withEmptyCartItemsAttachedFromStorage_whenContextEventIsNotPurchase_andNotCart() =
        runTest {
            val requestRecommendationEvent =
                RequestRecommendation(
                    options = RecommendationOptions(
                        logic = RecommendationLogic.Home,
                    ),
                    contextEvent = SdkEvent.External.RecommendationTrackEvent.ItemView(
                        CART_ITEM_1.itemId
                    )
                )
            val params = parameters {
                append(
                    "f",
                    "f:HOME,l:5,o:0"
                )
                append("v", "i:$CART_ITEM_1_ITEM_ID_URL_ENCODED")
                appendEmptyCart()
            }.formUrlEncode()
            val expectedUrl = "$RECOMMENDATION_BASE_URL?$params"

            val result = recommendationRequestFactory.create(requestRecommendationEvent)

            verifyResult(result, expectedUrl, shouldMockCartItemStorageBeCalled = true)
        }

    @Test
    fun test_create_RequestRecommendation_shouldReturn_recommendationUrlPath_withNonEmptyCartItemsAttachedFromStorage_whenContextEventIsNotPurchase_andNotCart() =
        runTest {
            every { mockCartItemStorage.items } returns mutableListOf(CART_ITEM_1)
            val requestRecommendationEvent =
                RequestRecommendation(
                    options = RecommendationOptions(
                        logic = RecommendationLogic.Home,
                    ),
                    contextEvent = SdkEvent.External.RecommendationTrackEvent.ItemView(
                        CART_ITEM_1.itemId
                    )
                )
            val params = parameters {
                append(
                    "f",
                    "f:HOME,l:5,o:0"
                )
                append("v", "i:$CART_ITEM_1_ITEM_ID_URL_ENCODED")
                append("cv", "1")
                append(
                    "ca",
                    "i:${CART_ITEM_1_ITEM_ID_URL_ENCODED},p:${CART_ITEM_1.price},q:${CART_ITEM_1.quantity}"
                )
            }.formUrlEncode()
            val expectedUrl = "$RECOMMENDATION_BASE_URL?$params"

            val result = recommendationRequestFactory.create(requestRecommendationEvent)

            verifyResult(result, expectedUrl, shouldMockCartItemStorageBeCalled = true)
        }

    @Test
    fun test_create_shouldReturn_recommendationUrlPath_withFilters_parameterized() = runTest {
        data class TestCase(val options: RecommendationOptions, val expectedUrl: String)

        listOf(
            TestCase(
                options = RecommendationOptions(RecommendationLogic.Home, filters = null),
                expectedUrl = "$RECOMMENDATION_BASE_URL?${
                    parameters {
                        append("f", "f:HOME,l:5,o:0")
                        appendEmptyCart()
                    }.formUrlEncode()
                }"
            ),
            TestCase(
                options = RecommendationOptions(RecommendationLogic.Home, filters = emptyList()),
                expectedUrl = "$RECOMMENDATION_BASE_URL?${
                    parameters {
                        append("f", "f:HOME,l:5,o:0")
                        appendEmptyCart()
                    }.formUrlEncode()
                }"
            ),
            TestCase(
                options = RecommendationOptions(
                    RecommendationLogic.Home,
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
                        appendEmptyCart()
                    }.formUrlEncode()
                }"
            ),
            TestCase(
                options = RecommendationOptions(
                    RecommendationLogic.Home,
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
                        appendEmptyCart()
                    }.formUrlEncode()
                }"
            ),
            TestCase(
                options = RecommendationOptions(
                    RecommendationLogic.Home,
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
                        appendEmptyCart()
                    }.formUrlEncode()
                }"
            ),
            TestCase(
                options = RecommendationOptions(
                    RecommendationLogic.Home,
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
                        appendEmptyCart()
                    }.formUrlEncode()
                }"
            ),
            TestCase(
                options = RecommendationOptions(
                    RecommendationLogic.Home,
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
                        appendEmptyCart()
                    }.formUrlEncode()
                }"
            ),
            TestCase(
                options = RecommendationOptions(
                    RecommendationLogic.Home, filters = listOf(
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
                        appendEmptyCart()
                    }.formUrlEncode()
                }"
            ),
            TestCase(
                options = RecommendationOptions(
                    RecommendationLogic.Home, limit = 10, offset = 20, filters = listOf(
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
                        appendEmptyCart()
                    }.formUrlEncode()
                }"
            )
        ).forEach { (options, expectedUrl) ->
            val result =
                recommendationRequestFactory.create(RequestRecommendation(options = options))
            verifyResult(result, expectedUrl, shouldMockCartItemStorageBeCalled = true)
            resetCalls(mockUrlFactory)
        }
    }

    private fun verifyResult(
        result: UrlRequest,
        expectedUrl: String,
        shouldMockCartItemStorageBeCalled: Boolean
    ) {
        result.url.toString() shouldBe expectedUrl
        result.method shouldBe HttpMethod.Get
        verifySuspend(VerifyMode.exactly(1)) { mockUrlFactory.create(ECUrlType.Recommendation) }
        if (shouldMockCartItemStorageBeCalled) {
            verify { mockCartItemStorage.items }
        } else {
            verify(VerifyMode.exactly(0)) { mockCartItemStorage.items }
        }
    }

    private fun ParametersBuilder.appendEmptyCart() {
        append("cv", "1")
        append(
            "ca",
            ""
        )
    }
}