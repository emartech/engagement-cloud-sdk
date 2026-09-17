package com.sap.ec.recommendation

data class RecommendationFilter(
    val type: FilterType,
    val field: String,
    val comparison: ComparisonType,
    val expectations: List<String>
) {
    constructor(type: FilterType, field: String, comparison: ComparisonType, expectation: String) :
        this(type, field, comparison, listOf(expectation))
}

enum class FilterType {
    INCLUDE,
    EXCLUDE
}

enum class ComparisonType {
    IS,
    IN,
    HAS,
    OVERLAPS
}
