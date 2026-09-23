package com.sap.ec.networking.clients.recommendation

import com.sap.ec.core.mapper.Mapper
import com.sap.ec.core.networking.model.Response
import com.sap.ec.recommendation.Product

interface RecommendationResponseMapperApi : Mapper<Response, List<Product>>