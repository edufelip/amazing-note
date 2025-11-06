package com.edufelip.shared.ui.indication

import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DrawModifierNode

/**
 * Simple, no-op indication that satisfies the new clickable requirement.
 * It draws nothing and exists only to avoid runtime crashes when a non-NodeFactory
 * Indication is provided through LocalIndication.
 */
object NoFeedbackIndication : IndicationNodeFactory {
    override fun create(interactionSource: InteractionSource): DelegatableNode = object : Modifier.Node(), DrawModifierNode {
        override fun ContentDrawScope.draw() {
            drawContent()
        }
    }

    // Stateless singleton
    override fun hashCode(): Int = 0
    override fun equals(other: Any?): Boolean = other === this
}
