package com.emarsys.api.predict.model

import kotlinx.serialization.Serializable

@Serializable
class RecommendationFilter {
    companion object {
        private const val IS = "IS"
        private const val IN = "IN"
        private const val HAS = "HAS"
        private const val OVERLAPS = "OVERLAPS"

        fun include(field: String): Include {
            return Include.include(field)
        }

        fun exclude(field: String): Exclude {
            return Exclude.exclude(field)
        }
    }

    val type: String
    val field: String
    val comparison: String
    val expectations: List<String?>

    internal constructor(type: String, field: String, comparison: String, expectations: List<String?>) {
        this.type = type
        this.field = field
        this.comparison = comparison
        this.expectations = expectations
    }

    internal constructor(type: String, field: String, comparison: String, expectation: String) {
        this.type = type
        this.field = field
        this.comparison = comparison
        expectations = listOf(expectation)
    }

    class Exclude private constructor(val field: String) {

        companion object {
            private const val TYPE = "EXCLUDE"

            fun exclude(field: String): Exclude {
                return Exclude(field)
            }
        }

        fun isValue(value: String): RecommendationFilter {
            return RecommendationFilter(TYPE, field, IS, value)
        }

        fun inValues(values: List<String>): RecommendationFilter {
            return RecommendationFilter(TYPE, field, IN, values)
        }

        fun hasValue(value: String): RecommendationFilter {
            return RecommendationFilter(TYPE, field, HAS, value)
        }

        fun overlapsValues(values: List<String>): RecommendationFilter {
            return RecommendationFilter(TYPE, field, OVERLAPS, values)
        }


    }

    class Include private constructor(val field: String) {
        companion object {
            private const val TYPE = "INCLUDE"

            fun include(field: String): Include {
                return Include(field)
            }
        }

        fun isValue(value: String): RecommendationFilter {
            return RecommendationFilter(TYPE, field, IS, value)
        }

        fun inValues(values: List<String>): RecommendationFilter {
            return RecommendationFilter(TYPE, field, IN, values)
        }

        fun hasValue(value: String): RecommendationFilter {
            return RecommendationFilter(TYPE, field, HAS, value)
        }

        fun overlapsValues(values: List<String>): RecommendationFilter {
            return RecommendationFilter(TYPE, field, OVERLAPS, values)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as RecommendationFilter

        if (type != other.type) return false
        if (field != other.field) return false
        if (comparison != other.comparison) return false
        if (expectations != other.expectations) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + field.hashCode()
        result = 31 * result + comparison.hashCode()
        result = 31 * result + expectations.hashCode()
        return result
    }

    override fun toString(): String {
        return "Type: $type, field: $field, comparison: $comparison, expectations: $expectations"
    }

}