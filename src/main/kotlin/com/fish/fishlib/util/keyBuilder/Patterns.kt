package com.fish.fishlib.util.keyBuilder

enum class Patterns(override val pattern: String) : IKeyPattern {
    CreativeTab("creative_tab.%s%s"),
    Tooltip("tooltip.%s%s"),
    ScreenTooltip("tooltip.screen.%s%s"),
    Message("message.%s%s"),
    ActionBar("message.actionbar.%s%s"),
    Screen("screen.%s%s"),
    KeywordGroup("keywordGroup.%s%s"),
    Config("%s.configuration%s"),
    Key("key.%s%s"),
    KeyCategory("key.category.%s%s"),
    ViewerInfo("recipe_viewer.info.%s%s"),
    ViewerTooltip("recipe_viewer.tooltip.%s%s"),
    ViewerCategory("recipe_viewer.category.%s%s"),
    JadeInfo("jade.info.%s%s"),
    JadeConfig("config.jade.plugin_%s%s"),
    ;
}

interface IKeyPattern {
    val pattern: String
}

fun String.toKeyPattern() = object : IKeyPattern {
    override val pattern = this@toKeyPattern
}