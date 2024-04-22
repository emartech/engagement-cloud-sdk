package com.emarsys.api.predict

import com.emarsys.api.predict.model.PredictCartItem
import com.emarsys.api.predict.model.RecommendationLogic
import com.emarsys.api.predict.model.RecommendationLogic.Companion.alsoBought
import com.emarsys.api.predict.model.RecommendationLogic.Companion.cart
import com.emarsys.api.predict.model.RecommendationLogic.Companion.category
import com.emarsys.api.predict.model.RecommendationLogic.Companion.home
import com.emarsys.api.predict.model.RecommendationLogic.Companion.personal
import com.emarsys.api.predict.model.RecommendationLogic.Companion.popular
import com.emarsys.api.predict.model.RecommendationLogic.Companion.related
import com.emarsys.api.predict.model.RecommendationLogic.Companion.search
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class RecommendationLogicTest {

    @Test
    fun testConstructor_variants_mustBeEmptyList_withoutVariantsParam() {
        val logic = RecommendationLogic(RecommendationLogic.SEARCH, mapOf())

        logic.variants shouldBe listOf()
    }

    @Test
    fun testConstructor_variants_mustBeEmptyList_withoutVariantsParam_data_mustBeEmptyMap_withoutDataParam() {
        val logic = RecommendationLogic(RecommendationLogic.SEARCH)

        logic.variants shouldBe listOf()
        logic.data shouldBe mapOf()
    }

    @Test
    fun testSearch_shouldFillFields() {
        val result = search()

        result.data shouldBe mapOf()
        result.logicName shouldBe "SEARCH"
    }

    @Test
    fun testSearch_shouldFillFields_ifDataIsProvided() {
        val expectedData = mapOf("q" to "searchTerm")
        val result = search("searchTerm")

        result.data shouldBe expectedData
        result.logicName shouldBe "SEARCH"
    }

    @Test
    fun testCart_shouldFillFields() {
        val result = cart()

        result.data shouldBe mapOf()
        result.logicName shouldBe "CART"
    }

    @Test
    fun testCart_shouldFillFields_ifDataIsProvided() {
        val data = mapOf(
                "cv" to "1",
                "ca" to "i:itemId1,p:200.1,q:100.1|i:itemId2,p:201.2,q:101.2"
        )

        val cartItems = listOf(
                PredictCartItem("itemId1", 200.1, 100.1),
                PredictCartItem("itemId2", 201.2, 101.2)
        )
        val result = cart(cartItems)

        result.data shouldBe data
        result.logicName shouldBe "CART"
    }

    @Test
    fun testRelated_shouldFillFields() {
        val result = related()

        result.data shouldBe mapOf()
        result.logicName shouldBe "RELATED"
    }

    @Test
    fun testRelated_shouldFillFields_ifDataIsProvided() {
        val data = mapOf("v" to "i:itemId")

        val result = related("itemId")

        result.data shouldBe data
        result.logicName shouldBe "RELATED"
    }

    @Test
    fun testCategory_shouldFillFields() {
        val result = category()

        result.data shouldBe mapOf()
        result.logicName shouldBe "CATEGORY"
    }

    @Test
    fun testCategory_shouldFillFields_ifDataIsProvided() {
        val data = mapOf(
                "vc" to "testCategoryPath"
        )

        val result = category("testCategoryPath")

        result.data shouldBe data
        result.logicName shouldBe "CATEGORY"
    }

    @Test
    fun testAlsoBought_shouldFillFields() {
        val result = alsoBought()

        result.data shouldBe mapOf()
        result.logicName shouldBe "ALSO_BOUGHT"
    }

    @Test
    fun testAlsoBought_shouldFillFields_ifDataIsProvided() {
        val data = mapOf("v" to "i:itemId")

        val result = alsoBought("itemId")

        result.data shouldBe data
        result.logicName shouldBe "ALSO_BOUGHT"
    }

    @Test
    fun testPopular_shouldFillFields() {
        val result = popular()

        result.data shouldBe mapOf()
        result.logicName shouldBe "POPULAR"
    }

    @Test
    fun testPopular_shouldFillFields_ifDataIsProvided() {
        val data = mapOf("vc" to "testCategoryPath")

        val result = popular("testCategoryPath")

        result.data shouldBe data
        result.logicName shouldBe "POPULAR"
    }

    @Test
    fun testPersonal_shouldFillFields() {
        val result = personal()

        result.data shouldBe mapOf()
        result.logicName shouldBe "PERSONAL"
    }

    @Test
    fun testPersonal_shouldFillFields_withVariants() {
        val expectedData: Map<String, String> = mapOf()
        val expectedVariants = listOf(
                "1",
                "2",
                "3"
        )
        val result = personal(expectedVariants)

        result.data shouldBe expectedData
        result.variants shouldBe expectedVariants
        result.logicName shouldBe "PERSONAL"
    }

    @Test
    fun testHome_shouldFillFields() {
        val result = home()

        result.data shouldBe mapOf()
        result.logicName shouldBe "HOME"
    }

    @Test
    fun testHome_shouldFillFields_withVariants() {
        val expectedData: Map<String, String> = mapOf()
        val expectedVariants = listOf(
                "1",
                "2",
                "3"
        )
        val result = home(expectedVariants)

        result.data shouldBe expectedData
        result.variants shouldBe expectedVariants
        result.logicName shouldBe "HOME"
    }
}