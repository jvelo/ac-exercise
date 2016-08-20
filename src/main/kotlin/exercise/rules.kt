package exercise

val oidiumSporulation = Rule(
        name = "Oidium sporulation",
        duration = 60,
        expressions = listOf(Expression(Dimension.HUMIDITY, Operator.GREATER_THAN, 90.0))
)

val botrytis = Rule(
        name = "Botrytis",
        duration = 360,
        expressions = listOf(
                Expression(Dimension.HUMIDITY, Operator.GREATER_THAN, 90.0),
                Expression(Dimension.TEMPERATURE, Operator.GREATER_THAN, 15.0),
                Expression(Dimension.TEMPERATURE, Operator.LESSER_THAN, 20.0))
)

val oidiumDevelopment = Rule(
        name = "Oidium development",
        duration = 600,
        expressions = listOf(
                Expression(Dimension.HUMIDITY, Operator.LESSER_THAN, 70.0),
                Expression(Dimension.TEMPERATURE, Operator.GREATER_THAN, 20.0)
        ),
        preconditions = listOf(oidiumSporulation)
)

val allRules = arrayOf(oidiumSporulation, botrytis, oidiumDevelopment)