package com.mineinabyss.geary.papermc.menus

import com.mineinabyss.idofront.textcomponents.miniMsg
import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.action.DialogAction
import io.papermc.paper.registry.data.dialog.input.DialogInput
import io.papermc.paper.registry.data.dialog.type.DialogType
import net.kyori.adventure.text.event.ClickCallback

private const val INPUT = "query"
private const val MAX_LENGTH = 32

// The width of the bar painted into the title texture, the box draws its border inside it
private const val INPUT_WIDTH = 127

/** Submitting an empty field clears the search */
fun searchDialog(title: String, current: String, onSubmit: (String) -> Unit): Dialog = Dialog.create { builder ->
    val submit = DialogAction.customClick({ response, _ ->
        response.getText(INPUT)?.trim()?.let(onSubmit)
    }, ClickCallback.Options.builder().build())
    builder.empty()
        .type(DialogType.notice(ActionButton.builder("<gold>Search".miniMsg()).action(submit).build()))
        .base(
            DialogBase.builder(title.miniMsg())
                .afterAction(DialogBase.DialogAfterAction.CLOSE)
                .pause(false)
                .canCloseWithEscape(true)
                .inputs(listOf(
                    DialogInput.text(INPUT, "<gold>Search items by name or id".miniMsg())
                        .initial(current).labelVisible(false).width(INPUT_WIDTH).maxLength(MAX_LENGTH).build()
                ))
                .build()
        )
}
