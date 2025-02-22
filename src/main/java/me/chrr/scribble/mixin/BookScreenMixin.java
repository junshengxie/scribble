package me.chrr.scribble.mixin;

import me.chrr.scribble.Scribble;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.BookScreen;
import net.minecraft.client.gui.widget.Widget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BookScreen.class)
public abstract class BookScreenMixin extends Screen {
    // Dummy constructor to match super class.
    private BookScreenMixin() {
        super(null);
    }

    // If we need to center the GUI, we shift the Y of the texture draw call down.
    // For 1.20.1, this draw call happens in render, so we don't need to do it separately.
    //? if >=1.20.2 {
    //? if >=1.21.2 {
    @ModifyArg(method = "renderBackground", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Ljava/util/function/Function;Lnet/minecraft/util/Identifier;IIFFIIII)V"), index = 3)
    //?} else
    /*@ModifyArg(method = "renderBackground", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Lnet/minecraft/util/Identifier;IIIIII)V"), index = 2)*/
    public int shiftBackgroundY(int y) {
        return Scribble.getBookScreenYOffset(height) + y;
    }
    //?}

    // If we need to center the GUI, we shift the Y of the close buttons down.
    @Redirect(method = "addCloseButton", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/BookScreen;addDrawableChild(Lnet/minecraft/client/gui/Element;)Lnet/minecraft/client/gui/Element;"))
    public <T extends Element & Drawable & Selectable> T shiftCloseButtonY(BookScreen instance, T element) {
        if (element instanceof Widget widget) {
            widget.setY(widget.getY() + Scribble.getBookScreenYOffset(height));
        }

        return addDrawableChild(element);
    }

    // If we need to center the GUI, we shift the Y of the page buttons down.
    @Redirect(method = "addPageButtons", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/BookScreen;addDrawableChild(Lnet/minecraft/client/gui/Element;)Lnet/minecraft/client/gui/Element;"))
    public <T extends Element & Drawable & Selectable> T shiftPageButtonY(BookScreen instance, T element) {
        if (element instanceof Widget widget) {
            widget.setY(widget.getY() + Scribble.getBookScreenYOffset(height));
        }

        return addDrawableChild(element);
    }

    // If we need to center the GUI, modify the coordinates to check.
    @ModifyVariable(method = "getTextStyleAt", at = @At(value = "HEAD"), ordinal = 1, argsOnly = true)
    public double shiftTextStyleY(double y) {
        return y - Scribble.getBookScreenYOffset(height);
    }

    // When rendering, we translate the matrices of the draw context to draw the text further down if needed.
    // Note that this happens after the parent screen render, so only the text in the book is shifted.
    //? if >=1.20.2 {
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;render(Lnet/minecraft/client/gui/DrawContext;IIF)V", shift = At.Shift.AFTER))
    //?} else
    /*@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/BookScreen;renderBackground(Lnet/minecraft/client/gui/DrawContext;)V", shift = At.Shift.AFTER))*/
    public void translateRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        context.getMatrices().push();
        context.getMatrices().translate(0f, Scribble.getBookScreenYOffset(height), 0f);
    }

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawHoverEvent(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Style;II)V"), index = 3)
    public int shiftHoverTooltipY(int y) {
        return y - Scribble.getBookScreenYOffset(height);
    }

    // At the end of rendering, we need to pop those matrices we pushed.
    //? if >=1.20.2 {
    @Inject(method = "render", at = @At(value = "RETURN"))
    //?} else
    /*@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;render(Lnet/minecraft/client/gui/DrawContext;IIF)V"))*/
    public void popRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        context.getMatrices().pop();
    }
}
