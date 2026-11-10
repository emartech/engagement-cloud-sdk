@file:OptIn(ExperimentalJsExport::class)

package com.sap.ec.api.recommendation.util

import com.sap.ec.api.recommendation.model.JsRecommendationLogic
import com.sap.ec.api.recommendation.model.JsRecommendationOptionsConfigApi

@JsExport
@JsName("RecommendationOptionsBuilder")
class JsRecommendationOptionsBuilder {

    fun withLogicRelated(): JsRecommendationOptionsConfigApi = JsRecommendationOptionsConfigBuilder(JsRecommendationLogic.RELATED.name)
    fun withLogicAlsoBought(): JsRecommendationOptionsConfigApi = JsRecommendationOptionsConfigBuilder(JsRecommendationLogic.ALSOBOUGHT.name)
    fun withLogicCart(): JsRecommendationOptionsConfigApi = JsRecommendationOptionsConfigBuilder(JsRecommendationLogic.CART.name)
    fun withLogicCategory(): JsRecommendationOptionsConfigApi = JsRecommendationOptionsConfigBuilder(JsRecommendationLogic.CATEGORY.name)
    fun withLogicPopular(): JsRecommendationOptionsConfigApi = JsRecommendationOptionsConfigBuilder(JsRecommendationLogic.POPULAR.name)
    fun withLogicSearch(): JsRecommendationOptionsConfigApi = JsRecommendationOptionsConfigBuilder(JsRecommendationLogic.SEARCH.name)
    fun withLogicPersonal(): JsRecommendationOptionsConfigApi = JsRecommendationOptionsConfigBuilder(JsRecommendationLogic.PERSONAL.name)
    fun withLogicHome(): JsRecommendationOptionsConfigApi = JsRecommendationOptionsConfigBuilder(JsRecommendationLogic.HOME.name)
    fun withLogicCustom(logic: String): JsRecommendationOptionsConfigApi = JsRecommendationOptionsConfigBuilder(logic)
}
